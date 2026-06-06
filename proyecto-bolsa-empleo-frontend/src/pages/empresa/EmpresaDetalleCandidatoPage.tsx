import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
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

interface Habilidad {
  id: number;
  caracteristica: { id: number; nombre: string };
  nivel: number;
}

interface Props {
  // Función de callback para mostrar mensajes globales (éxito/error)
  onMensaje: (m: MensajeGlobal) => void;
}

// Componente principal para mostrar el detalle de un candidato (oferente)
// Incluye información personal, CV y habilidades registradas
function EmpresaDetalleCandidatoPage({ onMensaje }: Props) {
  const navigate = useNavigate();
  const { id, puestoId } = useParams();
  const descargar = async (url: string) => {
    const res = await fetch(url, { headers: new Headers({ "Authorization": "Bearer " + localStorage.getItem("token") }) });
    if (!res.ok) {
      onMensaje({ tipo: 'danger', texto: "No se pudo abrir el currículum" });
      return;
    }
    const blob = await res.blob();
    window.open(URL.createObjectURL(blob), '_blank');
  };
  // Estado para almacenar la información personal del oferente
  const [oferente, setOferente] = useState<OferentePerfil | null>(null);
  // Estado para almacenar la lista de habilidades del oferente
  const [habilidades, setHabilidades] = useState<Habilidad[]>([]);
  // Estado para indicar si se está cargando datos desde el backend
  const [cargando, setCargando] = useState(true);

  // Effect para cargar el detalle del oferente y sus habilidades al montar el componente
  useEffect(() => {
    // Solo proceder si el usuario es empresa, existe un ID de oferente y un ID de puesto
    if (!id || !puestoId) return;
    fetch(`http://localhost:8080/api/empresa/candidatos/${id}?puestoId=${puestoId}`, { headers: new Headers({ "Authorization": "Bearer " + localStorage.getItem("token") }) })
      .then(async (res) => { if (res.ok) { const data = await res.json(); setOferente(data.oferente); setHabilidades(data.habilidades || []); } else throw new Error("No se pudo cargar el detalle del candidato"); })
      .catch((e: Error) => onMensaje({ tipo: 'danger', texto: e.message }))
      .finally(() => setCargando(false));
  }, [id, puestoId, onMensaje]);

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
                  <button className="btn btn-dark btn-sm" onClick={() => descargar(`http://localhost:8080/api/empresa/candidatos/${id}/cv`)}>Ver CV</button>
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
            onClick={() => navigate(puestoId ? `/empresa/puestos/${puestoId}/candidatos` : '/empresa/puestos')}>
            Volver
          </button>
        </>
      )}
    </section>
  );
}

export default EmpresaDetalleCandidatoPage;
