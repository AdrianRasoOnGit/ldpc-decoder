#!/usr/bin/env python3

from __future__ import annotations

import argparse
import csv
from pathlib import Path

import matplotlib.pyplot as plt


def read_csv(path: Path) -> tuple[list[float], list[float], list[float]]:
    ebn0: list[float] = []
    success_rate: list[float] = []
    avg_iterations: list[float] = []

    with path.open("r", newline="") as f:
        reader = csv.DictReader(f)

        required = {"ebN0Db", "successRate", "avgIterations"}
        missing = required - set(reader.fieldnames or [])

        if missing:
            raise ValueError(f"{path} is missing columns: {sorted(missing)}")

        for row in reader:
            ebn0.append(float(row["ebN0Db"]))
            success_rate.append(float(row["successRate"]))
            avg_iterations.append(float(row["avgIterations"]))

    return ebn0, success_rate, avg_iterations


def label_from_path(path: Path) -> str:
    name = path.stem

    if name.startswith("ber_"):
        name = name[len("ber_"):]

    return name.replace("_", " ")


def plot_metric(
    csv_paths: list[Path],
    metric_name: str,
    output_path: Path,
) -> None:
    plt.figure(figsize=(8, 5))

    for csv_path in csv_paths:
        ebn0, success_rate, avg_iterations = read_csv(csv_path)

        values = success_rate if metric_name == "successRate" else avg_iterations
        label = label_from_path(csv_path)

        plt.plot(
            ebn0,
            values,
            marker="o",
            linewidth=2,
            label=label,
        )

    plt.xlabel("Eb/N0 (dB)")

    if metric_name == "successRate":
        plt.ylabel("Success Rate")
        plt.title("Decoder Success Rate vs Eb/N0")
        plt.ylim(0.0, 1.05)
    else:
        plt.ylabel("Average Iterations")
        plt.title("Average Decoder Iterations vs Eb/N0")

    plt.grid(True, linestyle="--", linewidth=0.5)
    plt.legend()
    plt.tight_layout()

    output_path.parent.mkdir(parents=True, exist_ok=True)
    plt.savefig(output_path, dpi=160)
    plt.close()


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Plot LDPC decoder convergence metrics from simulation CSV files."
    )

    parser.add_argument(
        "csv",
        nargs="+",
        type=Path,
        help="Input BER CSV files containing successRate and avgIterations columns.",
    )

    parser.add_argument(
        "--out-dir",
        type=Path,
        default=Path("results/figures"),
        help="Directory for generated figures.",
    )

    args = parser.parse_args()

    csv_paths = [path for path in args.csv if path.exists()]

    if not csv_paths:
        raise FileNotFoundError("No valid CSV files provided.")

    plot_metric(
        csv_paths,
        "successRate",
        args.out_dir / "success_rate_curve.png",
    )

    plot_metric(
        csv_paths,
        "avgIterations",
        args.out_dir / "avg_iterations_curve.png",
    )

    print(f"Wrote {args.out_dir / 'success_rate_curve.png'}")
    print(f"Wrote {args.out_dir / 'avg_iterations_curve.png'}")


if __name__ == "__main__":
    main()
