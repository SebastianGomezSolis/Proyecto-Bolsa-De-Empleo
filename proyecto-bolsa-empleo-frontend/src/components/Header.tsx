import { useNavigate, useLocation } from 'react-router-dom';

interface Sesion {
  id: number;
  correo: string;
  rol: string;
  referenciaId: number;
  token: string;
}

function limpiarSesion() {
  localStorage.removeItem("token");
}

interface NavItemProps {
  label: string;
  href: string;
  active?: boolean;
}

function NavItem({ label, href, active = false }: NavItemProps) {
  const navigate = useNavigate();
  return (
    <li className="nav-item">
      <a className={`nav-link${active ? ' active' : ''}`} href={href}
         onClick={(e) => { e.preventDefault(); navigate(href); }}
         style={{ fontSize: '0.9rem', cursor: 'pointer' }}>{label}</a>
    </li>
  );
}

interface Props {
  sesion: Sesion | null;
}

function Header({ sesion }: Props) {
  const navigate = useNavigate();
  const location = useLocation();
  const ruta = location.pathname;
  const esAdmin = sesion?.rol === 'ADMIN';
  const esEmpresa = sesion?.rol === 'EMPRESA';
  const esOferente = sesion?.rol === 'OFERENTE';
  const logueado = Boolean(sesion);

  return (
    <nav className="navbar navbar-dark bg-dark">
      <div className="container-fluid p-0">
        <div className="d-flex align-items-center w-100">
          <a className="navbar-brand mb-0 h1 px-2 d-flex align-items-center" href="/"
             onClick={(e) => {
               e.preventDefault();
               const destinos: Record<string, string> = { ADMIN: '/admin/dashboard', EMPRESA: '/empresa/dashboard', OFERENTE: '/oferente/dashboard' };
               navigate(sesion ? destinos[sesion.rol] || '/' : '/');
             }}
             style={{ fontSize: '1.1rem', cursor: 'pointer' }}>
            <img src="/images/bolsaEmpleo.png" alt="Logo" style={{ height: '28px', width: 'auto', marginRight: '8px' }}
              onError={(e) => { (e.target as HTMLImageElement).style.display = 'none' }} />
            <span>Bolsa de Empleo</span>
          </a>

          <ul className="navbar-nav flex-row flex-wrap" style={{ gap: '1.5rem', marginLeft: '1rem', width: '100%' }}>
            {!logueado && (
              <>
                <NavItem label="Buscar puestos" href="/puestos/buscar" active={ruta === '/puestos/buscar'} />
                <NavItem label="Registro Empresa" href="/registro/empresa" active={ruta === '/registro/empresa'} />
                <NavItem label="Registro Oferente" href="/registro/oferente" active={ruta === '/registro/oferente'} />
              </>
            )}
            {esEmpresa && (
              <>
                <NavItem label="Dashboard" href="/empresa/dashboard" active={ruta === '/empresa/dashboard'} />
                <NavItem label="Mis puestos" href="/empresa/puestos" active={ruta === '/empresa/puestos'} />
                <NavItem label="Publicar puesto" href="/empresa/publicar" active={ruta === '/empresa/publicar'} />
              </>
            )}
            {esOferente && (
              <>
                <NavItem label="Dashboard" href="/oferente/dashboard" active={ruta === '/oferente/dashboard'} />
                <NavItem label="Mis habilidades" href="/oferente/habilidades" active={ruta === '/oferente/habilidades'} />
                <NavItem label="Mi CV" href="/oferente/cv" active={ruta === '/oferente/cv'} />
                <NavItem label="Buscar puesto" href="/oferente/buscar" active={ruta === '/oferente/buscar'} />
              </>
            )}
            {esAdmin && (
              <>
                <NavItem label="Dashboard" href="/admin/dashboard" active={ruta === '/admin/dashboard'} />
                <NavItem label="Empresas pendientes" href="/admin/pendientes" active={ruta === '/admin/pendientes'} />
                <NavItem label="Oferentes pendientes" href="/admin/oferentes-pendientes" active={ruta === '/admin/oferentes-pendientes'} />
                <NavItem label="Características" href="/admin/caracteristicas" active={ruta === '/admin/caracteristicas'} />
                <NavItem label="Reportes" href="/admin/reportes" active={ruta === '/admin/reportes'} />
              </>
            )}
            <li className="nav-item ms-auto d-flex align-items-center me-2">
              {logueado ? (
                <>
                  <span className="text-white me-3" style={{ fontSize: '0.9rem' }}>{sesion!.correo}</span>
                  <button className="btn btn-outline-light btn-sm" onClick={() => { limpiarSesion(); navigate('/'); }}>Salir</button>
                </>
              ) : (
                <a className={`nav-link${ruta === '/login' ? ' active' : ''}`} href="/login"
                   onClick={(e) => { e.preventDefault(); navigate('/login'); }}
                   style={{ fontSize: '0.9rem', cursor: 'pointer' }}>Login</a>
              )}
            </li>
          </ul>
        </div>
      </div>
    </nav>
  );
}

export default Header;
