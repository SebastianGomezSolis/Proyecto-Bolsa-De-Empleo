import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import SectionTitle from '../../components/SectionTitle';

interface MensajeGlobal {
  tipo: 'success' | 'error' | 'info' | 'warning' | 'danger';
  texto: string;
}

interface Props {
  onMensaje: (m: MensajeGlobal) => void;
}

function LoginPage({ onMensaje }: Props) {
  const navigate = useNavigate();
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
      const response = await fetch("http://localhost:8080/api/auth/login", {
        method: "POST",
        headers: new Headers({ "Content-Type": "application/json" }),
        body: JSON.stringify({ correo: form.correo, clave: form.clave })
      });
      if (!response.ok) throw new Error(await response.text());
      const datos = await response.json();

      if (recordar) {
        localStorage.setItem('bolsa.recordar', 'true');
        localStorage.setItem('bolsa.correo', form.correo);
        localStorage.setItem('bolsa.clave', form.clave);
      } else {
        localStorage.removeItem('bolsa.recordar');
        localStorage.removeItem('bolsa.correo');
        localStorage.removeItem('bolsa.clave');
      }

      localStorage.setItem("token", datos.token);
      onMensaje({ tipo: 'success', texto: `Bienvenido, ${datos.correo}.` });
      const destinos: Record<string, string> = { ADMIN: '/admin/dashboard', EMPRESA: '/empresa/dashboard', OFERENTE: '/oferente/dashboard' };
      navigate(destinos[datos.rol] || '/');
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
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

export default LoginPage;
