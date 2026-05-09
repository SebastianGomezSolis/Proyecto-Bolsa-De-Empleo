import { Sesion, Puesto, Caracteristica, Nacionalidad, EmpresaPendiente, OferentePendiente, CaracteristicasAdminResponse, CaracteristicasOferenteResponse, PuestosResponse, Habilidad, OferentePerfil, Candidato } from '../types';
import { obtenerToken } from './authService';

export const API_BASE = 'http://localhost:8080';
export const BASE_API = `${API_BASE}/api`;

async function solicitar<T>(ruta: string, opciones: RequestInit = {}): Promise<T> {
  const token = obtenerToken();

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(opciones.headers as Record<string, string> || {}),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const respuesta = await fetch(`${BASE_API}${ruta}`, { ...opciones, headers });
  const texto = await respuesta.text();
  let datos: unknown = null;
  try { datos = texto ? JSON.parse(texto) : null; } catch { datos = texto; }

  if (!respuesta.ok) {
    const mensaje = typeof datos === 'string' ? datos : (datos as any)?.message || 'Ocurrió un error en la solicitud.';
    throw new Error(mensaje);
  }

  return datos as T;
}

async function solicitarFormData<T>(ruta: string, formData: FormData): Promise<T> {
  const token = obtenerToken();

  const headers: Record<string, string> = {};
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const respuesta = await fetch(`${BASE_API}${ruta}`, { method: 'POST', body: formData, headers });
  const texto = await respuesta.text();
  let datos: unknown = null;
  try { datos = texto ? JSON.parse(texto) : null; } catch { datos = texto; }

  if (!respuesta.ok) {
    const mensaje = typeof datos === 'string' ? datos : (datos as any)?.message || 'Error al subir archivo.';
    throw new Error(mensaje);
  }

  return datos as T;
}

export const api = {
  // ─── Auth ────────────────────────────────────────────────────────────────
  login: (carga: { correo: string; clave: string }) =>
    solicitar<Sesion>('/auth/login', { method: 'POST', body: JSON.stringify(carga) }),

  logout: () =>
    solicitar<string>('/auth/logout', { method: 'POST' }),

  registrarEmpresa: (carga: Record<string, unknown>) =>
    solicitar<string>('/auth/registro/empresa', { method: 'POST', body: JSON.stringify(carga) }),

  registrarOferente: (carga: Record<string, unknown>) =>
    solicitar<string>('/auth/registro/oferente', { method: 'POST', body: JSON.stringify(carga) }),

  sesion: () =>
    solicitar<Sesion>('/auth/sesion'),

  // ─── Público ─────────────────────────────────────────────────────────────
  getUltimosPuestosPublicos: () =>
    solicitar<PuestosResponse>('/publico/puestos/ultimos'),

  getPuestosPublicos: () =>
    solicitar<PuestosResponse>('/publico/puestos'),

  buscarPuestosPublicos: (caracteristicaIds: number[]) => {
    const params = caracteristicaIds?.length
      ? '?' + caracteristicaIds.map((id) => `caracteristicaIds=${id}`).join('&')
      : '';
    return solicitar<PuestosResponse>(`/publico/puestos/buscar${params}`);
  },

  getNacionalidades: () =>
    solicitar<Nacionalidad[]>('/publico/nacionalidades'),

  getCaracteristicasRaiz: () =>
    solicitar<Caracteristica[]>('/publico/caracteristicas'),

  getCaracteristicasHijos: (padreId: number) =>
    solicitar<Caracteristica[]>(`/publico/caracteristicas?padreId=${padreId}`),

  // ─── Empresa ─────────────────────────────────────────────────────────────
  getPerfilEmpresa: () =>
    solicitar<Record<string, unknown>>('/empresa/perfil'),

  getPuestosEmpresa: () =>
    solicitar<Puesto[]>('/empresa/puestos'),

  crearPuesto: (carga: Record<string, unknown>) =>
    solicitar<string>('/empresa/puestos', { method: 'POST', body: JSON.stringify(carga) }),

  desactivarPuesto: (id: number) =>
    solicitar<string>(`/empresa/puestos/${id}/desactivar`, { method: 'POST' }),

  activarPuesto: (id: number) =>
    solicitar<string>(`/empresa/puestos/${id}/activar`, { method: 'POST' }),

  getCandidatosPuesto: (puestoId: number) =>
    solicitar<Candidato[]>(`/empresa/puestos/${puestoId}/candidatos`),

  getDetalleOferente: (id: number, puestoId: number) =>
    solicitar<{ oferente: OferentePerfil; habilidades: Habilidad[] }>(`/empresa/candidatos/${id}?puestoId=${puestoId}`),

  // ─── Oferente ────────────────────────────────────────────────────────────
  getPerfilOferente: () =>
    solicitar<OferentePerfil>('/oferente/perfil'),

  getHabilidades: () =>
    solicitar<Habilidad[]>('/oferente/habilidades'),

  agregarHabilidad: (carga: { caracteristicaId: number; nivel: number }) =>
    solicitar<string>('/oferente/habilidades', { method: 'POST', body: JSON.stringify(carga) }),

  eliminarHabilidad: (id: number) =>
    solicitar<string>(`/oferente/habilidades/${id}`, { method: 'DELETE' }),

  buscarPuestosOferente: (caracteristicaIds: number[]) => {
    const params = caracteristicaIds?.length
      ? '?' + caracteristicaIds.map((id) => `caracteristicaIds=${id}`).join('&')
      : '';
    return solicitar<PuestosResponse>(`/oferente/puestos/buscar${params}`);
  },

  getCaracteristicasOferente: (actualId?: number) => {
    const params = actualId != null ? `?actualId=${actualId}` : '';
    return solicitar<CaracteristicasOferenteResponse>(`/oferente/caracteristicas${params}`);
  },

  subirCV: (archivo: File) => {
    const fd = new FormData();
    fd.append('archivo', archivo);
    return solicitarFormData<string>('/oferente/cv/subir', fd);
  },

  // ─── Admin ───────────────────────────────────────────────────────────────
  getEmpresasPendientes: () =>
    solicitar<EmpresaPendiente[]>('/admin/empresas/pendientes'),

  autorizarEmpresa: (id: number) =>
    solicitar<string>(`/admin/empresas/${id}/autorizar`, { method: 'POST' }),

  getOferentesPendientes: () =>
    solicitar<OferentePendiente[]>('/admin/oferentes/pendientes'),

  autorizarOferente: (id: number) =>
    solicitar<string>(`/admin/oferentes/${id}/autorizar`, { method: 'POST' }),

  getCaracteristicasAdmin: (actualId?: number) => {
    const params = actualId != null ? `?actualId=${actualId}` : '';
    return solicitar<CaracteristicasAdminResponse>(`/admin/caracteristicas${params}`);
  },

  crearCaracteristica: (carga: { nombre: string; padreId: number | null }) =>
    solicitar<string>('/admin/caracteristicas', { method: 'POST', body: JSON.stringify(carga) }),
};
