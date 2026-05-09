import React, { useCallback, useEffect, useMemo, useState } from 'react';
import './index.css';
import Header from './components/Header';
import Footer from './components/Footer';
import GlobalBanner from './components/GlobalBanner';
import AlertaMensaje from './components/AlertaMensaje';
import HomePage from './pages/publico/HomePage';
import LoginPage from './pages/publico/LoginPage';
import RegistroPage from './pages/publico/RegistroPage';
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
import { obtenerSesionGuardada, limpiarSesion } from './services/authService';
import { MensajeGlobal, Sesion } from './types';

function obtenerRuta(): string {
  const hash = window.location.hash || '#/';
  const ruta = hash.replace('#', '');
  return ruta.startsWith('/') ? ruta : `/${ruta}`;
}

interface RutaParseada {
  base: string;
  params: Record<string, string>;
}

function parsearRuta(ruta: string): RutaParseada {
  const candidatosMatch = ruta.match(/^\/empresa\/puestos\/(\d+)\/candidatos$/);
  if (candidatosMatch) return { base: '/empresa/puestos/:id/candidatos', params: { puestoId: candidatosMatch[1] } };

  const detalleCandidatoMatch = ruta.match(/^\/empresa\/candidatos\/(\d+)/);
  if (detalleCandidatoMatch) {
    const searchParams = new URLSearchParams(ruta.split('?')[1] || '');
    return { base: '/empresa/candidatos/:id', params: { id: detalleCandidatoMatch[1], puestoId: searchParams.get('puestoId') || '' } };
  }

  const registroMatch = ruta.match(/^\/registro\/(empresa|oferente)$/);
  if (registroMatch) return { base: '/registro/:tipo', params: { tipo: registroMatch[1] } };

  return { base: ruta, params: {} };
}

const RUTAS_CON_BANNER = ['/', '/puestos/buscar', '/login', '/registro/empresa', '/registro/oferente'];

function App() {
  const [ruta, setRuta] = useState<string>(obtenerRuta);
  const [sesion, setSesion] = useState<Sesion | null>(obtenerSesionGuardada);
  const [mensaje, setMensaje] = useState<MensajeGlobal | null>(null);

  useEffect(() => {
    const handler = () => setRuta(obtenerRuta());
    window.addEventListener('hashchange', handler);
    return () => window.removeEventListener('hashchange', handler);
  }, []);

  const navegar = useCallback((destino: string) => {
    window.location.hash = destino;
  }, []);

  function cerrarSesion() {
    limpiarSesion();
    setSesion(null);
    setMensaje({ tipo: 'success', texto: 'Sesión cerrada correctamente.' });
    navegar('/');
  }

  const { base, params } = useMemo(() => parsearRuta(ruta), [ruta]);

  const pagina = useMemo(() => {
    switch (base) {
      case '/login':
        return <LoginPage onSesion={setSesion} onNavegar={navegar} onMensaje={setMensaje} />;
      case '/registro/:tipo':
      case '/registro/empresa':
      case '/registro/oferente':
        return <RegistroPage onNavegar={navegar} onMensaje={setMensaje} tipoInicial={params.tipo} />;
      case '/puestos/buscar':
        return <BuscarPuestoPublicoPage onMensaje={setMensaje} />;
      case '/empresa/dashboard':
        return <DashboardEmpresaPage sesion={sesion} onNavegar={navegar} />;
      case '/empresa/puestos':
        return <EmpresaPuestosPage sesion={sesion} onNavegar={navegar} onMensaje={setMensaje} />;
      case '/empresa/publicar':
        return <EmpresaPublicarPage sesion={sesion} onNavegar={navegar} onMensaje={setMensaje} />;
      case '/empresa/puestos/:id/candidatos':
        return <EmpresaCandidatosPage sesion={sesion} onNavegar={navegar} onMensaje={setMensaje} puestoId={Number(params.puestoId)} />;
      case '/empresa/candidatos/:id':
        return <EmpresaDetalleCandidatoPage sesion={sesion} onNavegar={navegar} onMensaje={setMensaje} id={Number(params.id)} puestoId={Number(params.puestoId)} />;
      case '/oferente/dashboard':
        return <DashboardOferentePage sesion={sesion} onNavegar={navegar} />;
      case '/oferente/habilidades':
        return <OferenteHabilidadesPage sesion={sesion} onNavegar={navegar} onMensaje={setMensaje} />;
      case '/oferente/cv':
        return <OferenteCVPage sesion={sesion} onNavegar={navegar} onMensaje={setMensaje} />;
      case '/oferente/buscar':
        return <OferenteBuscarPuestoPage sesion={sesion} onNavegar={navegar} onMensaje={setMensaje} />;
      case '/admin/dashboard':
        return <DashboardAdminPage sesion={sesion} onNavegar={navegar} />;
      case '/admin/pendientes':
        return <AdminPendientesPage sesion={sesion} onNavegar={navegar} onMensaje={setMensaje} />;
      case '/admin/oferentes-pendientes':
        return <AdminOferentesPendientesPage sesion={sesion} onNavegar={navegar} onMensaje={setMensaje} />;
      case '/admin/caracteristicas':
        return <AdminCaracteristicasPage sesion={sesion} onNavegar={navegar} onMensaje={setMensaje} />;
      case '/admin/reportes':
        return <AdminReportesPage sesion={sesion} onNavegar={navegar} />;
      default:
        return <HomePage sesion={sesion} onNavegar={navegar} onMensaje={setMensaje} />;
    }
  }, [base, params, sesion, navegar]);

  const mostrarBanner = RUTAS_CON_BANNER.includes(ruta);

  return (
    <div className="app-shell bg-body-tertiary min-vh-100 d-flex flex-column">
      <Header sesion={sesion} ruta={ruta} onNavegar={navegar} onLogout={cerrarSesion} />
      {mostrarBanner && <GlobalBanner ruta={ruta} onNavegar={navegar} />}

      <AlertaMensaje mensaje={mensaje} onCerrar={() => setMensaje(null)} />

      <main className="flex-grow-1">
        {pagina}
      </main>

      <Footer />
    </div>
  );
}

export default App;
