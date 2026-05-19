import { Sesion } from '../types';

// Servicio centralizado de comunicacion con el backend.
// Proporciona la URL base, manejo de sesion en sessionStorage
// y helpers para incluir el token JWT en las peticiones.

const esProduccion = !window.location.origin.includes('localhost:3000');
export const API_BASE = esProduccion ? '' : 'http://localhost:8080';
export const BASE_API = `${API_BASE}/api`;

const CLAVE_SESION = 'bolsa.session';

// Recupera la sesion guardada en sessionStorage (si existe)
export function obtenerSesionGuardada(): Sesion | null {
  const raw = sessionStorage.getItem(CLAVE_SESION);
  if (!raw) return null;
  try { return JSON.parse(raw) as Sesion; }
  catch { sessionStorage.removeItem(CLAVE_SESION); return null; }
}

// Guarda los datos de sesion en sessionStorage
export function guardarSesion(datos: Sesion): void {
  sessionStorage.setItem(CLAVE_SESION, JSON.stringify(datos));
}

// Elimina la sesion del sessionStorage (logout)
export function limpiarSesion(): void {
  sessionStorage.removeItem(CLAVE_SESION);
}

// Obtiene solo el token JWT de la sesion guardada
export function obtenerToken(): string | null {
  return obtenerSesionGuardada()?.token ?? null;
}

// Construye los headers de autenticacion con el token JWT
// Formato: Authorization: Bearer <token>
export function getAuthHeaders(): Record<string, string> {
  const token = obtenerToken();
  const headers: Record<string, string> = {};
  if (token) headers['Authorization'] = `Bearer ${token}`;
  return headers;
}

// Descarga un PDF abriendolo en una nueva pestana
export async function descargarPDF(url: string): Promise<void> {
  const res = await fetch(url, { headers: getAuthHeaders() });
  if (!res.ok) throw new Error(await res.text());
  const blob = await res.blob();
  const blobUrl = URL.createObjectURL(blob);
  window.open(blobUrl, '_blank');
  setTimeout(() => URL.revokeObjectURL(blobUrl), 30000);
}
