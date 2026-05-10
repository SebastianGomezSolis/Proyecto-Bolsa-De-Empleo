import { useEffect, useState } from 'react';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';
import { api } from '../../services/api';
import { Sesion, MensajeGlobal, OferentePerfil } from '../../types';

interface Props {
  // Sesión actual del usuario (se usa para verificar rol de oferente)
  sesion: Sesion | null;
  // Función de navegación para redirigir al usuario a otras páginas
  onNavegar: (ruta: string) => void;
  // Función de callback para mostrar mensajes globales (éxito/error)
  onMensaje: (m: MensajeGlobal) => void;
}

// Componente principal para gestionar el CV del oferente
function OferenteCVPage({ sesion, onNavegar, onMensaje }: Props) {
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
    if (!sesion || sesion.rol !== 'OFERENTE') return;
    api.getPerfilOferente()
      .then(setOferente)
      .catch((e: Error) => onMensaje({ tipo: 'danger', texto: e.message }))
      .finally(() => setCargando(false));
  }, [sesion, onMensaje]);

  // Verificar que el usuario esté autenticado y tenga rol de oferente
  if (!sesion || sesion.rol !== 'OFERENTE') {
    return (
      <section className="container py-5">
        <div className="alert alert-warning">Acceso restringido a oferentes autorizados.</div>
        <button className="btn btn-outline-secondary" onClick={() => onNavegar('/')}>Volver</button>
      </section>
    );
  }

  // Subir el archivo CV al backend
  const subirCV = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!archivo) return;
    setSubiendo(true);
    try {
      await api.subirCV(archivo);
      onMensaje({ tipo: 'success', texto: 'CV subido correctamente.' });
      setMostrarSubir(false);
      // Recargar el perfil para reflejar el nuevo CV
      const perfil = await api.getPerfilOferente();
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
              <a href={`/${oferente.curriculum}`}
                target="_blank" rel="noreferrer" className="btn btn-dark">Ver CV</a>
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
