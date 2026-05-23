import { useNavigate } from 'react-router-dom';
import SectionTitle from '../../components/SectionTitle';

function DashboardOferentePage() {
  const navigate = useNavigate();

  return (
    <section className="container py-5 d-flex align-items-center justify-content-center" style={{ minHeight: '50vh' }}>
      <div className="text-center">
        <SectionTitle eyebrow="Oferente" title="Dashboard" description="Administrá tus habilidades, tu CV y buscá puestos." />
        <div className="d-flex gap-2 justify-content-center flex-wrap mt-3">
          <button className="btn btn-dark" onClick={() => navigate('/oferente/habilidades')}>Mis habilidades</button>
          <button className="btn btn-dark" onClick={() => navigate('/oferente/cv')}>Mi CV</button>
          <button className="btn btn-dark" onClick={() => navigate('/oferente/buscar')}>Buscar puesto</button>
        </div>
      </div>
    </section>
  );
}

export default DashboardOferentePage;
