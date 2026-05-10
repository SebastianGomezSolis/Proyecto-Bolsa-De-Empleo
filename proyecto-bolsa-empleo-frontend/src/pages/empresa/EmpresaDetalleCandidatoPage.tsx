import { useEffect, useState } from 'react';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';
import { api } from '../../services/api';
import { Sesion, MensajeGlobal, OferentePerfil, Habilidad } from '../../types';

interface Props {
  // Sesión actual del usuario (se usa para verificar rol de empresa)
  sesion: Sesion | null;
  // Función de navegación para redirigir al usuario a otras páginas
  onNavegar: (ruta: string) => void;
  // Función de callback para mostrar mensajes globales (éxito/error)
  onMensaje: (m: MensajeGlobal) => void;
  // ID del oferente a visualizar
  id?: number;
  // ID del puesto asociado (para mostrar habilidades relevantes)
  puestoId?: number;
}

// Componente principal para mostrar el detalle de un candidato (oferente)
// Incluye información personal, CV y habilidades registradas
function EmpresaDetalleCandidatoPage({ sesion, onNavegar, onMensaje, id, puestoId }: Props) {
  // Estado para almacenar la información personal del oferente
  const [oferente, setOferente] = useState<OferentePerfil | null>(null);
  // Estado para almacenar la lista de habilidades del oferente
  const [habilidades, setHabilidades] = useState<Habilidad[]>([]);
  // Estado para indicar si se está cargando datos desde el backend
  const [cargando, setCargando] = useState(true);

  // Effect para cargar el detalle del oferente y sus habilidades al montar el componente
  useEffect(() => {
    // Solo proceder si el usuario es empresa, existe un ID de oferente y un ID de puesto
    if (!sesion || sesion.rol !== 'EMPRESA' || !id) return;
    if (!puestoId) return;
    api.getDetalleOferente(id, puestoId)
      .then((data) => { setOferente(data.oferente); setHabilidades(data.habilidades || []); })
      .catch((e: Error) => onMensaje({ tipo: 'danger', texto: e.message }))
      .finally(() => setCargando(false));
  }, [sesion, id, puestoId, onMensaje]);

  // Verificar si el usuario tiene sesión activa y rol de empresa
  // Si no cumple, mostrar mensaje de acceso restringido con botón para volver
  if (!sesion || sesion.rol !== 'EMPRESA') {
    return (
      <section className="container py-5">
        <div className="alert alert-warning">Acceso restringido a empresas autorizadas.</div>
        <button className="btn btn-outline-secondary" onClick={() => onNavegar('/')}>Volver</button>
      </section>
    );
  }

  return (
    <section className="container py-5">
      <SectionTitle eyebrow="Empresa" title="Detalle de oferente" />

      {cargando ? <LoadingBlock /> : (
        <>
          {/* Tarjeta con información personal del oferente */}
          {oferente && (
            <div className="border rounded p-3 mb-4">
              <h5>{oferente.nombre} {oferente.primerApellido}</h5>
              <p className="mb-1"><strong>Identificación:</strong> {oferente.identificacion}</p>
              <p className="mb-1"><strong>Email:</strong> {oferente.usuario?.correo}</p>
              <p className="mb-1"><strong>Teléfono:</strong> {oferente.telefono}</p>
              <p className="mb-1"><strong>Residencia:</strong> {oferente.lugarResidencia}</p>
              {/* Enlace para descargar el CV si está disponible */}
              {oferente.curriculum && (
                <div className="mt-2">
                  <a href={`/${oferente.curriculum}`}
                    target="_blank" rel="noreferrer" className="btn btn-dark btn-sm">Ver CV</a>
                </div>
              )}
            </div>
          )}

          {/* Tabla de habilidades registradas por el oferente */}
          <h5>Habilidades</h5>
          <table className="table table-hover">
            <thead>
              <tr><th>Característica</th><th>Nivel</th></tr>
            </thead>
            <tbody>
              {habilidades.length === 0 ? (
                <tr><td colSpan={2} className="text-muted">No tiene habilidades registradas.</td></tr>
              ) : habilidades.map((h) => (
                <tr key={h.id}>
                  <td>{h.caracteristica?.nombre}</td>
                  <td>{h.nivel}</td>
                </tr>
              ))}
            </tbody>
          </table>

          {/* Botón para volver a la lista de candidatos del puesto */}
          <button className="btn btn-dark btn-sm"
            onClick={() => onNavegar(puestoId ? `/empresa/puestos/${puestoId}/candidatos` : '/empresa/puestos')}>
            Volver
          </button>
        </>
      )}
    </section>
  );
}

export default EmpresaDetalleCandidatoPage;
