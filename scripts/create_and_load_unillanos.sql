-- Crear base de datos y tabla para admitidos Unillanos
CREATE DATABASE IF NOT EXISTS unillanos_admisiones
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
USE unillanos_admisiones;

-- Tabla principal (tipos adecuados + índices útiles)
DROP TABLE IF EXISTS admitidos;
CREATE TABLE admitidos (
  id INT NOT NULL AUTO_INCREMENT,
  anio SMALLINT NOT NULL,
  periodo TINYINT NOT NULL,
  tipo_hecho VARCHAR(20) NOT NULL,
  genero VARCHAR(12) NOT NULL,
  estado_civil VARCHAR(20) NOT NULL,
  fecha_nacimiento DATE NOT NULL,
  pais VARCHAR(80) NOT NULL,
  ciudad_nacimiento VARCHAR(120) NOT NULL,
  ciudad_residencia VARCHAR(120) NOT NULL,
  departamento_colegio VARCHAR(120) NOT NULL,
  municipio_colegio VARCHAR(120) NOT NULL,
  colegio VARCHAR(200) NOT NULL,
  naturaleza_colegio VARCHAR(20) NOT NULL,
  snies INT NULL,
  programa VARCHAR(200) NOT NULL,
  facultad VARCHAR(200) NOT NULL,
  modalidad VARCHAR(80) NOT NULL,
  metodologia VARCHAR(80) NOT NULL,
  sede VARCHAR(200) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_anio (anio),
  KEY idx_programa (programa),
  KEY idx_facultad (facultad)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Carga del CSV. Usa LOCAL para evitar restricciones de secure-file-priv
-- Ajusta la ruta si tu CSV está en otra ubicación.
SET @csv_path = 'C:/Escritorio/Taller PDF/admitidos_unillanos.csv';

-- Habilitar local_infile si es necesario (requiere privilegios adecuados)
-- SET GLOBAL local_infile = 1;

LOAD DATA LOCAL INFILE @csv_path
INTO TABLE admitidos
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' ENCLOSED BY '"' ESCAPED BY '\\'
LINES TERMINATED BY '\r\n'
IGNORE 1 LINES
(@anio_txt,
 @periodo_txt,
 @tipo_hecho,
 @genero,
 @estado_civil,
 @fecha_nac_txt,
 @pais,
 @ciudad_nacimiento,
 @ciudad_residencia,      -- En CSV viene como CIUDAD_RECIDENCIA (con i), mapeamos por posición
 @departamento_colegio,   -- En CSV: DEPARTEMENTO_COLEGIO (typo), mapeamos por posición
 @municipio_colegio,
 @colegio,
 @naturaleza_colegio,
 @snies_txt,
 @programa,
 @facultad,
 @modalidad,
 @metodologia,
 @sede)
SET
  anio  = NULLIF(REPLACE(@anio_txt, ',', ''), '') + 0,
  periodo = NULLIF(REPLACE(@periodo_txt, ',', ''), '') + 0,
  tipo_hecho = @tipo_hecho,
  genero = @genero,
  estado_civil = @estado_civil,
  fecha_nacimiento = DATE(STR_TO_DATE(@fecha_nac_txt, '%m/%d/%Y %h:%i:%s %p')),
  pais = @pais,
  ciudad_nacimiento = @ciudad_nacimiento,
  ciudad_residencia = @ciudad_residencia,
  departamento_colegio = @departamento_colegio,
  municipio_colegio = @municipio_colegio,
  colegio = @colegio,
  naturaleza_colegio = @naturaleza_colegio,
  snies = NULLIF(REPLACE(@snies_txt, ',', ''), '') + 0,
  programa = @programa,
  facultad = @facultad,
  modalidad = @modalidad,
  metodologia = @metodologia,
  sede = @sede;

-- Comprobaciones rápidas
SELECT COUNT(*) AS total_registros FROM admitidos;
SELECT anio, COUNT(*) FROM admitidos GROUP BY anio ORDER BY anio;
