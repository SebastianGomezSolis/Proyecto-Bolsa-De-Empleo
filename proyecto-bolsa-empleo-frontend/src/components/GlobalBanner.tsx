// Componente del banner global que se muestra en la parte superior de ciertas páginas.
// Muestra un mensaje promocional o informativo específico según la ruta actual,
// con un título, descripción y botón de acción contextual.

interface BannerConfig {
  // Texto pequeño que aparece arriba del título (ej: "Bolsa de Empleo")
  eyebrow: string;
  // Título principal del banner
  title: string;
  // Descripción o subtítulo explicativo
  description: string;
  // Texto del botón de acción
  buttonLabel: string;
  // Ruta a la que dirige el botón al hacer clic
  buttonRoute: string;
}

// Configuración de banners para cada ruta específica del aplicativo.
// Cada entrada define el contenido que se mostrará en ese camino.
const bannerConfig: Record<string, BannerConfig> = {
  '/': {
    eyebrow: 'Bolsa de Empleo',
    title: 'Conectamos talento con oportunidades.',
    description: 'Explorá puestos disponibles, registrate como empresa u oferente y gestioná tu perfil desde el panel correspondiente.',
    buttonLabel: 'Ver puestos disponibles',
    buttonRoute: '/puestos/buscar',
  },
  '/puestos/buscar': {
    eyebrow: 'Puestos',
    title: 'Buscá tu próximo empleo.',
    description: 'Filtrá por características para encontrar los puestos que mejor se adapten a tu perfil.',
    buttonLabel: 'Registrarse como oferente',
    buttonRoute: '/registro/oferente',
  },
  '/login': {
    eyebrow: 'Acceso',
    title: 'Ingresá al sistema.',
    description: 'Autenticáte para gestionar puestos, habilidades o revisar solicitudes pendientes.',
    buttonLabel: 'Volver al inicio',
    buttonRoute: '/',
  },
  '/registro/empresa': {
    eyebrow: 'Registro',
    title: 'Registrá tu empresa.',
    description: 'Creá tu cuenta de empresa para publicar puestos y encontrar el talento que necesitás.',
    buttonLabel: 'Ya tengo cuenta',
    buttonRoute: '/login',
  },
  '/registro/oferente': {
    eyebrow: 'Registro',
    title: 'Registrate como oferente.',
    description: 'Creá tu perfil, agregá tus habilidades y explorá las oportunidades disponibles.',
    buttonLabel: 'Ya tengo cuenta',
    buttonRoute: '/login',
  },
  '/empresa/dashboard': {
    eyebrow: 'Empresa',
    title: 'Panel de empresa.',
    description: 'Administrá tus puestos y buscá candidatos desde aquí.',
    buttonLabel: 'Ver mis puestos',
    buttonRoute: '/empresa/puestos',
  },
  '/empresa/puestos': {
    eyebrow: 'Empresa',
    title: 'Mis puestos publicados.',
    description: 'Administrá los puestos que has publicado y gestioná su estado.',
    buttonLabel: 'Publicar nuevo puesto',
    buttonRoute: '/empresa/publicar',
  },
  '/empresa/publicar': {
    eyebrow: 'Empresa',
    title: 'Publicar nuevo puesto.',
    description: 'Completá el formulario para publicar una nueva oferta laboral.',
    buttonLabel: 'Ver mis puestos',
    buttonRoute: '/empresa/puestos',
  },
  '/oferente/dashboard': {
    eyebrow: 'Oferente',
    title: 'Panel de oferente.',
    description: 'Administrá tus habilidades, tu CV y buscá puestos.',
    buttonLabel: 'Buscar puestos',
    buttonRoute: '/oferente/buscar',
  },
  '/oferente/habilidades': {
    eyebrow: 'Oferente',
    title: 'Mis habilidades.',
    description: 'Gestioná las habilidades de tu perfil para que las empresas puedan encontrarte.',
    buttonLabel: 'Buscar puestos',
    buttonRoute: '/oferente/buscar',
  },
  '/oferente/cv': {
    eyebrow: 'Oferente',
    title: 'Mi CV.',
    description: 'Subí o actualizá tu currículum para que las empresas puedan revisarlo.',
    buttonLabel: 'Mis habilidades',
    buttonRoute: '/oferente/habilidades',
  },
  '/oferente/buscar': {
    eyebrow: 'Oferente',
    title: 'Buscar puestos.',
    description: 'Filtrá por características para encontrar puestos que se ajusten a tu perfil.',
    buttonLabel: 'Mis habilidades',
    buttonRoute: '/oferente/habilidades',
  },
  '/admin/dashboard': {
    eyebrow: 'Administración',
    title: 'Panel de administrador.',
    description: 'Aprobaciones, catálogo de características y reportes.',
    buttonLabel: 'Ver pendientes',
    buttonRoute: '/admin/pendientes',
  },
  '/admin/pendientes': {
    eyebrow: 'Administración',
    title: 'Empresas pendientes.',
    description: 'Revisá y autorizá las empresas que esperan aprobación.',
    buttonLabel: 'Oferentes pendientes',
    buttonRoute: '/admin/oferentes-pendientes',
  },
  '/admin/oferentes-pendientes': {
    eyebrow: 'Administración',
    title: 'Oferentes pendientes.',
    description: 'Revisá y autorizá los oferentes que esperan aprobación.',
    buttonLabel: 'Empresas pendientes',
    buttonRoute: '/admin/pendientes',
  },
  '/admin/caracteristicas': {
    eyebrow: 'Administración',
    title: 'Gestión de características.',
    description: 'Administrá las categorías y subcategorías de habilidades del sistema.',
    buttonLabel: 'Ver pendientes',
    buttonRoute: '/admin/pendientes',
  },
  '/admin/reportes': {
    eyebrow: 'Administración',
    title: 'Reportes.',
    description: 'Generá reportes de puestos publicados por mes y año en formato PDF.',
    buttonLabel: 'Ver características',
    buttonRoute: '/admin/caracteristicas',
  },
};

interface Props {
  // Ruta actual del aplicativo (usada para buscar la configuración correspondiente)
  ruta: string;
  // Función de navegación para redirigir al usuario al hacer clic en el botón
  onNavegar: (ruta: string) => void;
}

function GlobalBanner({ ruta, onNavegar }: Props) {
  // Obtener la configuración para la ruta actual, o usar la de '/' como fallback
  const config = bannerConfig[ruta] || bannerConfig['/'];

  return (
    <section className="global-banner text-white">
      {/* Overlay oscuro para mejorar la legibilidad del texto */}
      <div className="banner-overlay">
        <div className="container py-5">
          <div className="row align-items-center g-4">
            <div className="col-lg-8">
              {/* Eyebrow: texto pequeño en mayúsculas y color primario */}
              <div className="text-primary fw-semibold text-uppercase mb-2" style={{ fontSize: '0.85rem' }}>{config.eyebrow}</div>
              {/* Título principal: grande y en negrita */}
              <h1 className="display-5 fw-bold mb-3">{config.title}</h1>
              {/* Descripción: texto lead (ligeramente más grande que el normal) */}
              <p className="lead mb-4 banner-copy">{config.description}</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

export default GlobalBanner;