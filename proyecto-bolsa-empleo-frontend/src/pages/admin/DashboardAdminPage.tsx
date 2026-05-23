import { useNavigate } from 'react-router-dom';
import SectionTitle from '../../components/SectionTitle';

function DashboardAdminPage() {
  const navigate = useNavigate();

  return (
    <section className="container py-5 d-flex align-items-center justify-content-center" style={{ minHeight: '50vh' }}>
      <div className="text-center">
        <SectionTitle eyebrow="Panel" title="Administrador" description="Aprobaciones, catálogo de características y reportes." />
        <div className="d-flex gap-2 justify-content-center flex-wrap mt-3">
          <button className="btn btn-dark" onClick={() => navigate('/admin/pendientes')}>Empresas pendientes</button>
          <button className="btn btn-dark" onClick={() => navigate('/admin/oferentes-pendientes')}>Oferentes pendientes</button>
          <button className="btn btn-dark" onClick={() => navigate('/admin/caracteristicas')}>Características</button>
          <button className="btn btn-dark" onClick={() => navigate('/admin/reportes')}>Reportes</button>
        </div>
      </div>
    </section>
  );
}

export default DashboardAdminPage;
