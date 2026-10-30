#!/usr/bin/env python3
"""
Runner de benchmarks para Merge Sort paralelo.
Ejecuta la batería completa, parsea la salida y guarda CSV.
Uso: python3 benchmark_runner.py <etiqueta-maquina>
Ejemplo: python3 benchmark_runner.py m1
"""

import csv
import os
import re
import subprocess
import sys

RESULTS_DIR = "results"
JAVA_CMD = ["java", "-Xmx2g", "-cp", "out", "sorting.Main", "bench"]

# Configuración de hilos por máquina
THREADS_BY_MACHINE = {
    "m1": "1,2,4,8",
    "i7": "1,2,4,6,12",
}

# Tamaños y umbrales a probar
SIZES = [100000, 300000, 1000000]
THRESHOLDS = [1000, 5000, 10000, 50000, 100000]
FIXED_SIZE = 1000000
FIXED_THRESHOLD = 10000


def run_bench(size, threshold, threads):
    """Ejecuta el benchmark de Java y retorna la salida como string."""
    cmd = JAVA_CMD + [str(size), str(threshold), threads]
    print(f"  $ {' '.join(cmd)}")
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0:
        print(f"  ERROR: {result.stderr}")
        return None
    return result.stdout


def parse_output(output):
    """Extrae el tiempo secuencial y las filas (hilos, tiempo, speedup) de la salida."""
    seq_match = re.search(r"Secuencial:\s+([0-9.]+)\s+ms", output)
    if not seq_match:
        return None, []
    sequential_ms = float(seq_match.group(1))

    rows = []
    for line in output.splitlines():
        # Las filas de la tabla empiezan con un entero seguido de tab
        m = re.match(r"^(\d+)\s+([0-9.]+)\s+([0-9.]+)x\s+(\w+)", line)
        if m:
            rows.append({
                "threads": int(m.group(1)),
                "time_ms": float(m.group(2)),
                "speedup": float(m.group(3)),
                "valid": m.group(4) == "OK",
            })
    return sequential_ms, rows


def write_csv(path, fieldnames, rows):
    with open(path, "w", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)
    print(f"  -> {path}")


def run_threads_sweep(machine, threads):
    """Speedup vs número de hilos a tamaño y umbral fijos."""
    print("\n[1/3] Speedup vs hilos (size=1M, threshold=10k)")
    output = run_bench(FIXED_SIZE, FIXED_THRESHOLD, threads)
    if output is None:
        return

    seq_ms, rows = parse_output(output)
    if seq_ms is None:
        print("  ERROR: no se pudo parsear la salida")
        return

    csv_rows = []
    for r in rows:
        csv_rows.append({
            "machine": machine,
            "size": FIXED_SIZE,
            "threshold": FIXED_THRESHOLD,
            "threads": r["threads"],
            "time_ms": r["time_ms"],
            "speedup": r["speedup"],
            "valid": r["valid"],
        })

    path = os.path.join(RESULTS_DIR, f"{machine}_threads.csv")
    write_csv(path, ["machine", "size", "threshold", "threads",
                     "time_ms", "speedup", "valid"], csv_rows)

    # Guardar salida cruda
    raw_path = os.path.join(RESULTS_DIR, f"{machine}_threads_raw.txt")
    with open(raw_path, "w") as f:
        f.write(output)
    print(f"  -> {raw_path} (salida cruda)")


def run_threshold_sweep(machine, threads):
    """Speedup vs umbral usando el máximo número de hilos."""
    print("\n[2/3] Speedup vs umbral (size=1M)")
    best_threads = threads.split(",")[-1]
    csv_rows = []

    for th in THRESHOLDS:
        output = run_bench(FIXED_SIZE, th, best_threads)
        if output is None:
            continue
        seq_ms, rows = parse_output(output)
        if seq_ms is None:
            continue
        for r in rows:
            csv_rows.append({
                "machine": machine,
                "size": FIXED_SIZE,
                "threshold": th,
                "threads": r["threads"],
                "time_ms": r["time_ms"],
                "speedup": r["speedup"],
                "valid": r["valid"],
            })

    path = os.path.join(RESULTS_DIR, f"{machine}_thresholds.csv")
    write_csv(path, ["machine", "size", "threshold", "threads",
                     "time_ms", "speedup", "valid"], csv_rows)


def run_size_sweep(machine, threads):
    """Speedup vs tamaño con umbral y hilos fijos."""
    print("\n[3/3] Speedup vs tamaño (threshold=10k)")
    best_threads = threads.split(",")[-1]
    csv_rows = []

    for size in SIZES:
        output = run_bench(size, FIXED_THRESHOLD, best_threads)
        if output is None:
            continue
        seq_ms, rows = parse_output(output)
        if seq_ms is None:
            continue
        for r in rows:
            csv_rows.append({
                "machine": machine,
                "size": size,
                "threshold": FIXED_THRESHOLD,
                "threads": r["threads"],
                "time_ms": r["time_ms"],
                "speedup": r["speedup"],
                "valid": r["valid"],
            })

    path = os.path.join(RESULTS_DIR, f"{machine}_sizes.csv")
    write_csv(path, ["machine", "size", "threshold", "threads",
                     "time_ms", "speedup", "valid"], csv_rows)


def main():
    if len(sys.argv) < 2:
        print("Uso: python3 benchmark_runner.py <etiqueta-maquina>")
        print("Ejemplo: python3 benchmark_runner.py m1")
        print("Máquinas predefinidas: " + ", ".join(THREADS_BY_MACHINE.keys()))
        sys.exit(1)

    machine = sys.argv[1]
    if machine not in THREADS_BY_MACHINE:
        print(f"Máquina desconocida: {machine}")
        print("Máquinas predefinidas: " + ", ".join(THREADS_BY_MACHINE.keys()))
        sys.exit(1)

    threads = THREADS_BY_MACHINE[machine]
    os.makedirs(RESULTS_DIR, exist_ok=True)

    print(f"=== Benchmark en máquina: {machine} ===")
    print(f"Hilos a probar: {threads}")

    run_threads_sweep(machine, threads)
    run_threshold_sweep(machine, threads)
    run_size_sweep(machine, threads)

    print("\n=== Listo. Archivos generados: ===")
    for f in sorted(os.listdir(RESULTS_DIR)):
        if f.startswith(machine):
            print(f"  {RESULTS_DIR}/{f}")


if __name__ == "__main__":
    main()