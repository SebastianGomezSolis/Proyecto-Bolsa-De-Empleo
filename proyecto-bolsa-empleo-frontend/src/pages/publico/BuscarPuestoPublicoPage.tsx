// Página pública de búsqueda de puestos. Permite filtrar ofertas laborales
// mediante un árbol jerárquico de características (categorías).

import { useEffect, useState } from 'react';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';
import SelectorCaracteristicas from '../../components/SelectorCaracteristicas';
import ResultadosPuesto from '../../components/ResultadosPuesto';
import { BASE_API, getAuthHeaders } from '../../services/api';
import { MensajeGlobal, Caracteristica, Puesto } from '../../types';

// Función recursiva para cargar el árbol completo de características desde el backend
async function cargarArbolCompleto(padreId: number | null = null): Promise<Caracteristica[]> {
  const url = padreId ? `${BASE_API}/publico/caracteristicas?padreId=${padreId}` : `${BASE_API}/publico/caracteristicas`;
  const response = await fetch(url, { headers: getAuthHeaders() });
  if (!response.ok) throw new Error(await response.text());
  const nodos: Caracteristica[] = await response.json();
  return Promise.all(
    nodos.map(async (n) => {
      const hijos = await cargarArbolCompleto(n.id);
      return { ...n, hijos };
    })
  );
}

interface Props {
  // Función de callback para mostrar mensajes globales (éxito/error)
  onMensaje: (m: MensajeGlobal) => void;
}

function BuscarPuestoPublicoPage({ onMensaje }: Props) {
  // Estado para almacenar las raíces del árbol de características
  const [raices, setRaices] = useState<Caracteristica[]>([]);
  // Estado para almacenar los IDs de características seleccionadas como filtro
  const [seleccionados, setSeleccionados] = useState<number[]>([]);
  // Estado para almacenar los resultados de la búsqueda (null = aún no se buscó)
  const [puestos, setPuestos] = useState<Puesto[] | null>(null);
  // Estado para indicar si se está cargando el árbol de características
  const [cargando, setCargando] = useState(true);
  // Estado para indicar si se está ejecutando una búsqueda
  const [buscando, setBuscando] = useState(false);

  // Effect que carga el árbol completo de características al montar el componente
  useEffect(() => {
    cargarArbolCompleto(null)
      .then(setRaices)
      .catch((e: Error) => onMensaje({ tipo: 'danger', texto: e.message }))
      .finally(() => setCargando(false));
  }, [onMensaje]);

  // Alterna la selección de una característica (agregar o quitar del filtro)
  const toggleSeleccion = (id: number) =>
    setSeleccionados((prev) => prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]);

  // Manejador de la búsqueda: envía las características seleccionadas al backend
  const buscar = async (e: React.FormEvent) => {
    e.preventDefault();
    setBuscando(true);
    try {
      const params = seleccionados.length ? `?caracteristicas=${seleccionados.join(',')}` : '';
      const response = await fetch(`${BASE_API}/publico/puestos/buscar${params}`, { headers: getAuthHeaders() });
      if (!response.ok) throw new Error(await response.text());
      const res = await response.json();
      setPuestos((res.puestos ?? []).map((p: Puesto) => ({ ...p, tipoCambio: res.tipoCambio })));
    } catch (e) {
      onMensaje({ tipo: 'danger', texto: (e as Error).message });
    } finally {
      setBuscando(false);
    }
  };

  // Limpia los filtros seleccionados y los resultados de búsqueda
  const limpiar = () => { setSeleccionados([]); setPuestos(null); };

  return (
    // Render del formulario de búsqueda y los resultados
    <section className="container py-5">
      <SectionTitle eyebrow="Búsqueda" title="Buscar puestos" description="Filtrá por características para encontrar puestos disponibles." />

      {cargando ? <LoadingBlock /> : (
        <>
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

          {puestos !== null && (
            <>
              <h5 className="mb-3">Resultados ({puestos.length})</h5>
              <ResultadosPuesto puestos={puestos} vacio="No se encontraron puestos con las características seleccionadas." />
            </>
          )}
          {puestos === null && (
            <p className="text-muted">Seleccioná características y presioná <strong>Buscar</strong> para ver resultados.</p>
          )}
        </>
      )}
    </section>
  );
}

export default BuscarPuestoPublicoPage;
