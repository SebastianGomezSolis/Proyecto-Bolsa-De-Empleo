import { useState } from 'react';
import SectionTitle from '../../components/SectionTitle';
import { api } from '../../services/api';
import { guardarSesion } from '../../services/authService';
import { MensajeGlobal, Sesion } from '../../types';

interface Props {
  onSesion: (s: Sesion) => void;
  onNavegar: (ruta: string) => void;
  onMensaje: (m: MensajeGlobal) => void;
}

function LoginPage({ onSesion, onNavegar, onMensaje }: Props) {
  const [form, setForm] = useState({
    correo: localStorage.getItem('bolsa.correo') ?? '',
    clave: localStorage.getItem('bolsa.clave') ?? '',
  });
  const [recordar, setRecordar] = useState(
    () => localStorage.getItem('bolsa.recordar') === 'true'
  );
  const [cargando, setCargando] = useState(false);

  const set = (campo: string, valor: string) => setForm((prev) => ({ ...prev, [campo]: valor }));

  const manejarLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setCargando(true);
    try {
      const datos = await api.login({ correo: form.correo, clave: form.clave });

      if (recordar) {
        localStorage.setItem('bolsa.recordar', 'true');
        localStorage.setItem('bolsa.correo', form.correo);
        localStorage.setItem('bolsa.clave', form.clave);
      } else {
        localStorage.removeItem('bolsa.recordar');
        localStorage.removeItem('bolsa.correo');
        localStorage.removeItem('bolsa.clave');
      }

      guardarSesion(datos);
      onSesion(datos);
      onMensaje({ tipo: 'success', texto: `Bienvenido, ${datos.correo}.` });
      const destinos: Record<string, string> = { ADMIN: '/admin/dashboard', EMPRESA: '/empresa/dashboard', OFERENTE: '/oferente/dashboard' };
      onNavegar(destinos[datos.rol] || '/');
    } catch (error) {
      onMensaje({ tipo: 'danger', texto: (error as Error).message });
    } finally {
      setCargando(false);
    }
  };

  return (
    <section className="container py-5">
      <div className="row justify-content-center">
        <div className="col-lg-5 col-xl-4">
          <div className="card shadow-lg border-0">
            <div className="card-body p-4 p-md-5">
              <SectionTitle
                eyebrow="Acceso"
                title="Iniciar sesión"
                description="Ingresá con tu correo y contraseña."
              />
              <form onSubmit={manejarLogin} className="row g-3">
                <div className="col-12">
                  <label className="form-label">Correo electrónico</label>
                  <input
                    type="email"
                    className="form-control"
                    value={form.correo}
                    onChange={(e) => set('correo', e.target.value)}
                    required
                  />
                </div>
                <div className="col-12">
                  <label className="form-label">Contraseña</label>
                  <input
                    type="password"
                    className="form-control"
                    value={form.clave}
                    onChange={(e) => set('clave', e.target.value)}
                    required
                  />
                </div>
                <div className="col-12 d-flex align-items-center gap-2">
                  <input
                    type="checkbox"
                    id="recordar"
                    className="form-check-input m-0"
                    checked={recordar}
                    onChange={(e) => setRecordar(e.target.checked)}
                  />
                  <label htmlFor="recordar" className="form-check-label">
                    Recordar credenciales
                  </label>
                </div>
                <div className="col-12 d-grid">
                  <button className="btn btn-primary fw-semibold" disabled={cargando}>
                    {cargando ? 'Ingresando...' : 'Iniciar sesión'}
                  </button>
                </div>
              </form>
              <hr className="my-4" />
              <p className="text-center mb-0 small">
                ¿No tenés cuenta?{' '}
                <button className="btn btn-link p-0 small" onClick={() => onNavegar('/registro')}>
                  Registrate aquí
                </button>
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

export default LoginPage;
