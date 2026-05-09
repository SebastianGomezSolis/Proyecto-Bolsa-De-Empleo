import { useEffect, useState } from 'react';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';
import { api } from '../../services/api';
import { Sesion, MensajeGlobal, Caracteristica, Habilidad } from '../../types';

interface Props {
  sesion: Sesion | null;
  onNavegar: (ruta: string) => void;
  onMensaje: (m: MensajeGlobal) => void;
}

function OferenteHabilidadesPage({ sesion, onNavegar, onMensaje }: Props) {
  const [habilidades, setHabilidades] = useState<Habilidad[]>([]);
  const [subcategorias, setSubcategorias] = useState<Caracteristica[]>([]);
  const [ruta, setRuta] = useState<Caracteristica[]>([]);
  const [actual, setActual] = useState<Caracteristica | null>(null);
  const [selId, setSelId] = useState<number | null>(null);
  const [nivel, setNivel] = useState(1);
  const [cargando, setCargando] = useState(true);
  const [navegando, setNavegando] = useState(false);
  const [hojas, setHojas] = useState<Set<number>>(new Set());

  const cargarHabilidades = () =>
    api.getHabilidades()
      .then(setHabilidades)
      .catch((e: Error) => onMensaje({ tipo: 'danger', texto: e.message }));

  const cargarSubcategorias = async (padreId: number | null = null) => {
    try {
      const data = padreId ? await api.getCaracteristicasHijos(padreId) : await api.getCaracteristicasRaiz();
      setSubcategorias(data);
      const nuevasHojas = new Set(hojas);
      await Promise.all(data.map(async (n) => {
        const hijos = await api.getCaracteristicasHijos(n.id);
        if (hijos.length === 0) nuevasHojas.add(n.id);
        else nuevasHojas.delete(n.id);
      }));
      setHojas(nuevasHojas);
    } catch (e) {
      onMensaje({ tipo: 'danger', texto: (e as Error).message });
    }
  };

  useEffect(() => {
    if (!sesion || sesion.rol !== 'OFERENTE') return;
    Promise.all([api.getHabilidades(), api.getCaracteristicasRaiz()])
      .then(([h, r]) => { setHabilidades(h); setSubcategorias(r); })
      .catch((e: Error) => onMensaje({ tipo: 'danger', texto: e.message }))
      .finally(() => setCargando(false));
  }, [sesion, onMensaje]);

  if (!sesion || sesion.rol !== 'OFERENTE') {
    return (
      <section className="container py-5">
        <div className="alert alert-warning">Acceso restringido a oferentes autorizados.</div>
        <button className="btn btn-outline-secondary" onClick={() => onNavegar('/')}>Volver</button>
      </section>
    );
  }

  const entrar = async (sub: Caracteristica) => {
    if (navegando) return;
    setNavegando(true);
    setActual(sub);
    setRuta((prev) => [...prev, sub]);
    setSelId(null);
    await cargarSubcategorias(sub.id);
    setNavegando(false);
  };

  const irARaiz = async () => {
    setActual(null);
    setRuta([]);
    setSelId(null);
    await cargarSubcategorias(null);
  };

  const irANodo = async (nodo: Caracteristica, idx: number) => {
    const nuevaRuta = ruta.slice(0, idx + 1);
    setRuta(nuevaRuta);
    setActual(nodo);
    setSelId(null);
    await cargarSubcategorias(nodo.id);
  };

  const seleccionar = (sub: Caracteristica) => setSelId(sub.id);

  const agregar = async (e: React.FormEvent) => {
    e.preventDefault();
    const id = selId || actual?.id;
    if (!id) return;
    try {
      await api.agregarHabilidad({ caracteristicaId: Number(id), nivel: Number(nivel) });
      onMensaje({ tipo: 'success', texto: 'Habilidad agregada.' });
      setSelId(null);
      setNivel(1);
      cargarHabilidades();
    } catch (e) {
      onMensaje({ tipo: 'danger', texto: (e as Error).message });
    }
  };

  const eliminar = async (id: number) => {
    if (!window.confirm('¿Eliminar esta habilidad?')) return;
    try {
      await api.eliminarHabilidad(id);
      onMensaje({ tipo: 'success', texto: 'Habilidad eliminada.' });
      cargarHabilidades();
    } catch (e) {
      onMensaje({ tipo: 'danger', texto: (e as Error).message });
    }
  };

  const esHoja = (sub: Caracteristica) => hojas.has(sub.id);

  return (
    <section className="container py-5">
      <SectionTitle eyebrow="Oferente" title="Mis habilidades" />

      {cargando ? <LoadingBlock /> : (
        <div className="row g-4">
          <div className="col-md-4">
            <table className="table table-hover table-sm">
              <thead>
                <tr><th>Característica</th><th>Nivel</th><th></th></tr>
              </thead>
              <tbody>
                {habilidades.length === 0 ? (
                  <tr><td colSpan={3} className="text-muted text-center">Sin habilidades.</td></tr>
                ) : habilidades.map((h) => (
                  <tr key={h.id}>
                    <td>{h.caracteristica?.nombre}</td>
                    <td>{h.nivel}</td>
                    <td>
                      <button className="btn btn-sm btn-outline-danger" onClick={() => eliminar(h.id)}>Quitar</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="col-md-4">
            <div className="mb-2">
              <span className="fw-semibold">Ruta:</span>
              <span className="ms-1">
                <button className="badge text-bg-secondary text-decoration-none border-0" onClick={irARaiz}>Raíces</button>
                {ruta.map((nodo, idx) => (
                  <span key={nodo.id}>
                    <span className="text-muted mx-1">/</span>
                    <button className="badge text-bg-secondary text-decoration-none border-0" onClick={() => irANodo(nodo, idx)}>
                      {nodo.nombre}
                    </button>
                  </span>
                ))}
              </span>
            </div>
            {actual && (
              <div className="mb-2 text-muted" style={{ fontSize: '0.9rem' }}>
                Subcategorías de: <strong>{actual.nombre}</strong>
              </div>
            )}
            {subcategorias.map((sub) => (
              <div key={sub.id} className="d-flex justify-content-between align-items-center border rounded px-3 py-2 mb-2 bg-white">
                <span>{sub.nombre}</span>
                {esHoja(sub) ? (
                  <button className={`btn btn-sm ${selId === sub.id ? 'btn-dark' : 'btn-outline-dark'}`} onClick={() => seleccionar(sub)}>
                    {selId === sub.id ? 'Seleccionado' : 'Selec.'}
                  </button>
                ) : (
                  <button className="btn btn-outline-secondary btn-sm" disabled={navegando} onClick={() => entrar(sub)}>Entrar</button>
                )}
              </div>
            ))}
            {subcategorias.length === 0 && (
              <div className="text-muted" style={{ fontSize: '0.9rem' }}>No hay subcategorías.</div>
            )}
          </div>

          <div className="col-md-4">
            <div className="border rounded p-3 bg-white">
              <h6 className="fw-bold mb-3">Agregar Habilidad</h6>
              <form onSubmit={agregar}>
                <div className="mb-3">
                  <label className="form-label">Característica</label>
                  <select className="form-select" value={selId || ''} onChange={(e) => setSelId(Number(e.target.value))} required>
                    <option value="" disabled>Seleccione...</option>
                    {subcategorias.filter(esHoja).map((sub) => (
                      <option key={sub.id} value={sub.id}>{sub.nombre}</option>
                    ))}
                  </select>
                </div>
                <div className="mb-3">
                  <label className="form-label">Nivel (1-5)</label>
                  <input type="number" className="form-control" min={1} max={5} value={nivel}
                    onChange={(e) => setNivel(Number(e.target.value))} required />
                </div>
                <button type="submit" className="btn btn-primary w-100">Agregar</button>
              </form>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}

export default OferenteHabilidadesPage;
