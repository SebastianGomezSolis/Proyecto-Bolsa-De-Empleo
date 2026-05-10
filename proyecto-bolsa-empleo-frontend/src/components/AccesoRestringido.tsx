// Componente que controla el acceso a rutas basadas en el rol del usuario.
// Si el usuario no está autenticado o no tiene el rol requerido, muestra un mensaje
// de acceso restringido y un botón para volver a la página principal.
// Si tiene el rol correcto, renderiza los hijos (el contenido protegido).

import { ReactNode } from 'react';
import { Sesion, Rol } from '../types';

interface Props {
  // Sesión actual del usuario (null si no está autenticado)
  sesion: Sesion | null;
  // Rol requerido para acceder al contenido (ADMIN, EMPRESA u OFERENTE)
  rol: Rol;
  // Contenido a renderizar si el usuario tiene acceso
  children: ReactNode;
  // Función de navegación para redirigir al usuario si no tiene acceso
  onNavegar: (ruta: string) => void;
  // Mensaje opcional a mostrar cuando se niega el acceso
  mensaje?: string;
}

function AccesoRestringido({ sesion, rol, children, onNavegar, mensaje }: Props) {
  // Verificar si el usuario está autenticado y tiene el rol requerido
  if (!sesion || sesion.rol !== rol) {
    return (
      <section className="container py-5">
        {/* Mostrar mensaje de advertencia (personalizado o por defecto) */}
        <div className="alert alert-warning">{mensaje || `Acceso restringido a ${rol.toLowerCase()}s autorizados.`}</div>
        {/* Botón para volver a la página principal */}
        <button className="btn btn-outline-secondary" onClick={() => onNavegar('/')}>Volver</button>
      </section>
    );
  }

  // Si tiene permisos, renderizar el contenido protegido
  return <>{children}</>;
}

export default AccesoRestringido;