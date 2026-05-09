import { useEffect, useState } from 'react';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';
import { api } from '../../services/api';
import { Sesion, MensajeGlobal, OferentePerfil, Habilidad } from '../../types';

interface Props {
  sesion: Sesion | null;
  onNavegar: (ruta: string) => void;
  onMensaje: (m: MensajeGlobal) => void;
  id?: number;
  puestoId?: number;
}

function EmpresaDetalleCandidatoPage({ sesion, onNavegar, onMensaje, id, puestoId }: Props) {
  const [oferente, setOferente] = useState<OferentePerfil | null>(null);
  const [habilidades, setHabilidades] = useState<Habilidad[]>([]);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    if (!sesion || sesion.rol !== 'EMPRESA' || !id) return;
    if (!puestoId) return;
    api.getDetalleOferente(id, puestoId)
      .then((data) => { setOferente(data.oferente); setHabilidades(data.habilidades || []); })
      .catch((e: Error) => onMensaje({ tipo: 'danger', texto: e.message }))
      .finally(() => setCargando(false));
  }, [sesion, id, puestoId, onMensaje]);

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
      <SectionTitle eyebrow="Empresa" title="Detalle de oferente" />

      {cargando ? <LoadingBlock /> : (
        <>
          {oferente && (
            <div className="border rounded p-3 mb-4">
              <h5>{oferente.nombre} {oferente.primerApellido}</h5>
              <p className="mb-1"><strong>Identificación:</strong> {oferente.identificacion}</p>
              <p className="mb-1"><strong>Email:</strong> {oferente.usuario?.correo}</p>
              <p className="mb-1"><strong>Teléfono:</strong> {oferente.telefono}</p>
              <p className="mb-1"><strong>Residencia:</strong> {oferente.lugarResidencia}</p>
              {oferente.curriculum && (
                <div className="mt-2">
                  <a href={`/${oferente.curriculum}`}
                    target="_blank" rel="noreferrer" className="btn btn-dark btn-sm">Ver CV</a>
                </div>
              )}
            </div>
          )}

          <h5>Habilidades</h5>
          <table className="table table-hover">
            <thead>
              <tr><th>Característica</th><th>Nivel</th></tr>
            </thead>
            <tbody>
              {habilidades.length === 0 ? (
                <tr><td colSpan={2} className="text-muted">No tiene habilidades registradas.</td></tr>
              ) : habilidades.map((h) => (
                <tr key={h.id}>
                  <td>{h.caracteristica?.nombre}</td>
                  <td>{h.nivel}</td>
                </tr>
              ))}
            </tbody>
          </table>

          <button className="btn btn-dark btn-sm"
            onClick={() => onNavegar(puestoId ? `/empresa/puestos/${puestoId}/candidatos` : '/empresa/puestos')}>
            Volver
          </button>
        </>
      )}
    </section>
  );
}

export default EmpresaDetalleCandidatoPage;
