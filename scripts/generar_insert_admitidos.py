#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Genera un script SQL con INSERTs a partir del CSV admitidos_unillanos.csv
Uso:
  python scripts/generar_insert_admitidos.py [ruta_csv] [ruta_sql_salida]
Si no se pasan argumentos, usa rutas por defecto en el workspace.
"""
import csv
import sys
import os
from datetime import datetime

# Columnas esperadas por posición en el CSV
# 0 ANIO
# 1 PERIODO
# 2 TIPO_HECHO
# 3 GENERO
# 4 ESTADO_CIVIL
# 5 FECHA_NACIMIENTO (MM/DD/YYYY hh:mm:ss AM/PM)
# 6 PAIS
# 7 CIUDAD_NACIMIENTO
# 8 CIUDAD_RECIDENCIA
# 9 DEPARTEMENTO_COLEGIO
# 10 MUNICIPIO_COLEGIO
# 11 COLEGIO
# 12 NATURALEZA_COLEGIO
# 13 SNIES
# 14 PROGRAMA
# 15 FACULTAD
# 16 MODALIDAD
# 17 METODOLOGIA
# 18 SEDE

WORKSPACE = os.path.normpath(r"C:/Escritorio/Taller PDF")
DEFAULT_CSV = os.path.join(WORKSPACE, "admitidos_unillanos.csv")
DEFAULT_SQL_OUT = os.path.join(WORKSPACE, "scripts", "insert_admitidos_unillanos.sql")

BATCH_SIZE = 500


def esc(s: str) -> str:
    """Escapa comillas simples y backslashes para SQL y recorta espacios."""
    if s is None:
        return ''
    s = s.strip()
    s = s.replace("\\", "\\\\")  # backslash
    s = s.replace("'", "''")          # single quote
    return s


def to_int(s: str):
    if s is None:
        return None
    s = s.replace(',', '').strip()
    if s == '':
        return None
    try:
        return int(s)
    except ValueError:
        # Si viene con decimales u otro ruido, intenta parsear lo numérico
        digits = ''.join(ch for ch in s if ch.isdigit())
        return int(digits) if digits else None


def to_date(s: str):
    s = (s or '').strip()
    if not s:
        return None
    # Esperado: MM/DD/YYYY hh:mm:ss AM/PM
    # Algunos CSV podrían traer '0' en el tiempo; manejamos formateo flexible
    patterns = [
        '%m/%d/%Y %I:%M:%S %p',
        '%m/%d/%Y %H:%M:%S',
        '%m/%d/%Y'
    ]
    for p in patterns:
        try:
            dt = datetime.strptime(s, p)
            return dt.strftime('%Y-%m-%d')
        except ValueError:
            continue
    # Si no se puede parsear, lo dejamos como NULL
    return None


def write_header(f):
    f.write('-- Archivo generado automáticamente. Codificación: UTF-8\n')
    f.write('USE unillanos_admisiones;\n')
    f.write('SET NAMES utf8mb4;\n\n')


def write_insert_batch(f, batch_rows):
    if not batch_rows:
        return
    cols = (
        'anio, periodo, tipo_hecho, genero, estado_civil, fecha_nacimiento, '
        'pais, ciudad_nacimiento, ciudad_residencia, departamento_colegio, municipio_colegio, '
        'colegio, naturaleza_colegio, snies, programa, facultad, modalidad, metodologia, sede'
    )
    f.write('INSERT INTO admitidos (' + cols + ') VALUES\n')
    for i, vals in enumerate(batch_rows):
        # vals debe ser una lista de columnas ya formateadas como strings
        f.write('  (' + ', '.join(vals) + ')')
        f.write(',\n' if i < len(batch_rows) - 1 else ';\n\n')


def main():
    csv_path = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_CSV
    out_path = sys.argv[2] if len(sys.argv) > 2 else DEFAULT_SQL_OUT

    os.makedirs(os.path.dirname(out_path), exist_ok=True)

    total_rows = 0
    batch = []

    with open(csv_path, 'r', encoding='utf-8-sig', newline='') as fin, \
         open(out_path, 'w', encoding='utf-8', newline='') as fout:
        reader = csv.reader(fin, delimiter=',', quotechar='"')

        # Leer encabezado y descartarlo
        header = next(reader, None)
        write_header(fout)

        for row in reader:
            if not row:
                continue
            # Asegurar longitud mínima
            if len(row) < 19:
                continue
            anio = to_int(row[0])
            periodo = to_int(row[1])
            tipo_hecho = esc(row[2])
            genero = esc(row[3])
            estado_civil = esc(row[4])
            fecha_nacimiento = to_date(row[5])
            pais = esc(row[6])
            ciudad_nacimiento = esc(row[7])
            ciudad_residencia = esc(row[8])
            departamento_colegio = esc(row[9])
            municipio_colegio = esc(row[10])
            colegio = esc(row[11])
            naturaleza_colegio = esc(row[12])
            snies = to_int(row[13])
            programa = esc(row[14])
            facultad = esc(row[15])
            modalidad = esc(row[16])
            metodologia = esc(row[17])
            sede = esc(row[18])

            vals = [
                'NULL' if anio is None else str(anio),
                'NULL' if periodo is None else str(periodo),
                f"'{tipo_hecho}'",
                f"'{genero}'",
                f"'{estado_civil}'",
                'NULL' if fecha_nacimiento is None else f"'{fecha_nacimiento}'",
                f"'{pais}'",
                f"'{ciudad_nacimiento}'",
                f"'{ciudad_residencia}'",
                f"'{departamento_colegio}'",
                f"'{municipio_colegio}'",
                f"'{colegio}'",
                f"'{naturaleza_colegio}'",
                'NULL' if snies is None else str(snies),
                f"'{programa}'",
                f"'{facultad}'",
                f"'{modalidad}'",
                f"'{metodologia}'",
                f"'{sede}'",
            ]

            batch.append(vals)
            total_rows += 1

            if len(batch) >= BATCH_SIZE:
                write_insert_batch(fout, batch)
                batch.clear()

        # Último batch
        if batch:
            write_insert_batch(fout, batch)
            batch.clear()

    print(f"Generado: {out_path}")


if __name__ == '__main__':
    main()
