import SectionTitle from '../../components/SectionTitle';
import { Sesion } from '../../types';

interface Props {
  // Sesión actual del usuario (se usa para verificar rol de oferente)
  sesion: Sesion | null;
  // Función de navegación para redirigir al usuario a otras páginas
  onNavegar: (ruta: string) => void;
}

// Componente principal del dashboard de oferente
function DashboardOferentePage({ sesion, onNavegar }: Props) {
  // Verificar que el usuario esté autenticado y tenga rol de oferente
  if (!sesion || sesion.rol !== 'OFERENTE') {
    return (
      <section className="container py-5">
        <div className="alert alert-warning">Acceso restringido a oferentes autorizados.</div>
        <button className="btn btn-outline-secondary" onClick={() => onNavegar('/')}>Volver</button>
      </section>
    );
  }

  return (
    // Contenedor principal centrado vertical y horizontalmente
    <section className="container py-5 d-flex align-items-center justify-content-center" style={{ minHeight: '50vh' }}>
      <div className="text-center">
        <SectionTitle eyebrow="Oferente" title="Dashboard" description="Administrá tus habilidades, tu CV y buscá puestos." />
        {/* Botones de acceso rápido a las secciones del oferente */}
        <div className="d-flex gap-2 justify-content-center flex-wrap mt-3">
          <button className="btn btn-dark" onClick={() => onNavegar('/oferente/habilidades')}>Mis habilidades</button>
          <button className="btn btn-dark" onClick={() => onNavegar('/oferente/cv')}>Mi CV</button>
          <button className="btn btn-dark" onClick={() => onNavegar('/oferente/buscar')}>Buscar puesto</button>
        </div>
      </div>
    </section>
  );
}

// Exportar el componente para usarlo en el enrutador
export default DashboardOferentePage;
