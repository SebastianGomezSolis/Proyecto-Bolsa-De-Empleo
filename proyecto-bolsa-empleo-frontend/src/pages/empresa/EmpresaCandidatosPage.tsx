import { useEffect, useState } from 'react';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';
import { api } from '../../services/api';
import { Sesion, MensajeGlobal, Puesto, Candidato } from '../../types';

interface Props {
  sesion: Sesion | null;
  onNavegar: (ruta: string) => void;
  onMensaje: (m: MensajeGlobal) => void;
  puestoId?: number;
}

function EmpresaCandidatosPage({ sesion, onNavegar, onMensaje, puestoId }: Props) {
  const [candidatos, setCandidatos] = useState<Candidato[]>([]);
  const [puesto, setPuesto] = useState<Puesto | null>(null);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    if (!sesion || sesion.rol !== 'EMPRESA' || !puestoId) return;
    const cargar = async () => {
      try {
        const [puestos, cands] = await Promise.all([
          api.getPuestosEmpresa(),
          api.getCandidatosPuesto(puestoId),
        ]);
        setPuesto(puestos.find((p) => p.id === Number(puestoId)) || null);
        const lista = Array.isArray(cands) ? cands : (cands as any)?.candidatos ?? [];
        setCandidatos(lista);
      } catch (e) {
        onMensaje({ tipo: 'danger', texto: (e as Error).message });
      } finally {
        setCargando(false);
      }
    };
    cargar();
  }, [sesion, puestoId, onMensaje]);

  if (!sesion || sesion.rol !== 'EMPRESA') {
    return (
      <section className="container py-5">
        <div className="alert alert-warning">Acceso restringido a empresas autorizadas.</div>
        <button className="btn btn-outline-secondary" onClick={() => onNavegar('/')}>Volver</button>
      </section>
    );
  }

  return (
    <section className="container py-5">
      <SectionTitle eyebrow="Empresa" title="Candidatos para el puesto" />
      {puesto && <p className="mb-4"><strong>Puesto:</strong> {puesto.descripcion}</p>}

      {cargando ? <LoadingBlock /> : (
        <>
          <table className="table table-hover">
            <thead>
              <tr>
                <th>Oferente</th>
                <th>Requisitos cumplidos</th>
                <th>% Coincidencia</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {candidatos.length === 0 ? (
                <tr><td colSpan={4} className="text-center text-muted">No se encontraron candidatos.</td></tr>
              ) : candidatos.map((c) => (
                <tr key={c.oferente?.id}>
                  <td>{c.oferente?.nombre} {c.oferente?.primerApellido}</td>
                  <td>{c.requisitosCumplidos} / {c.totalRequisitos}</td>
                  <td>{Number(c.porcentaje).toFixed(2)}%</td>
                  <td>
                    <button className="btn btn-outline-dark btn-sm"
                      onClick={() => onNavegar(`/empresa/candidatos/${c.oferente?.id}?puestoId=${puestoId}`)}>
                      Ver detalle
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <button className="btn btn-dark btn-sm" onClick={() => onNavegar('/empresa/puestos')}>Volver</button>
        </>
      )}
    </section>
  );
}

export default EmpresaCandidatosPage;
