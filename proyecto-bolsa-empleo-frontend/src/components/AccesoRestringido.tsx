import { ReactNode } from 'react';
import { Sesion, Rol } from '../types';

interface Props {
  sesion: Sesion | null;
  rol: Rol;
  children: ReactNode;
  onNavegar: (ruta: string) => void;
  mensaje?: string;
}

function AccesoRestringido({ sesion, rol, children, onNavegar, mensaje }: Props) {
  if (!sesion || sesion.rol !== rol) {
    return (
      <section className="container py-5">
        <div className="alert alert-warning">{mensaje || `Acceso restringido a ${rol.toLowerCase()}s autorizados.`}</div>
        <button className="btn btn-outline-secondary" onClick={() => onNavegar('/')}>Volver</button>
      </section>
    );
  }

  return <>{children}</>;
}

export default AccesoRestringido;
