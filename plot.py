#!/usr/bin/env python3
"""
Genera gráficas de speedup a partir de los CSV generados por benchmark_runner.py.
Uso: python3 plot.py
"""
import csv
import os
import matplotlib.pyplot as plt

RESULTS_DIR = "results"
OUTPUT_DIR = "figures"
os.makedirs(OUTPUT_DIR, exist_ok=True)

MACHINE_LABELS = {
    "m1": "M1 (8C/8T)",
    "i7": "i7-8700T (6C/12T)",
}


def read_csv(path):
    with open(path, newline="") as f:
        return list(csv.DictReader(f))


def load_all(metric):
    data = {}
    for machine in MACHINE_LABELS:
        path = os.path.join(RESULTS_DIR, f"{machine}_{metric}.csv")
        if os.path.exists(path):
            data[machine] = read_csv(path)
    return data


def plot_threads():
    data = load_all("threads")
    plt.figure(figsize=(8, 5))
    for machine, rows in data.items():
        xs = [int(r["threads"]) for r in rows]
        ys = [float(r["speedup"]) for r in rows]
        plt.plot(xs, ys, marker="o", linewidth=2, label=MACHINE_LABELS[machine])
    plt.plot([1, 12], [1, 12], "k--", alpha=0.4, label="Ideal (lineal)")
    plt.xlabel("Número de hilos")
    plt.ylabel("Speedup (vs secuencial)")
    plt.title("Speedup vs hilos — Merge Sort paralelo (1M elementos, umbral 10k)")
    plt.grid(True, alpha=0.3)
    plt.legend()
    plt.tight_layout()
    plt.savefig(os.path.join(OUTPUT_DIR, "speedup_vs_threads.png"), dpi=150)
    plt.close()
    print("  -> figures/speedup_vs_threads.png")


def plot_thresholds():
    data = load_all("thresholds")
    plt.figure(figsize=(8, 5))
    for machine, rows in data.items():
        xs = [int(r["threshold"]) for r in rows]
        ys = [float(r["speedup"]) for r in rows]
        plt.plot(xs, ys, marker="s", linewidth=2, label=MACHINE_LABELS[machine])
    plt.xscale("log")
    plt.xlabel("Umbral (elementos por tarea secuencial)")
    plt.ylabel("Speedup (vs secuencial)")
    plt.title("Speedup vs umbral — Merge Sort paralelo (1M elementos)")
    plt.grid(True, alpha=0.3, which="both")
    plt.legend()
    plt.tight_layout()
    plt.savefig(os.path.join(OUTPUT_DIR, "speedup_vs_threshold.png"), dpi=150)
    plt.close()
    print("  -> figures/speedup_vs_threshold.png")


def plot_sizes():
    data = load_all("sizes")
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 5))

    for machine, rows in data.items():
        xs = [int(r["size"]) for r in rows]
        ys_time = [float(r["time_ms"]) for r in rows]
        ys_speed = [float(r["speedup"]) for r in rows]
        ax1.plot(xs, ys_time, marker="o", linewidth=2, label=MACHINE_LABELS[machine])
        ax2.plot(xs, ys_speed, marker="o", linewidth=2, label=MACHINE_LABELS[machine])

    ax1.set_xscale("log")
    ax1.set_xlabel("Tamaño del arreglo")
    ax1.set_ylabel("Tiempo (ms)")
    ax1.set_title("Tiempo de ejecución vs tamaño")
    ax1.grid(True, alpha=0.3, which="both")
    ax1.legend()

    ax2.set_xscale("log")
    ax2.set_xlabel("Tamaño del arreglo")
    ax2.set_ylabel("Speedup")
    ax2.set_title("Speedup vs tamaño")
    ax2.grid(True, alpha=0.3, which="both")
    ax2.legend()

    plt.tight_layout()
    plt.savefig(os.path.join(OUTPUT_DIR, "speedup_vs_size.png"), dpi=150)
    plt.close()
    print("  -> figures/speedup_vs_size.png")


if __name__ == "__main__":
    print("Generando gráficas...")
    plot_threads()
    plot_thresholds()
    plot_sizes()
    print("Graficas en carpeta: figures/")