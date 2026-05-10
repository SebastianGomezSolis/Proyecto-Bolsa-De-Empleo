import { Sesion } from '../types';

// Propiedades para el subcomponente NavItem: etiqueta, ruta de destino y si está activo
interface NavItemProps {
  label: string;            // Texto visible del enlace de navegación
  href: string;             // Ruta de destino (con #)
  active?: boolean;         // Indica si el enlace corresponde a la ruta actual
}

// Subcomponente que renderiza un elemento <li> con un enlace de navegación (<a>)
function NavItem({ label, href, active = false }: NavItemProps) {
  return (
    <li className="nav-item">
      <a className={`nav-link${active ? ' active' : ''}`} href={href} style={{ fontSize: '0.9rem' }}>{label}</a>
    </li>
  );
}

// Propiedades del componente Header: sesión actual, ruta activa, navegación y cierre de sesión
interface Props {
  sesion: Sesion | null;             // Datos de la sesión del usuario, null si no ha iniciado sesión
  ruta: string;                      // Ruta hash actual del navegador
  onNavegar: (ruta: string) => void; // Función para navegar a otra ruta
  onLogout: () => void;             // Función para cerrar la sesión
}

// Componente de la barra de navegación principal. Muestra enlaces según el rol del usuario
// e incluye el logo, los items de navegación y el área de login/logout.
function Header({ sesion, onLogout, ruta }: Props) {
  const esAdmin = sesion?.rol === 'ADMIN';       // Verifica si el usuario es administrador
  const esEmpresa = sesion?.rol === 'EMPRESA';   // Verifica si el usuario es empresa
  const esOferente = sesion?.rol === 'OFERENTE'; // Verifica si el usuario es oferente
  const logueado = Boolean(sesion);              // Indica si hay una sesión activa

  return (
    <nav className="navbar navbar-dark bg-dark">
      <div className="container-fluid p-0">
        <div className="d-flex align-items-center w-100">
          <a className="navbar-brand mb-0 h1 px-2 d-flex align-items-center" href="#/" style={{ fontSize: '1.1rem' }}>
            <img src="/images/bolsaEmpleo.png" alt="Logo" style={{ height: '28px', width: 'auto', marginRight: '8px' }}
              onError={(e) => { (e.target as HTMLImageElement).style.display = 'none' }} />
            <span>Bolsa de Empleo</span>
          </a>

          <ul className="navbar-nav flex-row flex-wrap" style={{ gap: '1.5rem', marginLeft: '1rem', width: '100%' }}>
            {/* Navegación para usuarios no autenticados: búsqueda pública y registro */}
            {!logueado && (
              <>
                <NavItem label="Buscar puestos" href="#/puestos/buscar" active={ruta === '/puestos/buscar'} />
                <NavItem label="Registro Empresa" href="#/registro/empresa" active={ruta === '/registro/empresa'} />
                <NavItem label="Registro Oferente" href="#/registro/oferente" active={ruta === '/registro/oferente'} />
              </>
            )}

            {/* Navegación para empresas: dashboard, puestos publicados y publicación */}
            {esEmpresa && (
              <>
                <NavItem label="Dashboard" href="#/empresa/dashboard" active={ruta === '/empresa/dashboard'} />
                <NavItem label="Mis puestos" href="#/empresa/puestos" active={ruta === '/empresa/puestos'} />
                <NavItem label="Publicar puesto" href="#/empresa/publicar" active={ruta === '/empresa/publicar'} />
              </>
            )}

            {/* Navegación para oferentes: dashboard, habilidades, CV y búsqueda de puestos */}
            {esOferente && (
              <>
                <NavItem label="Dashboard" href="#/oferente/dashboard" active={ruta === '/oferente/dashboard'} />
                <NavItem label="Mis habilidades" href="#/oferente/habilidades" active={ruta === '/oferente/habilidades'} />
                <NavItem label="Mi CV" href="#/oferente/cv" active={ruta === '/oferente/cv'} />
                <NavItem label="Buscar puesto" href="#/oferente/buscar" active={ruta === '/oferente/buscar'} />
              </>
            )}

            {/* Navegación para administradores: panel, aprobaciones, características y reportes */}
            {esAdmin && (
              <>
                <NavItem label="Dashboard" href="#/admin/dashboard" active={ruta === '/admin/dashboard'} />
                <NavItem label="Empresas pendientes" href="#/admin/pendientes" active={ruta === '/admin/pendientes'} />
                <NavItem label="Oferentes pendientes" href="#/admin/oferentes-pendientes" active={ruta === '/admin/oferentes-pendientes'} />
                <NavItem label="Características" href="#/admin/caracteristicas" active={ruta === '/admin/caracteristicas'} />
                <NavItem label="Reportes" href="#/admin/reportes" active={ruta === '/admin/reportes'} />
              </>
            )}

            {/* Área de login/logout: muestra el correo del usuario y botón Salir, o enlace Login */}
            <li className="nav-item ms-auto d-flex align-items-center me-2">
              {logueado ? (
                <>
                  <span className="text-white me-3" style={{ fontSize: '0.9rem' }}>{sesion!.correo}</span>
                  <button className="btn btn-outline-light btn-sm" onClick={onLogout}>Salir</button>
                </>
              ) : (
                <a className={`nav-link${ruta === '/login' ? ' active' : ''}`} href="#/login" style={{ fontSize: '0.9rem' }}>Login</a>
              )}
            </li>
          </ul>
        </div>
      </div>
    </nav>
  );
}

export default Header;
