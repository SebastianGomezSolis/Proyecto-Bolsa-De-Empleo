import { useCallback, useEffect, useState } from 'react';
import SectionTitle from '../../components/SectionTitle';
import LoadingBlock from '../../components/LoadingBlock';
import { api } from '../../services/api';
import { Sesion, MensajeGlobal, Caracteristica } from '../../types';

interface Props {
  sesion: Sesion | null;
  onNavegar: (ruta: string) => void;
  onMensaje: (m: MensajeGlobal) => void;
}

function AdminCaracteristicasPage({ sesion, onNavegar, onMensaje }: Props) {
  const [subcategorias, setSubcategorias] = useState<Caracteristica[]>([]);
  const [ruta, setRuta] = useState<Caracteristica[]>([]);
  const [actual, setActual] = useState<Caracteristica | null>(null);
  const [nombre, setNombre] = useState('');
  const [todas, setTodas] = useState<Caracteristica[]>([]);
  const [cargando, setCargando] = useState(true);
  const [navegando, setNavegando] = useState(false);

  const cargarNodo = useCallback(async (actualId: number | null = null) => {
    try {
      const resp = await api.getCaracteristicasAdmin(actualId ?? undefined);
      setSubcategorias(resp.subcategorias ?? []);
      setActual(resp.actual ?? null);
      setRuta(resp.ruta ?? []);
      setTodas(resp.todas ?? []);
    } catch (e) {
      onMensaje({ tipo: 'danger', texto: (e as Error).message });
    }
  }, [onMensaje]);

  useEffect(() => {
    let cancelled = false;

    (async () => {
      if (!sesion || sesion.rol !== 'ADMIN') return;
      await Promise.resolve();
      if (cancelled) return;

      setCargando(true);
      try {
        await cargarNodo(null);
      } finally {
        if (!cancelled) setCargando(false);
      }
    })();

    return () => { cancelled = true; };
  }, [sesion, cargarNodo]);

  if (!sesion || sesion.rol !== 'ADMIN') {
    return (
      <section className="container py-5">
        <div className="alert alert-danger">Acceso restringido a administradores.</div>
        <button className="btn btn-outline-secondary" onClick={() => onNavegar('/')}>Volver</button>
      </section>
    );
  }

  const entrar = async (sub: Caracteristica) => {
    if (navegando) return;
    setNavegando(true);
    setNombre('');
    await cargarNodo(sub.id);
    setNavegando(false);
  };

  const irARaiz = async () => {
    setNombre('');
    await cargarNodo(null);
  };

  const irANodo = async (nodo: Caracteristica) => {
    setNombre('');
    await cargarNodo(nodo.id);
  };

  const crear = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!nombre.trim()) return;
    try {
      await api.crearCaracteristica({ nombre: nombre.trim(), padreId: actual?.id ?? null });
      onMensaje({ tipo: 'success', texto: 'Característica creada.' });
      setNombre('');
      await cargarNodo(actual?.id ?? null);
    } catch (e) {
      onMensaje({ tipo: 'danger', texto: (e as Error).message });
    }
  };

  return (
    <section className="container py-5">
      <SectionTitle eyebrow="Administración" title="Características" />

      {cargando ? <LoadingBlock /> : (
        <div className="row g-4">
          <div className="col-md-6">
            <div className="border rounded p-3 bg-white">
              <div className="mb-2">
                <span className="fw-semibold">Ruta:</span>
                <span className="ms-1">
                  <button className="badge text-bg-light border text-dark border-0 bg-transparent"
                    style={{ cursor: 'pointer' }} onClick={irARaiz}>Raíces</button>
                  {ruta.map((nodo) => (
                    <span key={nodo.id}>
                      <span className="text-muted mx-1">/</span>
                      <button className="badge text-bg-light border text-dark border-0 bg-transparent"
                        style={{ cursor: 'pointer' }} onClick={() => irANodo(nodo)}>
                        {nodo.nombre}
                      </button>
                    </span>
                  ))}
                </span>
              </div>
              <p className="text-muted mb-3" style={{ fontSize: '0.88rem' }}>
                Subcategorías de: <strong>{actual ? actual.nombre : 'raíces'}</strong>
              </p>
              {subcategorias.map((sub) => (
                <div key={sub.id} className="d-flex justify-content-between align-items-center border rounded px-3 py-2 mb-2 bg-light">
                  <span className="fw-semibold">{sub.nombre}</span>
                  <button className="btn btn-outline-secondary btn-sm" disabled={navegando} onClick={() => entrar(sub)}>Entrar</button>
                </div>
              ))}
              {subcategorias.length === 0 && (
                <div className="text-muted" style={{ fontSize: '0.88rem' }}>Esta categoría no tiene subcategorías.</div>
              )}
            </div>
          </div>

          <div className="col-md-4">
            <div className="border rounded p-3 bg-white">
              <h6 className="fw-bold mb-3">Agregar Característica</h6>
              <form onSubmit={crear}>
                <div className="mb-3">
                  <label className="form-label">Nombre</label>
                  <input type="text" className="form-control" value={nombre}
                    onChange={(e) => setNombre(e.target.value)} required />
                </div>
                <div className="mb-3">
                  <label className="form-label">Padre</label>
                  <select className="form-select" value={actual?.id ?? ''}
                    onChange={(e) => {
                      const val = e.target.value;
                      if (!val) { setActual(null); }
                      else {
                        const found = todas.find((c) => c.id === Number(val));
                        if (found) setActual(found);
                      }
                    }}>
                    <option value="">(sin padre — raíz)</option>
                    {todas.map((c) => (
                      <option key={c.id} value={c.id}>{c.nombre}</option>
                    ))}
                  </select>
                </div>
                <button type="submit" className="btn btn-dark">Crear</button>
              </form>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}

export default AdminCaracteristicasPage;
