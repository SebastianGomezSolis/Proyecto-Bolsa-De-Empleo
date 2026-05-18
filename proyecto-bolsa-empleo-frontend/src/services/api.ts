import { Sesion } from '../types';

const esProduccion = !window.location.origin.includes('localhost:3000') && !window.location.origin.includes('localhost:5173');
export const API_BASE = esProduccion ? '' : 'http://localhost:8080';
export const BASE_API = `${API_BASE}/api`;

const CLAVE_SESION = 'bolsa.session';

export function obtenerSesionGuardada(): Sesion | null {
  const raw = sessionStorage.getItem(CLAVE_SESION);
  if (!raw) return null;
  try { return JSON.parse(raw) as Sesion; }
  catch { sessionStorage.removeItem(CLAVE_SESION); return null; }
}

export function guardarSesion(datos: Sesion): void {
  sessionStorage.setItem(CLAVE_SESION, JSON.stringify(datos));
}

export function limpiarSesion(): void {
  sessionStorage.removeItem(CLAVE_SESION);
}

export function obtenerToken(): string | null {
  return obtenerSesionGuardada()?.token ?? null;
}

export function getAuthHeaders(): Record<string, string> {
  const token = obtenerToken();
  const headers: Record<string, string> = {};
  if (token) headers['Authorization'] = `Bearer ${token}`;
  return headers;
}
