import { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';

interface Sesion {
  id: number;
  correo: string;
  rol: string;
  referenciaId: number;
  token: string;
}

type Rol = 'ADMIN' | 'EMPRESA' | 'OFERENTE';

interface Props {
  rol: Rol;
  children: ReactNode;
  sesion: Sesion | null;
}

const DESTINOS: Record<string, string> = {
  ADMIN: '/admin/dashboard',
  EMPRESA: '/empresa/dashboard',
  OFERENTE: '/oferente/dashboard',
};

function AccesoRestringido({ rol, children, sesion }: Props) {
  if (!sesion || sesion.rol !== rol) {
    const destino = sesion ? DESTINOS[sesion.rol] || '/' : '/login';
    return <Navigate to={destino} replace />;
  }

  return <>{children}</>;
}

export default AccesoRestringido;
