import { useEffect, useState } from 'react';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';
import SelectorCaracteristicas from '../../components/SelectorCaracteristicas';
import ResultadosPuesto from '../../components/ResultadosPuesto';
import { api } from '../../services/api';
import { Sesion, MensajeGlobal, Caracteristica, Puesto, PuestosResponse } from '../../types';

interface Props {
  sesion: Sesion | null;
  onNavegar: (ruta: string) => void;
  onMensaje: (m: MensajeGlobal) => void;
}

function OferenteBuscarPuestoPage({ sesion, onNavegar, onMensaje }: Props) {
  const [raices, setRaices] = useState<Caracteristica[]>([]);
  const [seleccionados, setSeleccionados] = useState<number[]>([]);
  const [puestos, setPuestos] = useState<Puesto[] | null>(null);
  const [cargando, setCargando] = useState(true);
  const [buscando, setBuscando] = useState(false);

  useEffect(() => {
    if (!sesion || sesion.rol !== 'OFERENTE') return;
    const cargar = async () => {
      try {
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

  if (!sesion || sesion.rol !== 'OFERENTE') {
    return (
      <section className="container py-5">
        <div className="alert alert-warning">Acceso restringido a oferentes autorizados.</div>
        <button className="btn btn-outline-secondary" onClick={() => onNavegar('/')}>Volver</button>
      </section>
    );
  }

  const toggleSeleccion = (id: number) =>
    setSeleccionados((prev) => prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]);

  const buscar = async (e: React.FormEvent) => {
    e.preventDefault();
    setBuscando(true);
    try {
      const res = await api.buscarPuestosOferente(seleccionados);
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
      <SectionTitle eyebrow="Oferente" title="Buscar puestos" description="Filtrá por características para encontrar puestos que se ajusten a tu perfil. Como oferente registrado también ves puestos privados." />

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

export default OferenteBuscarPuestoPage;
