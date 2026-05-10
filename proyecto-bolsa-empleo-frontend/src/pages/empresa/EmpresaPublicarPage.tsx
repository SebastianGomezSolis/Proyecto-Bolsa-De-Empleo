import { useEffect, useState } from 'react';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';
import { api } from '../../services/api';
import { Sesion, MensajeGlobal, Caracteristica } from '../../types';

interface Props {
  // Sesión actual del usuario (se usa para verificar rol de empresa)
  sesion: Sesion | null;
  // Función de navegación para redirigir al usuario a otras páginas
  onNavegar: (ruta: string) => void;
  // Función de callback para mostrar mensajes globales (éxito/error)
  onMensaje: (m: MensajeGlobal) => void;
}

// Componente principal para publicar un nuevo puesto de trabajo
// Incluye formulario con descripción, salario, tipo de publicación y características requeridas
function EmpresaPublicarPage({ sesion, onNavegar, onMensaje }: Props) {
  // Estado del formulario: descripción, salario y tipo de publicación
  const [form, setForm] = useState({ descripcion: '', salario: '', tipoPublicacion: 'publico' });
  // Estado para almacenar la jerarquía de características disponibles
  const [raices, setRaices] = useState<Caracteristica[]>([]);
  // Estado para almacenar las características seleccionadas y su nivel mínimo
  const [selCaracteristicas, setSelCaracteristicas] = useState<Record<number, number>>({});
  // Estado para indicar si se están cargando las características
  const [cargando, setCargando] = useState(true);
  // Estado para indicar si se está enviando el formulario
  const [enviando, setEnviando] = useState(false);

  // Effect para cargar la jerarquía completa de características al montar el componente
  useEffect(() => {
    // Solo proceder si el usuario es una empresa
    if (!sesion || sesion.rol !== 'EMPRESA') return;
    const cargar = async () => {
      try {
        // Obtener las características raíz y construir el árbol completo
        const raiz = await api.getCaracteristicasRaiz();
        const conHijos = await Promise.all(
          raiz.map(async (r) => {
            const hijos = await api.getCaracteristicasHijos(r.id);
            const hijosConNietos = await Promise.all(
              hijos.map(async (h) => {
                const nietos = await api.getCaracteristicasHijos(h.id);
                return { ...h, hijos: nietos };
              })
            );
            return { ...r, hijos: hijosConNietos };
          })
        );
        setRaices(conHijos);
      } catch (e) {
        onMensaje({ tipo: 'danger', texto: (e as Error).message });
      } finally {
        setCargando(false);
      }
    };
    cargar();
  }, [sesion, onMensaje]);

  // Verificar si el usuario tiene sesión activa y rol de empresa
  // Si no cumple, mostrar mensaje de acceso restringido con botón para volver
  if (!sesion || sesion.rol !== 'EMPRESA') {
    return (
      <section className="container py-5">
        <div className="alert alert-warning">Acceso restringido a empresas autorizadas.</div>
        <button className="btn btn-outline-secondary" onClick={() => onNavegar('/')}>Volver</button>
      </section>
    );
  }

  // Función auxiliar para actualizar un campo del formulario
  const set = (campo: string, valor: string) => setForm((prev) => ({ ...prev, [campo]: valor }));

  // Función para marcar o desmarcar una característica requerida
  const toggleCaracteristica = (id: number, checked: boolean) => {
    setSelCaracteristicas((prev) => {
      const next = { ...prev };
      if (checked) { next[id] = 1; } else { delete next[id]; }
      return next;
    });
  };

  // Función para establecer el nivel mínimo requerido de una característica
  const setNivel = (id: number, nivel: number) => setSelCaracteristicas((prev) => ({ ...prev, [id]: Number(nivel) }));

  // Manejador del envío del formulario para crear un nuevo puesto
  const manejarEnvio = async (e: React.FormEvent) => {
    e.preventDefault();
    setEnviando(true);
    try {
      // Preparar los datos de las características seleccionadas con sus niveles
      const caracteristicaIds = Object.keys(selCaracteristicas).map(Number);
      const niveles = caracteristicaIds.map((id) => ({ id, nivel: Number(selCaracteristicas[id]) }));

      // Llamar al API para crear el puesto con todos los datos del formulario
      await api.crearPuesto({
        ...form,
        salario: Number(form.salario),
        caracteristicaIds,
        niveles,
      });
      onMensaje({ tipo: 'success', texto: 'Puesto publicado correctamente.' });
      onNavegar('/empresa/puestos'); // Redirigir a la lista de puestos
    } catch (error) {
      onMensaje({ tipo: 'danger', texto: (error as Error).message });
    } finally {
      setEnviando(false);
    }
  };

  // Función recursiva para renderizar una característica y sus hijos (sub-características)
  const renderCaracteristica = (item: Caracteristica) => {
    // Si es una hoja (no tiene hijos), mostrar checkbox con selector de nivel
    const esHoja = !item.hijos || item.hijos.length === 0;
    if (esHoja) {
      return (
        <div key={item.id} className="d-flex align-items-center gap-3 mb-1">
          <input className="form-check-input" type="checkbox" id={`car_${item.id}`}
            checked={selCaracteristicas[item.id] !== undefined}
            onChange={(e) => toggleCaracteristica(item.id, e.target.checked)} />
          <label className="form-check-label flex-grow-1" htmlFor={`car_${item.id}`}>{item.nombre}</label>
          {/* Mostrar selector de nivel solo si la característica está seleccionada */}
          {selCaracteristicas[item.id] !== undefined && (
            <div className="d-flex align-items-center gap-1">
              <label className="text-muted mb-0" style={{ fontSize: '0.8rem' }}>Nivel:</label>
              <select className="form-select form-select-sm" style={{ width: '70px' }}
                value={selCaracteristicas[item.id]}
                onChange={(e) => setNivel(item.id, Number(e.target.value))}>
                {[1, 2, 3, 4, 5].map((n) => <option key={n} value={n}>{n}</option>)}
              </select>
            </div>
          )}
        </div>
      );
    }
    // Si tiene hijos, mostrar como contenedor y renderizar recursivamente
    return (
      <div key={item.id} className="mb-2">
        <div className="fw-semibold text-secondary mb-1" style={{ fontSize: '0.88rem' }}>{item.nombre}</div>
        <div className="ms-3">
          {item.hijos?.map((nieto) => renderCaracteristica(nieto))}
        </div>
      </div>
    );
  };

  return (
    <section className="container py-5" style={{ maxWidth: '750px' }}>
      <SectionTitle eyebrow="Empresa" title="Publicar puesto" />

      {cargando ? <LoadingBlock /> : (
        <form onSubmit={manejarEnvio}>
          {/* Campo de descripción del puesto */}
          <div className="mb-3">
            <label className="form-label fw-semibold">Descripción del puesto</label>
            <textarea className="form-control" rows={4} value={form.descripcion}
              onChange={(e) => set('descripcion', e.target.value)}
              placeholder="Describa el puesto y sus responsabilidades..." required />
          </div>
          {/* Campo de salario ofrecido */}
          <div className="mb-3">
            <label className="form-label fw-semibold">Salario ($)</label>
            <input type="number" className="form-control" value={form.salario} min={0} step="any"
              onChange={(e) => set('salario', e.target.value)} placeholder="Ej: 5000" required />
          </div>
          {/* Selector de tipo de publicación: público o privado */}
          <div className="mb-4">
            <label className="form-label fw-semibold">Tipo de publicación</label>
            <div className="d-flex gap-4">
              <div className="form-check">
                <input className="form-check-input" type="radio" id="tipoPublico" value="publico"
                  checked={form.tipoPublicacion === 'publico'} onChange={() => set('tipoPublicacion', 'publico')} />
                <label className="form-check-label" htmlFor="tipoPublico">
                  Público <span className="text-muted" style={{ fontSize: '0.85rem' }}>(visible para todos)</span>
                </label>
              </div>
              <div className="form-check">
                <input className="form-check-input" type="radio" id="tipoPrivado" value="privado"
                  checked={form.tipoPublicacion === 'privado'} onChange={() => set('tipoPublicacion', 'privado')} />
                <label className="form-check-label" htmlFor="tipoPrivado">
                  Privado <span className="text-muted" style={{ fontSize: '0.85rem' }}>(solo usuarios registrados)</span>
                </label>
              </div>
            </div>
          </div>

          {/* Sección de características requeridas con selección de nivel */}
          <div className="mb-4">
            <label className="form-label fw-semibold">Características requeridas</label>
            <p className="text-muted" style={{ fontSize: '0.85rem' }}>
              Seleccioná las características y el nivel mínimo requerido (1 = básico, 5 = experto).
            </p>
            {/* Recorrer las características raíz y mostrarlas agrupadas */}
            {raices.map((raiz) => (
              <div key={raiz.id} className="mb-3">
                <div className="border rounded">
                  <div className="bg-secondary bg-opacity-10 px-3 py-2 fw-semibold" style={{ fontSize: '0.95rem' }}>
                    {raiz.nombre}
                  </div>
                  <div className="px-3 py-2">
                    {raiz.hijos?.map((hijo) => renderCaracteristica(hijo))}
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* Botones de acción: publicar y cancelar */}
          <div className="d-flex gap-2">
            <button type="submit" className="btn btn-dark" disabled={enviando}>
              {enviando ? 'Publicando...' : 'Publicar puesto'}
            </button>
            <button type="button" className="btn btn-outline-dark" onClick={() => onNavegar('/empresa/puestos')}>
              Cancelar
            </button>
          </div>
        </form>
      )}
    </section>
  );
}

export default EmpresaPublicarPage;
