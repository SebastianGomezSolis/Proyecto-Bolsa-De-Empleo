import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';


interface MensajeGlobal {
  tipo: 'success' | 'error' | 'info' | 'warning' | 'danger';
  texto: string;
}

interface OferentePerfil {
  id: number;
  identificacion: string;
  nombre: string;
  primerApellido: string;
  telefono: string;
  lugarResidencia: string;
  curriculum: string;
  usuario: { id: number; correo: string };
}

interface Props {
  // Función de callback para mostrar mensajes globales (éxito/error)
  onMensaje: (m: MensajeGlobal) => void;
}

// Componente principal para gestionar el CV del oferente
function OferenteCVPage({ onMensaje }: Props) {
  const navigate = useNavigate();
  const descargar = async (url: string) => {
    const res = await fetch(url, { headers: new Headers({ "Authorization": "Bearer " + localStorage.getItem("token") }) });
    if (!res.ok) {
      onMensaje({ tipo: 'danger', texto: "No se pudo abrir el CV" });
      return;
    }
    const blob = await res.blob();
    window.open(URL.createObjectURL(blob), '_blank');
  };
  // Perfil del oferente (incluye la ruta del CV si existe)
  const [oferente, setOferente] = useState<OferentePerfil | null>(null);
  // Controla si se muestra el formulario para subir un nuevo CV
  const [mostrarSubir, setMostrarSubir] = useState(false);
  // Archivo PDF seleccionado para subir
  const [archivo, setArchivo] = useState<File | null>(null);
  // Indicador de carga inicial del perfil
  const [cargando, setCargando] = useState(true);
  // Indicador de subida de CV en curso
  const [subiendo, setSubiendo] = useState(false);

  // Effect para cargar el perfil del oferente al montar el componente
  useEffect(() => {
    fetch("http://localhost:8080/api/oferente/perfil", { headers: new Headers({ "Authorization": "Bearer " + localStorage.getItem("token") }) })
        .then(async (res) => { if (res.ok) setOferente(await res.json()); else throw new Error("No se pudo cargar el perfil"); })
        .catch((e: Error) => onMensaje({ tipo: 'danger', texto: e.message }))
        .finally(() => setCargando(false));
  }, [onMensaje]);

  // Subir el archivo CV al backend
  const subirCV = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!archivo) return;
    setSubiendo(true);
    try {
      const fd = new FormData(); fd.append('archivo', archivo);
      const subirRes = await fetch("http://localhost:8080/api/oferente/cv/subir", { method: 'POST', body: fd, headers: new Headers({ "Authorization": "Bearer " + localStorage.getItem("token") }) });
      if (!subirRes.ok) {
        throw new Error("No se pudo subir el CV");
      }
      onMensaje({ tipo: 'success', texto: 'CV subido correctamente.' });
      setMostrarSubir(false);
      const perfilRes = await fetch("http://localhost:8080/api/oferente/perfil", { headers: new Headers({ "Authorization": "Bearer " + localStorage.getItem("token") }) });
      if (!perfilRes.ok) {
        throw new Error("El CV se subió, pero no se pudo recargar el perfil");
      }
      const perfil = await perfilRes.json();
      setOferente(perfil);
    } catch (err) {
      onMensaje({ tipo: 'danger', texto: (err as Error).message });
    } finally {
      setSubiendo(false);
    }
  };

  return (
      <section className="container py-5" style={{ maxWidth: '600px' }}>
        <SectionTitle eyebrow="Oferente" title="Mi CV" />

        {cargando ? <LoadingBlock /> : (
            <>
              {/* Botones para ver y subir CV */}
              <div className="d-flex gap-2 mb-4">
                {oferente?.curriculum ? (
                    <button className="btn btn-dark" onClick={() => descargar("http://localhost:8080/api/oferente/cv")}>Ver CV</button>
                ) : (
                    <button className="btn btn-dark" disabled>Ver CV</button>
                )}
                <button className="btn btn-dark" onClick={() => setMostrarSubir(true)}>Subir CV</button>
              </div>

              {/* Mensaje informativo cuando no hay CV subido */}
              {!mostrarSubir && !oferente?.curriculum && (
                  <div className="alert alert-warning" style={{ maxWidth: '450px' }}>
                    Aún no tenés un CV subido. Presioná <strong>Subir CV</strong> para agregar uno.
                  </div>
              )}
              {/* Mensaje informativo cuando ya hay un CV subido */}
              {!mostrarSubir && oferente?.curriculum && (
                  <div className="alert alert-success" style={{ maxWidth: '450px' }}>
                    Ya tenés un CV subido. Podés verlo o reemplazarlo cuando quieras.
                  </div>
              )}

              {/* Formulario para subir un nuevo CV (solo PDF) */}
              {mostrarSubir && (
                  <div className="border rounded p-4 bg-white" style={{ maxWidth: '450px' }}>
                    <h5 className="mb-3">Subir nuevo CV</h5>
                    <p className="text-muted" style={{ fontSize: '0.88rem' }}>
                      Solo se aceptan archivos <strong>PDF</strong>. Si ya tenés uno subido, será reemplazado.
                    </p>
                    <form onSubmit={subirCV}>
                      <div className="mb-3">
                        <label className="form-label fw-semibold">Archivo PDF</label>
                        <input type="file" className="form-control" accept="application/pdf" required
                               onChange={(e) => setArchivo(e.target.files ? e.target.files[0] : null)} />
                      </div>
                      <div className="d-flex gap-2">
                        <button type="submit" className="btn btn-dark" disabled={subiendo}>
                          {subiendo ? 'Subiendo...' : 'Subir'}
                        </button>
                        <button type="button" className="btn btn-outline-dark" onClick={() => setMostrarSubir(false)}>Cancelar</button>
                      </div>
                    </form>
                  </div>
              )}
            </>
        )}
      </section>
  );
}

// Exportar el componente para usarlo en el enrutador
export default OferenteCVPage;
