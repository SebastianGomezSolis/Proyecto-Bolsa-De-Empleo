import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

interface MensajeGlobal {
  tipo: 'success' | 'error' | 'info' | 'warning' | 'danger';
  texto: string;
}

const FORM_EMPRESA = { correo: '', clave: '', nombre: '', localizacion: '', telefono: '', descripcion: '' };

interface Props {
  onMensaje: (m: MensajeGlobal) => void;
}

type FormValues = Record<string, string>;

function RegistroEmpresaPage({ onMensaje }: Props) {
  const navigate = useNavigate();
  const [form, setForm] = useState<FormValues>({ ...FORM_EMPRESA });
  const [cargando, setCargando] = useState(false);

  // Actualiza un campo del formulario
  const set = (campo: string, valor: string) => setForm((prev) => ({ ...prev, [campo]: valor }));

  // Envia los datos de registro al backend
  const manejarRegistro = async (e: React.FormEvent) => {
    e.preventDefault();
    setCargando(true);
    try {
      const response = await fetch("http://localhost:8080/api/auth/registro/empresa", {
        method: "POST",
        headers: new Headers({ "Content-Type": "application/json" }),
        body: JSON.stringify(form),
      });
      if (!response.ok) throw new Error(await response.text());
      onMensaje({ tipo: 'success', texto: 'Registro exitoso. Espere la aprobación del administrador.' });
      navigate('/login');
    } catch (error) {
      onMensaje({ tipo: 'danger', texto: (error as Error).message });
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } finally {
      setCargando(false);
    }
  };

  return (
    <section className="container my-4 flex-grow-1">
      <div className="row justify-content-center">
        <div className="col-md-6">
          <div className="card shadow-sm">
            <div className="card-body p-4">
              <div className="text-center mb-3">
                <h4 className="mb-3">Registro de Empresa</h4>
                <img
                  src="/images/empresas.png"
                  alt="Imagen de empresa"
                  className="img-fluid"
                  style={{ maxWidth: '100px' }}
                  onError={(e) => { (e.target as HTMLImageElement).style.display = 'none' }}
                />
              </div>

              <form onSubmit={manejarRegistro}>
                <div className="mb-3">
                  <label className="form-label">Nombre de la empresa</label>
                  <input type="text" className="form-control" value={form.nombre} onChange={(e) => set('nombre', e.target.value)} required />
                </div>
                <div className="mb-3">
                  <label className="form-label">Localización</label>
                  <input type="text" className="form-control" value={form.localizacion} onChange={(e) => set('localizacion', e.target.value)} />
                </div>
                <div className="mb-3">
                  <label className="form-label">Correo electrónico</label>
                  <input type="email" className="form-control" value={form.correo} onChange={(e) => set('correo', e.target.value)} required />
                </div>
                <div className="mb-3">
                  <label className="form-label">Teléfono</label>
                  <input type="text" className="form-control" value={form.telefono} onChange={(e) => set('telefono', e.target.value)} />
                </div>
                <div className="mb-3">
                  <label className="form-label">Descripción</label>
                  <textarea className="form-control" rows={3} value={form.descripcion} onChange={(e) => set('descripcion', e.target.value)} />
                </div>
                <div className="mb-4">
                  <label className="form-label">Contraseña</label>
                  <input type="password" className="form-control" value={form.clave} onChange={(e) => set('clave', e.target.value)} required />
                </div>

                <button type="submit" className="btn btn-dark w-100" disabled={cargando}>
                  {cargando ? 'Registrando...' : 'Registrarse'}
                </button>
              </form>

              <p className="text-center mt-3 mb-0">
                ¿Ya tiene una cuenta?{' '}
                <a href="/login" onClick={(e) => { e.preventDefault(); navigate('/login'); }} style={{ cursor: 'pointer' }}>
                  Iniciar sesión
                </a>
              </p>
              <p className="text-center mb-0">
                ¿Es un oferente?{' '}
                <a href="/registro/oferente" onClick={(e) => { e.preventDefault(); navigate('/registro/oferente'); }} style={{ cursor: 'pointer' }}>
                  Registrarse como oferente
                </a>
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

export default RegistroEmpresaPage;
