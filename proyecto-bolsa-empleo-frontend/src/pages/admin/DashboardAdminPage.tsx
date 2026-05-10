// Página principal del panel de administración.
// Muestra botones de acceso rápido a las secciones de gestión del sistema.

import SectionTitle from '../../components/SectionTitle';
import { Sesion } from '../../types';

interface Props {
  // Sesión actual del usuario (se usa para verificar rol de administrador)
  sesion: Sesion | null;
  // Función de navegación para redirigir al usuario a otras páginas
  onNavegar: (ruta: string) => void;
}

function DashboardAdminPage({ sesion, onNavegar }: Props) {
  // Redirección para usuarios no autenticados o que no son administradores
  if (!sesion || sesion.rol !== 'ADMIN') {
    return (
      <section className="container py-5">
        <div className="alert alert-danger">Acceso restringido a administradores.</div>
        <button className="btn btn-outline-secondary" onClick={() => onNavegar('/')}>Volver</button>
      </section>
    );
  }

  return (
    <section className="container py-5 d-flex align-items-center justify-content-center" style={{ minHeight: '50vh' }}>
      <div className="text-center">
        <SectionTitle eyebrow="Panel" title="Administrador" description="Aprobaciones, catálogo de características y reportes." />
        {/* Botones de acceso rápido a las secciones de administración */}
        <div className="d-flex gap-2 justify-content-center flex-wrap mt-3">
          <button className="btn btn-dark" onClick={() => onNavegar('/admin/pendientes')}>Empresas pendientes</button>
          <button className="btn btn-dark" onClick={() => onNavegar('/admin/oferentes-pendientes')}>Oferentes pendientes</button>
          <button className="btn btn-dark" onClick={() => onNavegar('/admin/caracteristicas')}>Características</button>
          <button className="btn btn-dark" onClick={() => onNavegar('/admin/reportes')}>Reportes</button>
        </div>
      </div>
    </section>
  );
}

export default DashboardAdminPage;
