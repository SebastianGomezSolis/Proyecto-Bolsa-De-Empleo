import { useEffect, useState } from 'react';
import { api } from '../../services/api';
import { MensajeGlobal, Nacionalidad } from '../../types';

const FORM_EMPRESA = { correo: '', clave: '', nombre: '', localizacion: '', telefono: '', descripcion: '' };
const FORM_OFERENTE = { correo: '', clave: '', identificacion: '', nombre: '', primerApellido: '', isoNacionalidad: '', telefono: '', lugarResidencia: '' };

interface Props {
  onNavegar: (ruta: string) => void;
  onMensaje: (m: MensajeGlobal) => void;
  tipoInicial?: string;
}

type FormValues = Record<string, string>;

function RegistroPage({ onNavegar, onMensaje, tipoInicial }: Props) {
  const [tipo, setTipo] = useState(tipoInicial === 'empresa' ? 'empresa' : 'oferente');
  const [form, setForm] = useState<FormValues>(tipoInicial === 'empresa' ? { ...FORM_EMPRESA } : { ...FORM_OFERENTE });
  const [nacionalidades, setNacionalidades] = useState<Nacionalidad[]>([]);
  const [cargando, setCargando] = useState(false);

  useEffect(() => {
    api.getNacionalidades().then(setNacionalidades).catch(() => {});
  }, []);

  const cambiarTipo = (t: string) => {
    setTipo(t);
    setForm(t === 'empresa' ? { ...FORM_EMPRESA } : { ...FORM_OFERENTE });
    onNavegar(`/registro/${t}`);
  };

  const set = (campo: string, valor: string) => setForm((prev) => ({ ...prev, [campo]: valor }));

  const manejarRegistro = async (e: React.FormEvent) => {
    e.preventDefault();
    setCargando(true);
    try {
      const fn = tipo === 'empresa' ? api.registrarEmpresa : api.registrarOferente;
      const msg = await fn(form);
      onMensaje({ tipo: 'success', texto: typeof msg === 'string' ? msg : 'Registro exitoso. Espere la aprobación del administrador.' });
      onNavegar('/login');
    } catch (error) {
      onMensaje({ tipo: 'danger', texto: (error as Error).message });
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
                <h4 className="mb-3">Registro de {tipo === 'empresa' ? 'Empresa' : 'Oferente'}</h4>
                <img
                  src={tipo === 'empresa' ? '/images/empresas.png' : '/images/oferente.png'}
                  alt="Imagen de registro"
                  className="img-fluid"
                  style={{ maxWidth: '100px' }}
                  onError={(e) => { (e.target as HTMLImageElement).style.display = 'none' }}
                />
              </div>

              <div className="btn-group w-100 mb-4" role="group">
                <button type="button" className={`btn ${tipo === 'oferente' ? 'btn-dark' : 'btn-outline-dark'}`} onClick={() => cambiarTipo('oferente')}>
                  Oferente
                </button>
                <button type="button" className={`btn ${tipo === 'empresa' ? 'btn-dark' : 'btn-outline-dark'}`} onClick={() => cambiarTipo('empresa')}>
                  Empresa
                </button>
              </div>

              <form onSubmit={manejarRegistro}>
                {tipo === 'empresa' && (
                  <>
                    <div className="mb-3">
                      <label className="form-label">Nombre</label>
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
                  </>
                )}

                {tipo === 'oferente' && (
                  <>
                    <div className="mb-3">
                      <label className="form-label">Identificación</label>
                      <input type="text" className="form-control" value={form.identificacion} onChange={(e) => set('identificacion', e.target.value)} required />
                    </div>
                    <div className="mb-3">
                      <label className="form-label">Nombre</label>
                      <input type="text" className="form-control" value={form.nombre} onChange={(e) => set('nombre', e.target.value)} required />
                    </div>
                    <div className="mb-3">
                      <label className="form-label">Primer apellido</label>
                      <input type="text" className="form-control" value={form.primerApellido} onChange={(e) => set('primerApellido', e.target.value)} required />
                    </div>
                    <div className="mb-3">
                      <label className="form-label">Nacionalidad</label>
                      <select className="form-select" value={form.isoNacionalidad} onChange={(e) => set('isoNacionalidad', e.target.value)} required>
                        <option value="">Seleccione una nacionalidad...</option>
                        {nacionalidades.map((n) => (
                          <option key={n.iso} value={n.iso}>{n.nombre}</option>
                        ))}
                      </select>
                    </div>
                    <div className="mb-3">
                      <label className="form-label">Teléfono</label>
                      <input type="text" className="form-control" value={form.telefono} onChange={(e) => set('telefono', e.target.value)} />
                    </div>
                    <div className="mb-3">
                      <label className="form-label">Correo electrónico</label>
                      <input type="email" className="form-control" value={form.correo} onChange={(e) => set('correo', e.target.value)} required />
                    </div>
                    <div className="mb-3">
                      <label className="form-label">Lugar de residencia</label>
                      <input type="text" className="form-control" value={form.lugarResidencia} onChange={(e) => set('lugarResidencia', e.target.value)} />
                    </div>
                    <div className="mb-4">
                      <label className="form-label">Contraseña</label>
                      <input type="password" className="form-control" value={form.clave} onChange={(e) => set('clave', e.target.value)} required />
                    </div>
                  </>
                )}

                <button type="submit" className="btn btn-dark w-100" disabled={cargando}>
                  {cargando ? 'Registrando...' : 'Registrarse'}
                </button>
              </form>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

export default RegistroPage;
