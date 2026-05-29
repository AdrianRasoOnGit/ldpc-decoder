#!/usr/bin/env python3

from __future__ import annotations

import csv
import re
from pathlib import Path

import matplotlib.pyplot as plt


ALPHA_PATTERN = re.compile(r"ber_nms_alpha_(\d+)_(\d+)\.csv")


def extract_alpha(path: Path) -> float:
    match = ALPHA_PATTERN.match(path.name)

    if not match:
        raise ValueError(f"Cannot extract alpha from filename: {path.name}")

    return float(f"{match.group(1)}.{match.group(2)}")


def read_csv(path: Path) -> tuple[list[float], list[float], list[float]]:
    ebn0: list[float] = []
    ber: list[float] = []
    fer: list[float] = []

    with path.open("r", newline="") as f:
        reader = csv.DictReader(f)

        for row in reader:
            ebn0.append(float(row["ebN0Db"]))
            ber.append(float(row["ber"]))
            fer.append(float(row["fer"]))

    return ebn0, ber, fer


def plot_alpha_sweep(csv_paths: list[Path], metric: str, output: Path) -> None:
    plt.figure(figsize=(8, 5))

    sorted_paths = sorted(csv_paths, key=extract_alpha)

    for path in sorted_paths:
        alpha = extract_alpha(path)
        ebn0, ber, fer = read_csv(path)

        values = ber if metric == "ber" else fer

        plt.semilogy(
            ebn0,
            values,
            marker="o",
            linewidth=2,
            label=f"α={alpha:.2f}",
        )

    plt.xlabel("Eb/N0 (dB)")
    plt.ylabel(metric.upper())
    plt.title(f"Normalized Min-Sum Alpha Sweep: {metric.upper()} vs Eb/N0")
    plt.grid(True, which="both", linestyle="--", linewidth=0.5)
    plt.legend()
    plt.tight_layout()

    output.parent.mkdir(parents=True, exist_ok=True)
    plt.savefig(output, dpi=160)
    plt.close()


def main() -> None:
    input_dir = Path("results/ber")
    output_dir = Path("results/figures")

    csv_paths = list(input_dir.glob("ber_nms_alpha_*.csv"))

    if not csv_paths:
        raise FileNotFoundError(
            "No alpha sweep CSV files found in results/ber. "
            "Run AlphaSearchRunner first."
        )

    plot_alpha_sweep(
        csv_paths,
        "ber",
        output_dir / "alpha_sweep_ber_curve.png",
    )

    plot_alpha_sweep(
        csv_paths,
        "fer",
        output_dir / "alpha_sweep_fer_curve.png",
    )

    print(f"Wrote {output_dir / 'alpha_sweep_ber_curve.png'}")
    print(f"Wrote {output_dir / 'alpha_sweep_fer_curve.png'}")


if __name__ == "__main__":
    main()
