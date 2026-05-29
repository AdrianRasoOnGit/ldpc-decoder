#!/usr/bin/env python3

from __future__ import annotations

import argparse
import csv
from pathlib import Path

import matplotlib.pyplot as plt


def read_ber_csv(path: Path) -> tuple[list[float], list[float], list[float]]:
    ebn0: list[float] = []
    ber: list[float] = []
    fer: list[float] = []

    with path.open("r", newline="") as f:
        reader = csv.DictReader(f)

        required = {"ebN0Db", "ber", "fer"}
        missing = required - set(reader.fieldnames or [])

        if missing:
            raise ValueError(f"{path} is missing columns: {sorted(missing)}")

        for row in reader:
            ebn0.append(float(row["ebN0Db"]))
            ber.append(float(row["ber"]))
            fer.append(float(row["fer"]))

    return ebn0, ber, fer


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
        ebn0, ber, fer = read_ber_csv(csv_path)

        values = ber if metric_name == "ber" else fer

        plt.semilogy(
            ebn0,
            values,
            marker="o",
            linewidth=2,
            label=label_from_path(csv_path),
        )

    plt.xlabel("Eb/N0 (dB)")
    plt.ylabel(metric_name.upper())
    plt.title(f"{metric_name.upper()} vs Eb/N0")
    plt.grid(True, which="both", linestyle="--", linewidth=0.5)
    plt.legend()
    plt.tight_layout()

    output_path.parent.mkdir(parents=True, exist_ok=True)
    plt.savefig(output_path, dpi=160)
    plt.close()


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Plot BER/FER curves from LDPC simulation CSV files."
    )

    parser.add_argument(
        "csv",
        nargs="+",
        type=Path,
        help="Input BER CSV files.",
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

    plot_metric(csv_paths, "ber", args.out_dir / "ber_curve.png")
    plot_metric(csv_paths, "fer", args.out_dir / "fer_curve.png")

    print(f"Wrote {args.out_dir / 'ber_curve.png'}")
    print(f"Wrote {args.out_dir / 'fer_curve.png'}")


if __name__ == "__main__":
    main()
