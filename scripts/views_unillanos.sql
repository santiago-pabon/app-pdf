USE unillanos_admisiones;

-- Vistas de apoyo para análisis

-- 1) Conteo por año y periodo
CREATE OR REPLACE VIEW v_admitidos_por_anio_periodo AS
SELECT anio, periodo, COUNT(*) AS total
FROM admitidos
GROUP BY anio, periodo
ORDER BY anio, periodo;

-- 2) Conteo por facultad y programa
CREATE OR REPLACE VIEW v_admitidos_por_facultad_programa AS
SELECT facultad, programa, COUNT(*) AS total
FROM admitidos
GROUP BY facultad, programa
ORDER BY facultad, total DESC;

-- 3) Conteo por sede
CREATE OR REPLACE VIEW v_admitidos_por_sede AS
SELECT sede, COUNT(*) AS total
FROM admitidos
GROUP BY sede
ORDER BY total DESC;

-- 4) Distribución por genero por programa
CREATE OR REPLACE VIEW v_genero_por_programa AS
SELECT programa,
       genero,
       COUNT(*) AS total
FROM admitidos
GROUP BY programa, genero
ORDER BY programa, total DESC;

-- 5) Top 20 colegios de origen
CREATE OR REPLACE VIEW v_top_colegios AS
SELECT colegio, municipio_colegio, departamento_colegio, COUNT(*) AS total
FROM admitidos
GROUP BY colegio, municipio_colegio, departamento_colegio
ORDER BY total DESC
LIMIT 20;
