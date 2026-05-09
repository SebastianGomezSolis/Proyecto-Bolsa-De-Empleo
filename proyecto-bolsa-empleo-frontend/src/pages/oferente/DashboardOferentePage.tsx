import SectionTitle from '../../components/SectionTitle';
import { Sesion } from '../../types';

interface Props {
  sesion: Sesion | null;
  onNavegar: (ruta: string) => void;
}

function DashboardOferentePage({ sesion, onNavegar }: Props) {
  if (!sesion || sesion.rol !== 'OFERENTE') {
    return (
      <section className="container py-5">
        <div className="alert alert-warning">Acceso restringido a oferentes autorizados.</div>
        <button className="btn btn-outline-secondary" onClick={() => onNavegar('/')}>Volver</button>
      </section>
    );
  }

  return (
    <section className="container py-5 d-flex align-items-center justify-content-center" style={{ minHeight: '50vh' }}>
      <div className="text-center">
        <SectionTitle eyebrow="Oferente" title="Dashboard" description="Administrá tus habilidades, tu CV y buscá puestos." />
        <div className="d-flex gap-2 justify-content-center flex-wrap mt-3">
          <button className="btn btn-dark" onClick={() => onNavegar('/oferente/habilidades')}>Mis habilidades</button>
          <button className="btn btn-dark" onClick={() => onNavegar('/oferente/cv')}>Mi CV</button>
          <button className="btn btn-dark" onClick={() => onNavegar('/oferente/buscar')}>Buscar puesto</button>
        </div>
      </div>
    </section>
  );
}

export default DashboardOferentePage;
