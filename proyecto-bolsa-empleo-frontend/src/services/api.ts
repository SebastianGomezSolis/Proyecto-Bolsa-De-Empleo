// Servicio central para comunicarse con el API backend.
// Este archivo contiene todas las funciones que hacen peticiones HTTP al backend
// y manejan la autenticación mediante tokens JWT almacenados en localStorage.

import { Sesion, Puesto, Caracteristica, Nacionalidad, EmpresaPendiente, OferentePendiente, CaracteristicasAdminResponse, CaracteristicasOferenteResponse, PuestosResponse, Habilidad, OferentePerfil, Candidato } from '../types';
import { obtenerToken } from './authService';

// URL base del backend (localhost:8080 es donde corre el Spring Boot por defecto)
export const API_BASE = 'http://localhost:8080';
// Base de todas las rutas de API (todos los endpoints empiezan con /api)
export const BASE_API = `${API_BASE}/api`;

// Función genérica para hacer peticiones HTTP al backend.
// Maneja automáticamente la inclusión del token JWT en los headers y el parsing de la respuesta JSON.
async function solicitar<T>(ruta: string, opciones: RequestInit = {}): Promise<T> {
  // Obtener el token JWT del localStorage (si existe)
  const token = obtenerToken();

  // Configurar headers básicos: Content-Type JSON y token de autorización si existe
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(opciones.headers as Record<string, string> || {}),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  // Hacer la petición HTTP al backend
  const respuesta = await fetch(`${BASE_API}${ruta}`, { ...opciones, headers });
  const texto = await respuesta.text();
  let datos: unknown = null;
  try { 
    // Intentar parsear la respuesta como JSON
    datos = texto ? JSON.parse(texto) : null; 
  } catch { 
    // Si falla el parsing, mantener como texto plano
    datos = texto; 
  }

  // Si la respuesta no es exitosa (código HTTP >= 400), lanzar error
  if (!respuesta.ok) {
    const mensaje = typeof datos === 'string' ? datos : (datos as any)?.message || 'Ocurrió un error en la solicitud.';
    throw new Error(mensaje);
  }

  // Retornar los datos tipados
  return datos as T;
}

/**
 * Función especializada para hacer peticiones con FormData (usada para subir archivos).
 * Similar a solicitar() pero maneja FormData en lugar de JSON.
 * 
 * @param ruta - El endpoint al que se quiere hacer la petición
 * @param formData - Objeto FormData con los datos a enviar (usualmente un archivo)
 * @returns Promise que resuelve con los datos de la respuesta tipados como T
 */
async function solicitarFormData<T>(ruta: string, formData: FormData): Promise<T> {
  const token = obtenerToken();

  // Para FormData no se especifica Content-Type (el navegador lo maneja automáticamente)
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

// Objeto que contiene todas las funciones de API organizadas por módulo
export const api = {
  // ─── Auth ────────────────────────────────────────────────────────────────
  // Endpoints relacionados con autenticación y registro de usuarios

  // POST /auth/login
  // Inicia sesión con correo y contraseña, retorna los datos de la sesión
  login: (carga: { correo: string; clave: string }) =>
    solicitar<Sesion>('/auth/login', { method: 'POST', body: JSON.stringify(carga) }),

  // POST /auth/logout
  // Cierra la sesión del usuario actual
  logout: () =>
    solicitar<string>('/auth/logout', { method: 'POST' }),

  // POST /auth/registro/empresa
  // Registra una nueva empresa (requiere aprobación de admin)
  registrarEmpresa: (carga: Record<string, unknown>) =>
    solicitar<string>('/auth/registro/empresa', { method: 'POST', body: JSON.stringify(carga) }),

  // POST /auth/registro/oferente
  // Registra un nuevo oferente (requiere aprobación de admin)
  registrarOferente: (carga: Record<string, unknown>) =>
    solicitar<string>('/auth/registro/oferente', { method: 'POST', body: JSON.stringify(carga) }),

  // GET /auth/sesion
  // Obtiene la información de la sesión actual
  sesion: () =>
    solicitar<Sesion>('/auth/sesion'),

  // ─── Público ─────────────────────────────────────────────────────────────
  // Endpoints accesibles sin autenticación (visitantes)

  // GET /publico/puestos/ultimos
  // Obtiene los últimos 5 puestos públicos ordenados por fecha de publicación
  getUltimosPuestosPublicos: () =>
    solicitar<PuestosResponse>('/publico/puestos/ultimos'),

  // GET /publico/puestos
  // Obtiene todos los puestos públicos activos
  getPuestosPublicos: () =>
    solicitar<PuestosResponse>('/publico/puestos'),

  // GET /publico/puestos/buscar?caracteristicaIds=...
  // Busca puestos públicos que coincidan con las características especificadas
  buscarPuestosPublicos: (caracteristicaIds: number[]) => {
    const params = caracteristicaIds?.length
      ? '?' + caracteristicaIds.map((id) => `caracteristicaIds=${id}`).join('&')
      : '';
    return solicitar<PuestosResponse>(`/publico/puestos/buscar${params}`);
  },

  // GET /publico/nacionalidades
  // Obtiene la lista de todas las nacionalidades disponibles
  getNacionalidades: () =>
    solicitar<Nacionalidad[]>('/publico/nacionalidades'),

  // GET /publico/caracteristicas
  // Obtiene las características raíz del árbol (sin padre)
  getCaracteristicasRaiz: () =>
    solicitar<Caracteristica[]>('/publico/caracteristicas'),

  // GET /publico/caracteristicas?padreId=...
  // Obtiene las características hijas de una característica padre específica
  getCaracteristicasHijos: (padreId: number) =>
    solicitar<Caracteristica[]>(`/publico/caracteristicas?padreId=${padreId}`),

  // ─── Empresa ─────────────────────────────────────────────────────────────
  // Endpoints accesibles solo para usuarios con rol EMPRESA

  // GET /empresa/perfil
  // Obtiene el perfil de la empresa actualmente logueada
  getPerfilEmpresa: () =>
    solicitar<Record<string, unknown>>('/empresa/perfil'),

  // GET /empresa/puestos
  // Obtiene todos los puestos pertenecientes a la empresa logueada
  getPuestosEmpresa: () =>
    solicitar<Puesto[]>('/empresa/puestos'),

  // POST /empresa/puestos
  // Crea un nuevo puesto de trabajo con sus características y niveles requeridos
  crearPuesto: (carga: Record<string, unknown>) =>
    solicitar<string>('/empresa/puestos', { method: 'POST', body: JSON.stringify(carga) }),

  // POST /empresa/puestos/{id}/desactivar
  // Desactiva un puesto para ocultarlo de las búsquedas públicas
  desactivarPuesto: (id: number) =>
    solicitar<string>(`/empresa/puestos/${id}/desactivar`, { method: 'POST' }),

  // POST /empresa/puestos/{id}/activar
  // Reactiva un puesto previamente desactivado
  activarPuesto: (id: number) =>
    solicitar<string>(`/empresa/puestos/${id}/activar`, { method: 'POST' }),

  // GET /empresa/puestos/{puestoId}/candidatos
  // Obtiene la lista de candidatos compatibles con un puesto específico
  getCandidatosPuesto: (puestoId: number) =>
    solicitar<Candidato[]>(`/empresa/puestos/${puestoId}/candidatos`),

  // GET /empresa/candidatos/{id}?puestoId=...
  // Obtiene el detalle completo de un candidato específico para un puesto
  getDetalleOferente: (id: number, puestoId: number) =>
    solicitar<{ oferente: OferentePerfil; habilidades: Habilidad[] }>(`/empresa/candidatos/${id}?puestoId=${puestoId}`),

  // ─── Oferente ────────────────────────────────────────────────────────────
  // Endpoints accesibles solo para usuarios con rol OFERENTE

  // GET /oferente/perfil
  // Obtiene el perfil del oferente actualmente logueado
  getPerfilOferente: () =>
    solicitar<OferentePerfil>('/oferente/perfil'),

  // GET /oferente/habilidades
  // Obtiene todas las habilidades registradas por el oferente logueado
  getHabilidades: () =>
    solicitar<Habilidad[]>('/oferente/habilidades'),

  // POST /oferente/habilidades
  // Agrega una nueva habilidad al perfil del oferente
  agregarHabilidad: (carga: { caracteristicaId: number; nivel: number }) =>
    solicitar<string>('/oferente/habilidades', { method: 'POST', body: JSON.stringify(carga) }),

  // DELETE /oferente/habilidades/{id}
  // Elimina una habilidad del perfil del oferente
  eliminarHabilidad: (id: number) =>
    solicitar<string>(`/oferente/habilidades/${id}`, { method: 'DELETE' }),

  // GET /oferente/puestos/buscar?caracteristicaIds=...
  // Busca puestos (públicos y privados) que coincidan con las características seleccionadas
  buscarPuestosOferente: (caracteristicaIds: number[]) => {
    const params = caracteristicaIds?.length
      ? '?' + caracteristicaIds.map((id) => `caracteristicaIds=${id}`).join('&')
      : '';
    return solicitar<PuestosResponse>(`/oferente/puestos/buscar${params}`);
  },

  // GET /oferente/caracteristicas?actualId=...
  // Obtiene el árbol de características navegable para seleccionar habilidades
  getCaracteristicasOferente: (actualId?: number) =>
    solicitar<CaracteristicasOferenteResponse>(`/oferente/caracteristicas${actualId != null ? `?actualId=${actualId}` : ''}`),

  // POST /oferente/cv/subir
  // Sube el curriculum (CV) del oferente en formato PDF
  subirCV: (archivo: File) => {
    const fd = new FormData();
    fd.append('archivo', archivo);
    return solicitarFormData<string>('/oferente/cv/subir', fd);
  },

  // ─── Admin ───────────────────────────────────────────────────────────────
  // Endpoints accesibles solo para usuarios con rol ADMIN

  // GET /admin/empresas/pendientes
  // Obtiene la lista de empresas pendientes de aprobación
  getEmpresasPendientes: () =>
    solicitar<EmpresaPendiente[]>('/admin/empresas/pendientes'),

  // POST /admin/empresas/{id}/autorizar
  // Autoriza a una empresa para que pueda usar el sistema
  autorizarEmpresa: (id: number) =>
    solicitar<string>(`/admin/empresas/${id}/autorizar`, { method: 'POST' }),

  // GET /admin/oferentes/pendientes
  // Obtiene la lista de oferentes pendientes de aprobación
  getOferentesPendientes: () =>
    solicitar<OferentePendiente[]>('/admin/oferentes/pendientes'),

  // POST /admin/oferentes/{id}/autorizar
  // Autoriza a un oferente para que pueda usar el sistema
  autorizarOferente: (id: number) =>
    solicitar<string>(`/admin/oferentes/${id}/autorizar`, { method: 'POST' }),

  // GET /admin/caracteristicas?actualId=...
  // Obtiene el árbol de características navegable para administración
  getCaracteristicasAdmin: (actualId?: number) =>
    solicitar<CaracteristicasAdminResponse>(`/admin/caracteristicas${actualId != null ? `?actualId=${actualId}` : ''}`),

  // POST /admin/caracteristicas
  // Crea una nueva característica en el árbol jerárquico
  crearCaracteristica: (carga: { nombre: string; padreId: number | null }) =>
    solicitar<string>('/admin/caracteristicas', { method: 'POST', body: JSON.stringify(carga) }),
};