import { useEffect, useState } from 'react';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';
import { api } from '../../services/api';
import { Sesion, MensajeGlobal, Caracteristica, Habilidad } from '../../types';

interface Props {
  // Sesión actual del usuario (se usa para verificar rol de oferente)
  sesion: Sesion | null;
  // Función de navegación para redirigir al usuario a otras páginas
  onNavegar: (ruta: string) => void;
  // Función de callback para mostrar mensajes globales (éxito/error)
  onMensaje: (m: MensajeGlobal) => void;
}

// Componente principal para gestionar las habilidades del oferente
function OferenteHabilidadesPage({ sesion, onNavegar, onMensaje }: Props) {
  // Lista de habilidades ya registradas del oferente
  const [habilidades, setHabilidades] = useState<Habilidad[]>([]);
  // Subcategorías visibles en la navegación actual
  const [subcategorias, setSubcategorias] = useState<Caracteristica[]>([]);
  // Ruta de navegación (breadcrumb) dentro del árbol de categorías
  const [ruta, setRuta] = useState<Caracteristica[]>([]);
  // Nodo (categoría) actualmente seleccionado en la navegación
  const [actual, setActual] = useState<Caracteristica | null>(null);
  // ID de la característica hoja seleccionada para agregar
  const [selId, setSelId] = useState<number | null>(null);
  // Nivel (1-5) para la nueva habilidad
  const [nivel, setNivel] = useState(1);
  // Indicador de carga inicial de datos
  const [cargando, setCargando] = useState(true);
  // Indicador de navegación en curso entre categorías
  const [navegando, setNavegando] = useState(false);
  // Conjunto de IDs de características que son hojas (sin hijos)
  const [hojas, setHojas] = useState<Set<number>>(new Set());

  // Recargar la lista de habilidades desde el backend
  const cargarHabilidades = () =>
    api.getHabilidades()
      .then(setHabilidades)
      .catch((e: Error) => onMensaje({ tipo: 'danger', texto: e.message }));

  // Cargar subcategorías de un padre (o raíces si padreId es null)
  const cargarSubcategorias = async (padreId: number | null = null) => {
    try {
      const data = padreId ? await api.getCaracteristicasHijos(padreId) : await api.getCaracteristicasRaiz();
      setSubcategorias(data);
      // Determinar qué nodos son hojas consultando sus hijos
      const nuevasHojas = new Set(hojas);
      await Promise.all(data.map(async (n) => {
        const hijos = await api.getCaracteristicasHijos(n.id);
        if (hijos.length === 0) nuevasHojas.add(n.id);
        else nuevasHojas.delete(n.id);
      }));
      setHojas(nuevasHojas);
    } catch (e) {
      onMensaje({ tipo: 'danger', texto: (e as Error).message });
    }
  };

  // Effect para cargar habilidades y categorías raíz al montar el componente
  useEffect(() => {
    if (!sesion || sesion.rol !== 'OFERENTE') return;
    Promise.all([api.getHabilidades(), api.getCaracteristicasRaiz()])
      .then(([h, r]) => { setHabilidades(h); setSubcategorias(r); })
      .catch((e: Error) => onMensaje({ tipo: 'danger', texto: e.message }))
      .finally(() => setCargando(false));
  }, [sesion, onMensaje]);

  // Verificar que el usuario esté autenticado y tenga rol de oferente
  if (!sesion || sesion.rol !== 'OFERENTE') {
    return (
      <section className="container py-5">
        <div className="alert alert-warning">Acceso restringido a oferentes autorizados.</div>
        <button className="btn btn-outline-secondary" onClick={() => onNavegar('/')}>Volver</button>
      </section>
    );
  }

  // Navegar a una subcategoría (hacia adentro del árbol)
  const entrar = async (sub: Caracteristica) => {
    if (navegando) return;
    setNavegando(true);
    setActual(sub);
    setRuta((prev) => [...prev, sub]);
    setSelId(null);
    await cargarSubcategorias(sub.id);
    setNavegando(false);
  };

  // Volver al nivel raíz del árbol de categorías
  const irARaiz = async () => {
    setActual(null);
    setRuta([]);
    setSelId(null);
    await cargarSubcategorias(null);
  };

  // Navegar a un nodo específico del breadcrumb
  const irANodo = async (nodo: Caracteristica, idx: number) => {
    const nuevaRuta = ruta.slice(0, idx + 1);
    setRuta(nuevaRuta);
    setActual(nodo);
    setSelId(null);
    await cargarSubcategorias(nodo.id);
  };

  // Seleccionar una característica hoja para agregar como habilidad
  const seleccionar = (sub: Caracteristica) => setSelId(sub.id);

  // Agregar una nueva habilidad al oferente
  const agregar = async (e: React.FormEvent) => {
    e.preventDefault();
    const id = selId || actual?.id;
    if (!id) return;
    try {
      await api.agregarHabilidad({ caracteristicaId: Number(id), nivel: Number(nivel) });
      onMensaje({ tipo: 'success', texto: 'Habilidad agregada.' });
      setSelId(null);
      setNivel(1);
      cargarHabilidades();
    } catch (e) {
      onMensaje({ tipo: 'danger', texto: (e as Error).message });
    }
  };

  // Eliminar una habilidad del oferente (con confirmación)
  const eliminar = async (id: number) => {
    if (!window.confirm('¿Eliminar esta habilidad?')) return;
    try {
      await api.eliminarHabilidad(id);
      onMensaje({ tipo: 'success', texto: 'Habilidad eliminada.' });
      cargarHabilidades();
    } catch (e) {
      onMensaje({ tipo: 'danger', texto: (e as Error).message });
    }
  };

  // Verificar si una característica es hoja (no tiene subcategorías)
  const esHoja = (sub: Caracteristica) => hojas.has(sub.id);

  return (
    <section className="container py-5">
      <SectionTitle eyebrow="Oferente" title="Mis habilidades" />

      {cargando ? <LoadingBlock /> : (
        <div className="row g-4">
          {/* Columna izquierda: tabla de habilidades ya registradas */}
          <div className="col-md-4">
            <table className="table table-hover table-sm">
              <thead>
                <tr><th>Característica</th><th>Nivel</th><th></th></tr>
              </thead>
              <tbody>
                {habilidades.length === 0 ? (
                  <tr><td colSpan={3} className="text-muted text-center">Sin habilidades.</td></tr>
                ) : habilidades.map((h) => (
                  <tr key={h.id}>
                    <td>{h.caracteristica?.nombre}</td>
                    <td>{h.nivel}</td>
                    <td>
                      <button className="btn btn-sm btn-outline-danger" onClick={() => eliminar(h.id)}>Quitar</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Columna central: navegación del árbol de categorías */}
          <div className="col-md-4">
            <div className="mb-2">
              {/* Breadcrumb de navegación entre categorías */}
              <span className="fw-semibold">Ruta:</span>
              <span className="ms-1">
                <button className="badge text-bg-secondary text-decoration-none border-0" onClick={irARaiz}>Raíces</button>
                {ruta.map((nodo, idx) => (
                  <span key={nodo.id}>
                    <span className="text-muted mx-1">/</span>
                    <button className="badge text-bg-secondary text-decoration-none border-0" onClick={() => irANodo(nodo, idx)}>
                      {nodo.nombre}
                    </button>
                  </span>
                ))}
              </span>
            </div>
            {actual && (
              <div className="mb-2 text-muted" style={{ fontSize: '0.9rem' }}>
                Subcategorías de: <strong>{actual.nombre}</strong>
              </div>
            )}
            {/* Lista de subcategorías con botón seleccionar/entrar */}
            {subcategorias.map((sub) => (
              <div key={sub.id} className="d-flex justify-content-between align-items-center border rounded px-3 py-2 mb-2 bg-white">
                <span>{sub.nombre}</span>
                {esHoja(sub) ? (
                  <button className={`btn btn-sm ${selId === sub.id ? 'btn-dark' : 'btn-outline-dark'}`} onClick={() => seleccionar(sub)}>
                    {selId === sub.id ? 'Seleccionado' : 'Selec.'}
                  </button>
                ) : (
                  <button className="btn btn-outline-secondary btn-sm" disabled={navegando} onClick={() => entrar(sub)}>Entrar</button>
                )}
              </div>
            ))}
            {subcategorias.length === 0 && (
              <div className="text-muted" style={{ fontSize: '0.9rem' }}>No hay subcategorías.</div>
            )}
          </div>

          {/* Columna derecha: formulario para agregar una nueva habilidad */}
          <div className="col-md-4">
            <div className="border rounded p-3 bg-white">
              <h6 className="fw-bold mb-3">Agregar Habilidad</h6>
              <form onSubmit={agregar}>
                <div className="mb-3">
                  <label className="form-label">Característica</label>
                  <select className="form-select" value={selId || ''} onChange={(e) => setSelId(Number(e.target.value))} required>
                    <option value="" disabled>Seleccione...</option>
                    {subcategorias.filter(esHoja).map((sub) => (
                      <option key={sub.id} value={sub.id}>{sub.nombre}</option>
                    ))}
                  </select>
                </div>
                <div className="mb-3">
                  <label className="form-label">Nivel (1-5)</label>
                  <input type="number" className="form-control" min={1} max={5} value={nivel}
                    onChange={(e) => setNivel(Number(e.target.value))} required />
                </div>
                <button type="submit" className="btn btn-primary w-100">Agregar</button>
              </form>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}

// Exportar el componente para usarlo en el enrutador
export default OferenteHabilidadesPage;
