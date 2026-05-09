CREATE DATABASE bolsa_empleo_p2;
USE bolsa_empleo_p2;

-- Tabla central de autenticación
CREATE TABLE usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    correo VARCHAR(100) NOT NULL UNIQUE,
    clave VARCHAR(255) NOT NULL,
    rol VARCHAR(20)  NOT NULL,   -- ADMIN | EMPRESA | OFERENTE
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Administrador
CREATE TABLE administrador (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL UNIQUE,
    identificacion VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    CONSTRAINT fk_admin_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

-- Nacionalidades
CREATE TABLE nacionalidad (
    iso VARCHAR(5) NOT NULL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    iso3 VARCHAR(5),
    codigo_numero INT,
    codigo_telefono INT
);

-- Empresa
CREATE TABLE empresa (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    localizacion VARCHAR(150),
    telefono VARCHAR(20),
    descripcion TEXT,
    autorizado   BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_empresa_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

-- Perfil oferente (1:1 con usuario)
CREATE TABLE oferente (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL UNIQUE,
    identificacion VARCHAR(20)  NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    primer_apellido VARCHAR(100) NOT NULL,
    nacionalidad VARCHAR(5)   NOT NULL,
    telefono VARCHAR(20),
    lugar_residencia VARCHAR(150),
    autorizado BOOLEAN NOT NULL DEFAULT FALSE,
    curriculum VARCHAR(255),
    CONSTRAINT fk_oferente_usuario FOREIGN KEY (usuario_id)   REFERENCES usuario(id),
    CONSTRAINT fk_oferente_nacionalidad FOREIGN KEY (nacionalidad) REFERENCES nacionalidad(iso)
);

-- Carascteristica
CREATE TABLE caracteristica (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    padre_id INT,
    CONSTRAINT fk_caracteristica_padre FOREIGN KEY (padre_id) REFERENCES caracteristica(id)
);

-- Habilidad
CREATE TABLE habilidad (
    id INT AUTO_INCREMENT PRIMARY KEY,
    oferente_id INT NOT NULL,
    caracteristica_id INT NOT NULL,
    nivel INT NOT NULL,
    UNIQUE KEY uq_oferente_caracteristica (oferente_id, caracteristica_id),
    CONSTRAINT fk_habilidad_oferente FOREIGN KEY (oferente_id) REFERENCES oferente(id),
    CONSTRAINT fk_habilidad_caracteristica FOREIGN KEY (caracteristica_id) REFERENCES caracteristica(id),
    CONSTRAINT chk_habilidad_nivel CHECK (nivel BETWEEN 1 AND 5)
);

-- Puesto
CREATE TABLE puesto (
    id INT AUTO_INCREMENT PRIMARY KEY,
    descripcion TEXT NOT NULL,
    salario DECIMAL(12, 2) NOT NULL,
    tipo_publicacion VARCHAR(20) NOT NULL DEFAULT 'publico',
    empresa_id INT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_puesto_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id),
    CONSTRAINT chk_puesto_tipo CHECK (tipo_publicacion IN ('publico', 'privado'))
);

-- Características requeridas por puesto
CREATE TABLE puesto_caracteristica (
    id INT AUTO_INCREMENT PRIMARY KEY,
    puesto_id INT NOT NULL,
    caracteristica_id INT NOT NULL,
    nivel_requerido INT NOT NULL,
    UNIQUE KEY uq_puesto_caracteristica (puesto_id, caracteristica_id),
    CONSTRAINT fk_puestocaracteristica_puesto FOREIGN KEY (puesto_id) REFERENCES puesto(id),
    CONSTRAINT fk_puestocaracteristica_caracteristica FOREIGN KEY (caracteristica_id) REFERENCES caracteristica(id),
    CONSTRAINT chk_puestocaracteristica_nivel CHECK (nivel_requerido BETWEEN 1 AND 5)
);
