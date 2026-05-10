// Página de inicio pública del sistema. Muestra los últimos puestos publicados.

import { useEffect, useState } from 'react';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';
import CardPuesto from '../../components/CardPuesto';
import { api } from '../../services/api';
import { Sesion, MensajeGlobal, Puesto, PuestosResponse } from '../../types';

interface Props {
  // Sesión actual del usuario (null si no está autenticado)
  sesion: Sesion | null;
  // Función de navegación para redirigir al usuario a otras páginas
  onNavegar: (ruta: string) => void;
  // Función de callback para mostrar mensajes globales (éxito/error)
  onMensaje: (m: MensajeGlobal) => void;
}

function HomePage({ sesion, onNavegar, onMensaje }: Props) {
  // Estado para almacenar los últimos puestos publicados
  const [puestos, setPuestos] = useState<Puesto[]>([]);
  // Estado para indicar si se están cargando los datos desde el backend
  const [cargando, setCargando] = useState(true);
  // Indica si el usuario tiene una sesión activa
  const logueado = Boolean(sesion);

  // Effect que carga los últimos puestos públicos al montar el componente
  useEffect(() => {
    api.getUltimosPuestosPublicos()
      .then((res: PuestosResponse) => {
        setPuestos((res.puestos ?? []).map((p: Puesto) => ({ ...p, tipoCambio: res.tipoCambio })));
      })
      .catch((e: Error) => onMensaje({ tipo: 'danger', texto: e.message }))
      .finally(() => setCargando(false));
  }, [onMensaje]);

  return (
    <>
      {/* Sección de últimos puestos publicados */}
      <section className="container py-5">
        <SectionTitle
          eyebrow="Últimas ofertas"
          title="Puestos recientes"
          description="Un vistazo a las últimas oportunidades laborales publicadas en el sistema."
        />
        {cargando ? <LoadingBlock /> : (
          <div className="row g-3">
            {puestos.length === 0 ? (
              <div className="col-12"><p className="text-secondary">No hay puestos publicados aún.</p></div>
            ) : puestos.map((p) => (
              <div key={p.id} className="col-md-6 col-lg-4">
                <CardPuesto puesto={p} />
              </div>
            ))}
          </div>
        )}
        {!cargando && puestos.length > 0 && !logueado && (
          <div className="mt-4">
            <button className="btn btn-outline-primary" onClick={() => onNavegar('/puestos/buscar')}>Ver todos los puestos</button>
          </div>
        )}
      </section>
    </>
  );
}

export default HomePage;
