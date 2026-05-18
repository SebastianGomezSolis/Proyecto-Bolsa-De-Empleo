import { useEffect, useState } from 'react';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';
import SelectorCaracteristicas from '../../components/SelectorCaracteristicas';
import ResultadosPuesto from '../../components/ResultadosPuesto';
import { BASE_API, getAuthHeaders } from '../../services/api';
import { Sesion, MensajeGlobal, Caracteristica, Puesto } from '../../types';

interface Props {
  // Sesión actual del usuario (se usa para verificar rol de oferente)
  sesion: Sesion | null;
  // Función de navegación para redirigir al usuario a otras páginas
  onNavegar: (ruta: string) => void;
  // Función de callback para mostrar mensajes globales (éxito/error)
  onMensaje: (m: MensajeGlobal) => void;
}

// Componente principal para buscar puestos por características
function OferenteBuscarPuestoPage({ sesion, onNavegar, onMensaje }: Props) {
  // Estado con el árbol de características (raíces con hijos y nietos)
  const [raices, setRaices] = useState<Caracteristica[]>([]);
  // IDs de las características seleccionadas para filtrar
  const [seleccionados, setSeleccionados] = useState<number[]>([]);
  // Resultados de la búsqueda (null = no se ha buscado aún)
  const [puestos, setPuestos] = useState<Puesto[] | null>(null);
  // Indicador de carga inicial de las características
  const [cargando, setCargando] = useState(true);
  // Indicador de búsqueda en curso
  const [buscando, setBuscando] = useState(false);

  // Effect para cargar el árbol completo de características al montar el componente
  useEffect(() => {
    if (!sesion || sesion.rol !== 'OFERENTE') return;
    const cargar = async () => {
      try {
        // Obtener raíces y sus hijos y nietos para construir el árbol
        const raizRes = await fetch(`${BASE_API}/publico/caracteristicas`, { headers: getAuthHeaders() });
        if (!raizRes.ok) throw new Error(await raizRes.text());
        const raiz: Caracteristica[] = await raizRes.json();
        const conHijos = await Promise.all(
          raiz.map(async (r) => {
            const hijosRes = await fetch(`${BASE_API}/publico/caracteristicas?padreId=${r.id}`, { headers: getAuthHeaders() });
            if (!hijosRes.ok) throw new Error(await hijosRes.text());
            const hijos: Caracteristica[] = await hijosRes.json();
            const hijosConNietos = await Promise.all(
              hijos.map(async (h) => {
                const nietosRes = await fetch(`${BASE_API}/publico/caracteristicas?padreId=${h.id}`, { headers: getAuthHeaders() });
                if (!nietosRes.ok) throw new Error(await nietosRes.text());
                const nietos = await nietosRes.json();
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

  // Verificar que el usuario esté autenticado y tenga rol de oferente
  if (!sesion || sesion.rol !== 'OFERENTE') {
    return (
      <section className="container py-5">
        <div className="alert alert-warning">Acceso restringido a oferentes autorizados.</div>
        <button className="btn btn-outline-secondary" onClick={() => onNavegar('/')}>Volver</button>
      </section>
    );
  }

  // Alternar selección de una característica (agregar o quitar)
  const toggleSeleccion = (id: number) =>
    setSeleccionados((prev) => prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]);

  // Ejecutar la búsqueda de puestos con las características seleccionadas
  const buscar = async (e: React.FormEvent) => {
    e.preventDefault();
    setBuscando(true);
    try {
      const params = seleccionados.length > 0 ? `?caracteristicaIds=${seleccionados.join(',')}` : '';
      const buscarRes = await fetch(`${BASE_API}/oferente/puestos/buscar${params}`, { headers: getAuthHeaders() });
      if (!buscarRes.ok) throw new Error(await buscarRes.text());
      const res = await buscarRes.json();
      setPuestos((res.puestos ?? []).map((p: Puesto) => ({ ...p, tipoCambio: res.tipoCambio })));
    } catch (e) {
      onMensaje({ tipo: 'danger', texto: (e as Error).message });
    } finally {
      setBuscando(false);
    }
  };

  // Limpiar selección y resultados
  const limpiar = () => { setSeleccionados([]); setPuestos(null); };

  return (
    <section className="container py-5">
      <SectionTitle eyebrow="Oferente" title="Buscar puestos" description="Filtrá por características para encontrar puestos que se ajusten a tu perfil. Como oferente registrado también ves puestos privados." />

      {cargando ? <LoadingBlock /> : (
        <>
          {/* Formulario de búsqueda con selector de características */}
          <form onSubmit={buscar}>
            <div className="border rounded p-3 bg-white mb-4">
              <h6 className="fw-bold mb-3">Buscar puestos por características</h6>
              <SelectorCaracteristicas raices={raices} seleccionados={seleccionados} onToggle={toggleSeleccion} />
              <div className="d-flex gap-2 mt-2">
                <button type="submit" className="btn btn-dark btn-sm" disabled={buscando}>
                  {buscando ? 'Buscando...' : 'Buscar'}
                </button>
                <button type="button" className="btn btn-outline-secondary btn-sm" onClick={limpiar}>Limpiar</button>
              </div>
            </div>
          </form>

          {/* Mostrar resultados de la búsqueda cuando existen */}
          {puestos !== null && (
            <>
              <h5 className="mb-3">Resultados ({puestos.length})</h5>
              <ResultadosPuesto puestos={puestos} vacio="No se encontraron puestos con las características seleccionadas." />
            </>
          )}
          {/* Mensaje informativo cuando aún no se ha realizado ninguna búsqueda */}
          {puestos === null && (
            <p className="text-muted">Seleccioná características y presioná <strong>Buscar</strong> para ver resultados.</p>
          )}
        </>
      )}
    </section>
  );
}

// Exportar el componente para usarlo en el enrutador
export default OferenteBuscarPuestoPage;
