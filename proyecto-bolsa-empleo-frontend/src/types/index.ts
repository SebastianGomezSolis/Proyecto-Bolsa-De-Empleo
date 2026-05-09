export type Rol = 'ADMIN' | 'EMPRESA' | 'OFERENTE';

export interface Sesion {
  correo: string;
  rol: Rol;
  token?: string;
}

export interface MensajeGlobal {
  tipo: 'success' | 'danger' | 'error' | 'warning' | 'info';
  texto: string;
}

export interface TipoCambio {
  venta: number;
  compra: number;
}

export interface EmpresaSimple {
  id: number;
  nombre: string;
}

export interface Puesto {
  id: number;
  descripcion: string;
  salario: number;
  tipoPublicacion: 'publico' | 'privado';
  activo: boolean;
  fechaRegistro: string;
  empresa?: EmpresaSimple;
  caracteristicas?: PuestoCaracteristica[];
  tipoCambio?: TipoCambio | null;
}

export interface PuestoCaracteristica {
  id: number;
  caracteristica: { id: number; nombre: string };
  nivelRequerido: number;
}

export interface Caracteristica {
  id: number;
  nombre: string;
  hijos?: Caracteristica[];
}

export interface Nacionalidad {
  iso: string;
  nombre: string;
}

export interface EmpresaPendiente {
  id: number;
  nombre: string;
  usuario: { correo: string };
}

export interface OferentePendiente {
  id: number;
  nombre: string;
  primerApellido: string;
  usuario: { correo: string };
}

export interface OferentePerfil {
  id: number;
  nombre: string;
  primerApellido: string;
  identificacion: string;
  usuario: { correo: string };
  telefono: string;
  lugarResidencia: string;
  curriculum: boolean | string;
}

export interface Habilidad {
  id: number;
  caracteristica: { id: number; nombre: string };
  nivel: number;
}

export interface Candidato {
  oferente: { id: number; nombre: string; primerApellido: string };
  requisitosCumplidos: number;
  totalRequisitos: number;
  porcentaje: number;
}

export interface PuestosResponse {
  puestos: Puesto[];
  tipoCambio?: TipoCambio | null;
  raices?: Caracteristica[];
  caracteristicaIds?: number[];
}

export interface CaracteristicasOferenteResponse {
  subcategorias: Caracteristica[];
  actual: Caracteristica | null;
}

export interface CaracteristicasAdminResponse {
  subcategorias: Caracteristica[];
  actual: Caracteristica | null;
  ruta: Caracteristica[];
  todas: Caracteristica[];
}

export interface FormEmpresa {
  correo: string;
  clave: string;
  nombre: string;
  localizacion: string;
  telefono: string;
  descripcion: string;
}

export interface FormOferente {
  correo: string;
  clave: string;
  identificacion: string;
  nombre: string;
  primerApellido: string;
  isoNacionalidad: string;
  telefono: string;
  lugarResidencia: string;
}
