import { useNavigate } from 'react-router-dom';
import SectionTitle from '../../components/SectionTitle';

function DashboardEmpresaPage() {
  const navigate = useNavigate();

  return (
    <section className="container py-5 d-flex align-items-center justify-content-center" style={{ minHeight: '50vh' }}>
      <div className="text-center">
        <SectionTitle eyebrow="Empresa" title="Dashboard" description="Desde aquí podés administrar tus puestos y buscar candidatos." />
        <div className="d-flex gap-2 justify-content-center flex-wrap mt-3">
          <button className="btn btn-dark" onClick={() => navigate('/empresa/puestos')}>Ver mis puestos</button>
          <button className="btn btn-dark" onClick={() => navigate('/empresa/publicar')}>Publicar nuevo puesto</button>
        </div>
      </div>
    </section>
  );
}

export default DashboardEmpresaPage;
