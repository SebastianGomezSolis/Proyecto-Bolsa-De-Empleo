import { useEffect, useState } from 'react';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';
import SelectorCaracteristicas from '../../components/SelectorCaracteristicas';
import ResultadosPuesto from '../../components/ResultadosPuesto';
import { api } from '../../services/api';
import { MensajeGlobal, Caracteristica, Puesto, PuestosResponse } from '../../types';

async function cargarArbolCompleto(padreId: number | null = null): Promise<Caracteristica[]> {
  const nodos = padreId ? await api.getCaracteristicasHijos(padreId) : await api.getCaracteristicasRaiz();
  return Promise.all(
    nodos.map(async (n) => {
      const hijos = await cargarArbolCompleto(n.id);
      return { ...n, hijos };
    })
  );
}

interface Props {
  onMensaje: (m: MensajeGlobal) => void;
}

function BuscarPuestoPublicoPage({ onMensaje }: Props) {
  const [raices, setRaices] = useState<Caracteristica[]>([]);
  const [seleccionados, setSeleccionados] = useState<number[]>([]);
  const [puestos, setPuestos] = useState<Puesto[] | null>(null);
  const [cargando, setCargando] = useState(true);
  const [buscando, setBuscando] = useState(false);

  useEffect(() => {
    cargarArbolCompleto(null)
      .then(setRaices)
      .catch((e: Error) => onMensaje({ tipo: 'danger', texto: e.message }))
      .finally(() => setCargando(false));
  }, [onMensaje]);

  const toggleSeleccion = (id: number) =>
    setSeleccionados((prev) => prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]);

  const buscar = async (e: React.FormEvent) => {
    e.preventDefault();
    setBuscando(true);
    try {
      const res = await api.buscarPuestosPublicos(seleccionados);
      setPuestos((res.puestos ?? []).map((p: Puesto) => ({ ...p, tipoCambio: res.tipoCambio })));
    } catch (e) {
      onMensaje({ tipo: 'danger', texto: (e as Error).message });
    } finally {
      setBuscando(false);
    }
  };

  const limpiar = () => { setSeleccionados([]); setPuestos(null); };

  return (
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
