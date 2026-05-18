// Página de registro de usuarios. Permite crear cuentas tanto para empresas
// como para oferentes, con formularios adaptados según el tipo seleccionado.

import { useEffect, useState } from 'react';
import { BASE_API, getAuthHeaders } from '../../services/api';
import { MensajeGlobal, Nacionalidad } from '../../types';

// Valores iniciales del formulario para cada tipo de registro
const FORM_EMPRESA = { correo: '', clave: '', nombre: '', localizacion: '', telefono: '', descripcion: '' };
const FORM_OFERENTE = { correo: '', clave: '', identificacion: '', nombre: '', primerApellido: '', isoNacionalidad: '', telefono: '', lugarResidencia: '' };

interface Props {
  // Función de navegación para redirigir al usuario a otras páginas
  onNavegar: (ruta: string) => void;
  // Función de callback para mostrar mensajes globales (éxito/error)
  onMensaje: (m: MensajeGlobal) => void;
  // Tipo de registro inicial ('empresa' u 'oferente')
  tipoInicial?: string;
}

type FormValues = Record<string, string>;

function RegistroPage({ onNavegar, onMensaje, tipoInicial }: Props) {
  // Estado para el tipo de registro seleccionado (empresa / oferente)
  const [tipo, setTipo] = useState(tipoInicial === 'empresa' ? 'empresa' : 'oferente');
  // Estado del formulario con valores según el tipo seleccionado
  const [form, setForm] = useState<FormValues>(tipoInicial === 'empresa' ? { ...FORM_EMPRESA } : { ...FORM_OFERENTE });
  // Estado para la lista de nacionalidades (requerida para oferentes)
  const [nacionalidades, setNacionalidades] = useState<Nacionalidad[]>([]);
  // Estado para indicar si se está procesando el registro
  const [cargando, setCargando] = useState(false);

  // Effect que carga las nacionalidades disponibles al montar el componente
  useEffect(() => {
    fetch(`${BASE_API}/publico/nacionalidades`, { headers: getAuthHeaders() })
      .then(async (res) => { if (res.ok) setNacionalidades(await res.json()); })
      .catch(() => {});
  }, []);

  // Cambia entre tipo empresa / oferente y reinicia el formulario
  const cambiarTipo = (t: string) => {
    setTipo(t);
    setForm(t === 'empresa' ? { ...FORM_EMPRESA } : { ...FORM_OFERENTE });
    onNavegar(`/registro/${t}`);
  };

  // Función auxiliar para actualizar un campo específico del formulario
  const set = (campo: string, valor: string) => setForm((prev) => ({ ...prev, [campo]: valor }));

  // Manejador del envío del formulario de registro
  const manejarRegistro = async (e: React.FormEvent) => {
    e.preventDefault();
    setCargando(true);
    try {
      const url = tipo === 'empresa' ? `${BASE_API}/auth/registro/empresa` : `${BASE_API}/auth/registro/oferente`;
      const response = await fetch(url, { method: 'POST', headers: { 'Content-Type': 'application/json', ...getAuthHeaders() }, body: JSON.stringify(form) });
      if (!response.ok) throw new Error(await response.text());
      const msg = await response.text();
      onMensaje({ tipo: 'success', texto: typeof msg === 'string' ? msg : 'Registro exitoso. Espere la aprobación del administrador.' });
      onNavegar('/login');
    } catch (error) {
      onMensaje({ tipo: 'danger', texto: (error as Error).message });
    } finally {
      setCargando(false);
    }
  };

  return (
    // Render del formulario de registro con selector de tipo (empresa / oferente)
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

              {/* Botones para alternar entre registro de empresa y oferente */}
              <div className="btn-group w-100 mb-4" role="group">
                <button type="button" className={`btn ${tipo === 'oferente' ? 'btn-dark' : 'btn-outline-dark'}`} onClick={() => cambiarTipo('oferente')}>
                  Oferente
                </button>
                <button type="button" className={`btn ${tipo === 'empresa' ? 'btn-dark' : 'btn-outline-dark'}`} onClick={() => cambiarTipo('empresa')}>
                  Empresa
                </button>
              </div>

              <form onSubmit={manejarRegistro}>
                {/* Campos del formulario para registro de empresa */}
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

                {/* Campos del formulario para registro de oferente */}
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
