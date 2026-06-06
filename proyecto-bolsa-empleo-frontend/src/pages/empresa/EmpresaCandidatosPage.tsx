import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';


interface MensajeGlobal {
  tipo: 'success' | 'error' | 'info' | 'warning' | 'danger';
  texto: string;
}

interface Puesto {
  id: number;
  descripcion: string;
  salario: number;
  tipoPublicacion: string;
  empresa: { id: number; nombre: string; usuarioCorreo: string };
  activo: boolean;
  fechaRegistro: string;
  caracteristicas: { id: number; nombre: string; nivelRequerido: number }[];
  tipoCambio?: { compra: number; venta: number; fecha: string };
}

interface Candidato {
  oferente: { id: number; nombre: string; primerApellido: string };
  similitud: number;
  porcentaje: number;
  requisitosCumplidos: number;
  totalRequisitos: number;
}

interface Props {
  // Función de callback para mostrar mensajes globales (éxito/error)
  onMensaje: (m: MensajeGlobal) => void;
}

// Componente principal para mostrar la lista de candidatos de un puesto específico
function EmpresaCandidatosPage({ onMensaje }: Props) {
  const navigate = useNavigate();
  const { puestoId } = useParams();
  // Estado para almacenar la lista de candidatos del puesto
  const [candidatos, setCandidatos] = useState<Candidato[]>([]);
  // Estado para almacenar la información del puesto actual
  const [puesto, setPuesto] = useState<Puesto | null>(null);
  // Estado para indicar si se está cargando datos desde el backend
  const [cargando, setCargando] = useState(true);

  // Effect para cargar los datos del puesto y sus candidatos al montar el componente
  useEffect(() => {
    // Solo proceder si el usuario es empresa y existe un ID de puesto
    if (!puestoId) return;
    const cargar = async () => {
      try {
        // Obtener los puestos de la empresa y los candidatos del puesto en paralelo
        const [puestosResp, candsResp] = await Promise.all([
          fetch("http://localhost:8080/api/empresa/puestos", { headers: new Headers({ "Authorization": "Bearer " + localStorage.getItem("token") }) }),
          fetch(`http://localhost:8080/api/empresa/puestos/${puestoId}/candidatos`, { headers: new Headers({ "Authorization": "Bearer " + localStorage.getItem("token") }) }),
        ]);
        if (!puestosResp.ok) {
          throw new Error("No se pudo cargar la información del puesto");
        }

        if (!candsResp.ok) {
          throw new Error("No se pudieron cargar los candidatos");
        }
        const puestos = await puestosResp.json();
        const cands = await candsResp.json();
        // Buscar el puesto actual dentro de la lista de puestos
        setPuesto((puestos as Puesto[]).find((p) => p.id === Number(puestoId)) || null);
        // Normalizar la respuesta (puede venir como array o como objeto con propiedad candidatos)
        const lista = Array.isArray(cands) ? cands : (cands as any)?.candidatos ?? [];
        setCandidatos(lista);
      } catch (e) {
        onMensaje({ tipo: 'danger', texto: (e as Error).message });
      } finally {
        setCargando(false);
      }
    };
    cargar();
  }, [puestoId, onMensaje]);

  return (
    <section className="container py-5">
      <SectionTitle eyebrow="Empresa" title="Candidatos para el puesto" />
      {/* Mostrar la descripción del puesto si está disponible */}
      {puesto && <p className="mb-4"><strong>Puesto:</strong> {puesto.descripcion}</p>}

      {cargando ? <LoadingBlock /> : (
        <>
          {/* Tabla de candidatos con su nivel de coincidencia */}
          <table className="table table-hover">
            <thead>
              <tr>
                <th>Oferente</th>
                <th>Requisitos cumplidos</th>
                <th>% Coincidencia</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {/* Mostrar mensaje si no hay candidatos */}
              {candidatos.length === 0 ? (
                <tr><td colSpan={4} className="text-center text-muted">No se encontraron candidatos.</td></tr>
              ) : candidatos.map((c) => (
                <tr key={c.oferente?.id}>
                  <td>{c.oferente?.nombre} {c.oferente?.primerApellido}</td>
                  <td>{c.requisitosCumplidos} / {c.totalRequisitos}</td>
                  <td>{Number(c.porcentaje).toFixed(2)}%</td>
                  <td>
                    {/* Botón para ver el detalle completo del candidato */}
                    <button className="btn btn-outline-dark btn-sm"
                      onClick={() => navigate(`/empresa/candidatos/${c.oferente?.id}/puesto/${puestoId}`)}>
                      Ver detalle
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {/* Botón para volver a la lista de puestos */}
          <button className="btn btn-dark btn-sm" onClick={() => navigate('/empresa/puestos')}>Volver</button>
        </>
      )}
    </section>
  );
}

export default EmpresaCandidatosPage;
