import { useEffect, useState } from 'react';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';
import CardPuesto from '../../components/CardPuesto';
import { api } from '../../services/api';
import { Sesion, MensajeGlobal, Puesto, PuestosResponse } from '../../types';

interface Props {
  sesion: Sesion | null;
  onNavegar: (ruta: string) => void;
  onMensaje: (m: MensajeGlobal) => void;
}

function HomePage({ sesion, onNavegar, onMensaje }: Props) {
  const [puestos, setPuestos] = useState<Puesto[]>([]);
  const [cargando, setCargando] = useState(true);
  const logueado = Boolean(sesion);

  useEffect(() => {
    api.getUltimosPuestosPublicos()
      .then((res: PuestosResponse) => {
        setPuestos((res.puestos ?? []).map((p: Puesto) => ({ ...p, tipoCambio: res.tipoCambio })));
      })
      .catch((e: Error) => onMensaje({ tipo: 'danger', texto: e.message }))
      .finally(() => setCargando(false));
  }, [onMensaje]);

  return (
    <>
      <section className="container py-5">
        <div className="row g-4">
          <div className="col-md-4">
            <div className="card shadow-sm border-0 h-100 feature-card">
              <div className="card-body text-center py-4">
                <h3 className="h5 fw-bold mt-3">Empresas</h3>
                <p className="text-secondary mb-3">Registrá tu empresa y publicá puestos para encontrar el talento que necesitás.</p>
                {!logueado && <button className="btn btn-outline-primary" onClick={() => onNavegar('/registro/empresa')}>Registrar empresa</button>}
                {logueado && <button className="btn btn-outline-secondary" disabled>Registrar empresa</button>}
              </div>
            </div>
          </div>
          <div className="col-md-4">
            <div className="card shadow-sm border-0 h-100 feature-card">
              <div className="card-body text-center py-4">
                <h3 className="h5 fw-bold mt-3">Oferentes</h3>
                <p className="text-secondary mb-3">Creá tu perfil, agregá tus habilidades y explorá las oportunidades disponibles.</p>
                {!logueado && <button className="btn btn-outline-primary" onClick={() => onNavegar('/registro/oferente')}>Registrar oferente</button>}
                {logueado && <button className="btn btn-outline-secondary" disabled>Registrar oferente</button>}
              </div>
            </div>
          </div>
          <div className="col-md-4">
            <div className="card shadow-sm border-0 h-100 feature-card">
              <div className="card-body text-center py-4">
                <h3 className="h5 fw-bold mt-3">Puestos</h3>
                <p className="text-secondary mb-3">Explorá todas las ofertas laborales activas publicadas por empresas registradas.</p>
                {!logueado && <button className="btn btn-outline-primary" onClick={() => onNavegar('/puestos/buscar')}>Ver puestos</button>}
                {logueado && <button className="btn btn-outline-secondary" disabled>Ver puestos</button>}
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="container pb-5">
        <SectionTitle
          eyebrow="Últimas ofertas"
          title="Puestos recientes"
          description="Un vistazo a las últimas oportunidades laborales publicadas en el sistema."
        />
        {cargando ? <LoadingBlock /> : (
          <div className="row g-3">
            {puestos.length === 0 ? (
              <div className="col-12"><p className="text-secondary">No hay puestos publicados aún.</p></div>
            ) : puestos.map((p) => (
              <div key={p.id} className="col-md-6 col-lg-4">
                <CardPuesto puesto={p} />
              </div>
            ))}
          </div>
        )}
        {!cargando && puestos.length > 0 && !logueado && (
          <div className="mt-4">
            <button className="btn btn-outline-primary" onClick={() => onNavegar('/puestos/buscar')}>Ver todos los puestos</button>
          </div>
        )}
      </section>
    </>
  );
}

export default HomePage;
