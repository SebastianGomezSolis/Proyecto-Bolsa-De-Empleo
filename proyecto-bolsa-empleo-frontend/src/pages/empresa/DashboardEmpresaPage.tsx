import SectionTitle from '../../components/SectionTitle';
import { Sesion } from '../../types';

interface Props {
  sesion: Sesion | null;
  onNavegar: (ruta: string) => void;
}

function DashboardEmpresaPage({ sesion, onNavegar }: Props) {
  if (!sesion || sesion.rol !== 'EMPRESA') {
    return (
      <section className="container py-5">
        <div className="alert alert-warning">Acceso restringido a empresas autorizadas.</div>
        <button className="btn btn-outline-secondary" onClick={() => onNavegar('/')}>Volver</button>
      </section>
    );
  }

  return (
    <section className="container py-5 d-flex align-items-center justify-content-center" style={{ minHeight: '50vh' }}>
      <div className="text-center">
        <SectionTitle eyebrow="Empresa" title="Dashboard" description="Desde aquí podés administrar tus puestos y buscar candidatos." />
        <div className="d-flex gap-2 justify-content-center flex-wrap mt-3">
          <button className="btn btn-dark" onClick={() => onNavegar('/empresa/puestos')}>Ver mis puestos</button>
          <button className="btn btn-dark" onClick={() => onNavegar('/empresa/publicar')}>Publicar nuevo puesto</button>
        </div>
      </div>
    </section>
  );
}

export default DashboardEmpresaPage;
