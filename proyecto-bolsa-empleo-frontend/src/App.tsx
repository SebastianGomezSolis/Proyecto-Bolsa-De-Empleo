import { useState } from 'react';
import { BrowserRouter, Routes, Route, useLocation, useNavigate } from 'react-router-dom';
import './index.css';
import Header from './components/Header';
import Footer from './components/Footer';
import GlobalBanner from './components/GlobalBanner';
import AlertaMensaje from './components/AlertaMensaje';
import ProtectedRoute from './components/ProtectedRoute';
import AccesoRestringido from './components/AccesoRestringido';
import HomePage from './pages/publico/HomePage';
import LoginPage from './pages/publico/LoginPage';
import RegistroEmpresaPage from './pages/publico/RegistroEmpresaPage';
import RegistroOferentePage from './pages/publico/RegistroOferentePage';
import BuscarPuestoPublicoPage from './pages/publico/BuscarPuestoPublicoPage';
import DashboardAdminPage from './pages/admin/DashboardAdminPage';
import AdminPendientesPage from './pages/admin/AdminPendientesPage';
import AdminOferentesPendientesPage from './pages/admin/AdminOferentesPendientesPage';
import AdminCaracteristicasPage from './pages/admin/AdminCaracteristicasPage';
import AdminReportesPage from './pages/admin/AdminReportesPage';
import DashboardEmpresaPage from './pages/empresa/DashboardEmpresaPage';
import EmpresaPuestosPage from './pages/empresa/EmpresaPuestosPage';
import EmpresaPublicarPage from './pages/empresa/EmpresaPublicarPage';
import EmpresaCandidatosPage from './pages/empresa/EmpresaCandidatosPage';
import EmpresaDetalleCandidatoPage from './pages/empresa/EmpresaDetalleCandidatoPage';
import DashboardOferentePage from './pages/oferente/DashboardOferentePage';
import OferenteBuscarPuestoPage from './pages/oferente/OferenteBuscarPuestoPage';
import OferenteHabilidadesPage from './pages/oferente/OferenteHabilidadesPage';
import OferenteCVPage from './pages/oferente/OferenteCVPage';
interface MensajeGlobal {
  tipo: 'success' | 'error' | 'info' | 'warning' | 'danger';
  texto: string;
}

function decodificarToken(token: string): { id: number; correo: string; rol: string; referenciaId: number; token: string } | null {
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return {
            id: payload.id,
            correo: payload.sub,
            rol: payload.rol,
            referenciaId: payload.referenciaId,
            token: token,
        };
    } catch {
        return null;
    }
}

function AppContent() {
    const location = useLocation();
    const navigate = useNavigate();
    const token = localStorage.getItem('token');
    const sesion = token ? decodificarToken(token) : null;
    const [mensaje, setMensaje] = useState<MensajeGlobal | null>(null);

    return (
        <div className="app-shell bg-body-tertiary min-vh-100 d-flex flex-column">
            <Header sesion={sesion} />
            <GlobalBanner ruta={location.pathname} onNavegar={navigate} />
            <AlertaMensaje mensaje={mensaje} onCerrar={() => setMensaje(null)} />

            <main className="flex-grow-1">
                <Routes>
                    <Route path="/login" element={<LoginPage onMensaje={setMensaje} />} />
                    <Route path="/registro/empresa" element={<RegistroEmpresaPage onMensaje={setMensaje} />} />
                    <Route path="/registro/oferente" element={<RegistroOferentePage onMensaje={setMensaje} />} />
                    <Route path="/puestos/buscar" element={<BuscarPuestoPublicoPage onMensaje={setMensaje} />} />
                    <Route path="/" element={<HomePage onMensaje={setMensaje} />} />

                    <Route path="/empresa/dashboard" element={
                        <ProtectedRoute sesion={sesion}><AccesoRestringido sesion={sesion} rol="EMPRESA"><DashboardEmpresaPage /></AccesoRestringido></ProtectedRoute>
                    } />
                    <Route path="/empresa/puestos" element={
                        <ProtectedRoute sesion={sesion}><AccesoRestringido sesion={sesion} rol="EMPRESA"><EmpresaPuestosPage onMensaje={setMensaje} /></AccesoRestringido></ProtectedRoute>
                    } />
                    <Route path="/empresa/publicar" element={
                        <ProtectedRoute sesion={sesion}><AccesoRestringido sesion={sesion} rol="EMPRESA"><EmpresaPublicarPage onMensaje={setMensaje} /></AccesoRestringido></ProtectedRoute>
                    } />
                    <Route path="/empresa/puestos/:puestoId/candidatos" element={
                        <ProtectedRoute sesion={sesion}><AccesoRestringido sesion={sesion} rol="EMPRESA"><EmpresaCandidatosPage onMensaje={setMensaje} /></AccesoRestringido></ProtectedRoute>
                    } />
                    <Route path="/empresa/candidatos/:id/puesto/:puestoId" element={
                        <ProtectedRoute sesion={sesion}><AccesoRestringido sesion={sesion} rol="EMPRESA"><EmpresaDetalleCandidatoPage onMensaje={setMensaje} /></AccesoRestringido></ProtectedRoute>
                    } />

                    <Route path="/oferente/dashboard" element={
                        <ProtectedRoute sesion={sesion}><AccesoRestringido sesion={sesion} rol="OFERENTE"><DashboardOferentePage /></AccesoRestringido></ProtectedRoute>
                    } />
                    <Route path="/oferente/habilidades" element={
                        <ProtectedRoute sesion={sesion}><AccesoRestringido sesion={sesion} rol="OFERENTE"><OferenteHabilidadesPage onMensaje={setMensaje} /></AccesoRestringido></ProtectedRoute>
                    } />
                    <Route path="/oferente/cv" element={
                        <ProtectedRoute sesion={sesion}><AccesoRestringido sesion={sesion} rol="OFERENTE"><OferenteCVPage onMensaje={setMensaje} /></AccesoRestringido></ProtectedRoute>
                    } />
                    <Route path="/oferente/buscar" element={
                        <ProtectedRoute sesion={sesion}><AccesoRestringido sesion={sesion} rol="OFERENTE"><OferenteBuscarPuestoPage onMensaje={setMensaje} /></AccesoRestringido></ProtectedRoute>
                    } />

                    <Route path="/admin/dashboard" element={
                        <ProtectedRoute sesion={sesion}><AccesoRestringido sesion={sesion} rol="ADMIN"><DashboardAdminPage /></AccesoRestringido></ProtectedRoute>
                    } />
                    <Route path="/admin/pendientes" element={
                        <ProtectedRoute sesion={sesion}><AccesoRestringido sesion={sesion} rol="ADMIN"><AdminPendientesPage onMensaje={setMensaje} /></AccesoRestringido></ProtectedRoute>
                    } />
                    <Route path="/admin/oferentes-pendientes" element={
                        <ProtectedRoute sesion={sesion}><AccesoRestringido sesion={sesion} rol="ADMIN"><AdminOferentesPendientesPage onMensaje={setMensaje} /></AccesoRestringido></ProtectedRoute>
                    } />
                    <Route path="/admin/caracteristicas" element={
                        <ProtectedRoute sesion={sesion}><AccesoRestringido sesion={sesion} rol="ADMIN"><AdminCaracteristicasPage onMensaje={setMensaje} /></AccesoRestringido></ProtectedRoute>
                    } />
                    <Route path="/admin/reportes" element={
                        <ProtectedRoute sesion={sesion}><AccesoRestringido sesion={sesion} rol="ADMIN"><AdminReportesPage onMensaje={setMensaje} /></AccesoRestringido></ProtectedRoute>
                    } />
                </Routes>
            </main>

            <Footer />
        </div>
    );
}

function App() {
    return (
        <BrowserRouter>
            <AppContent />
        </BrowserRouter>
    );
}

export default App;
