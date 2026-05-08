import argparse
from pathlib import Path

import matplotlib.pyplot as plt
import pandas as pd


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--metric", required=True, choices=["wer", "cer"])
    parser.add_argument("--artifacts-dir", default="artifacts")
    args = parser.parse_args()

    metric = args.metric.lower()
    metric_upper = metric.upper()

    artifacts_dir = Path(args.artifacts_dir)

    whisper_report_path = (
        artifacts_dir / "metrics" / "reports" / metric / "whisper" / "report.csv"
    )
    tone_report_path = (
        artifacts_dir / "metrics" / "reports" / metric / "tone" / "report.csv"
    )

    if not whisper_report_path.exists():
        raise FileNotFoundError(f"file not found: {whisper_report_path}")

    if not tone_report_path.exists():
        raise FileNotFoundError(f"file not found: {tone_report_path}")

    whisper_df = pd.read_csv(whisper_report_path)
    tone_df = pd.read_csv(tone_report_path)

    whisper_metric_column = f"whisper_{metric}"
    tone_metric_column = f"tone_{metric}"

    whisper_df = whisper_df[["sample_id", metric]].rename(
        columns={metric: whisper_metric_column}
    )
    tone_df = tone_df[["sample_id", metric]].rename(
        columns={metric: tone_metric_column}
    )

    merged_df = whisper_df.merge(tone_df, on="sample_id", how="inner")
    merged_df = merged_df.sort_values("sample_id").reset_index(drop=True)

    if merged_df.empty:
        raise ValueError("no common sample_id found between whisper and tone reports")

    merged_df["sample_index"] = range(1, len(merged_df) + 1)

    whisper_average = merged_df[whisper_metric_column].mean()
    tone_average = merged_df[tone_metric_column].mean()

    output_dir = artifacts_dir / "metrics" / "plots" / metric
    output_dir.mkdir(parents=True, exist_ok=True)

    plt.figure(figsize=(12, 6))
    plt.plot(
        merged_df["sample_index"],
        merged_df[whisper_metric_column],
        label="Whisper"
    )
    plt.plot(
        merged_df["sample_index"],
        merged_df[tone_metric_column],
        label="T-one"
    )
    plt.xlabel("Sample index")
    plt.ylabel(metric_upper)
    plt.title(f"{metric_upper} by sample: Whisper vs T-one")
    plt.legend()
    plt.tight_layout()
    plt.savefig(output_dir / f"{metric}_by_sample.png")
    plt.close()

    plt.figure(figsize=(8, 6))
    plt.bar(["Whisper", "T-one"], [whisper_average, tone_average])
    plt.ylabel(f"Average {metric_upper}")
    plt.title(f"Average {metric_upper} comparison")
    plt.tight_layout()
    plt.savefig(output_dir / f"average_{metric}_comparison.png")
    plt.close()

    print(f"plots saved to: {output_dir}")
    print(f"whisper average {metric_upper}: {whisper_average:.6f}")
    print(f"tone average {metric_upper}: {tone_average:.6f}")


if __name__ == "__main__":
    main()