import { ReactNode } from "react";
import { Navigate } from "react-router-dom";

interface Sesion {
  id: number;
  correo: string;
  rol: string;
  referenciaId: number;
  token: string;
}

interface ProtectedRouteProps {
    children: ReactNode;
    sesion: Sesion | null;
}

function ProtectedRoute({ children, sesion }: ProtectedRouteProps) {
    if (!sesion) {
        return <Navigate to="/login" replace />;
    }

    return <>{children}</>;
}

export default ProtectedRoute;
