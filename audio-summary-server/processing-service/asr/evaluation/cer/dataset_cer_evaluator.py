import csv
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable, List, Optional

from asr.evaluation.cer.cer_calculator import CerCalculator
from asr.evaluation.parquet_dataset_reader import DatasetRecord
from asr.evaluation.text_normalizer import TextNormalizer


@dataclass
class SampleCerResult:
    sample_id: str
    speaker_name: Optional[str]
    reference_text: str
    prediction_text: str
    cer: float


@dataclass
class DatasetCerSummary:
    total_records: int
    evaluated_records: int
    skipped_missing_prediction: int
    skipped_empty_reference: int
    average_cer: float


class DatasetCerEvaluator:
    def __init__(self, prediction_dir: str, report_dir: str) -> None:
        self.prediction_dir = Path(prediction_dir)
        self.report_dir = Path(report_dir)
        self.report_dir.mkdir(parents=True, exist_ok=True)

        self.normalizer = TextNormalizer()
        self.cer_calculator = CerCalculator(ignore_spaces=True)

    def evaluate_records(
        self,
        records: Iterable[DatasetRecord],
        limit: Optional[int] = None,
    ) -> DatasetCerSummary:
        total_records = 0
        evaluated_records = 0
        skipped_missing_prediction = 0
        skipped_empty_reference = 0

        results: List[SampleCerResult] = []

        for record in records:
            if limit is not None and total_records >= limit:
                print(f"limit reached: {limit}")
                break

            total_records += 1

            prediction_path = self.prediction_dir / f"{record.sample_id}.txt"

            if not prediction_path.exists():
                print(f"prediction not found: {prediction_path.name}")
                skipped_missing_prediction += 1
                continue

            raw_reference = record.reference_text
            raw_prediction = prediction_path.read_text(encoding="utf-8")

            normalized_reference = self.normalizer.normalize(raw_reference)
            normalized_prediction = self.normalizer.normalize(raw_prediction)

            if not normalized_reference:
                print(f"empty reference after normalization: {record.sample_id}")
                skipped_empty_reference += 1
                continue

            cer = self.cer_calculator.calculate(
                reference_text=normalized_reference,
                hypothesis_text=normalized_prediction,
            )

            results.append(
                SampleCerResult(
                    sample_id=record.sample_id,
                    speaker_name=record.speaker_name,
                    reference_text=normalized_reference,
                    prediction_text=normalized_prediction,
                    cer=cer,
                )
            )

            evaluated_records += 1
            print(f"evaluated: {record.sample_id}, cer={cer:.4f}")

        average_cer = self._calculate_average_cer(results)

        summary = DatasetCerSummary(
            total_records=total_records,
            evaluated_records=evaluated_records,
            skipped_missing_prediction=skipped_missing_prediction,
            skipped_empty_reference=skipped_empty_reference,
            average_cer=average_cer,
        )

        self._write_detailed_report(results)
        self._write_summary(summary)

        return summary

    def _calculate_average_cer(self, results: List[SampleCerResult]) -> float:
        if not results:
            return 0.0

        total_cer = sum(result.cer for result in results)
        return total_cer / len(results)

    def _write_detailed_report(self, results: List[SampleCerResult]) -> None:
        report_path = self.report_dir / "report.csv"

        with report_path.open("w", encoding="utf-8", newline="") as file:
            writer = csv.writer(file)
            writer.writerow(
                [
                    "sample_id",
                    "speaker_name",
                    "reference_text",
                    "prediction_text",
                    "cer",
                ]
            )

            for result in results:
                writer.writerow(
                    [
                        result.sample_id,
                        result.speaker_name or "",
                        result.reference_text,
                        result.prediction_text,
                        f"{result.cer:.6f}",
                    ]
                )

        print(f"detailed report saved: {report_path}")

    def _write_summary(self, summary: DatasetCerSummary) -> None:
        summary_path = self.report_dir / "summary.txt"

        lines = [
            f"total_records: {summary.total_records}",
            f"evaluated_records: {summary.evaluated_records}",
            f"skipped_missing_prediction: {summary.skipped_missing_prediction}",
            f"skipped_empty_reference: {summary.skipped_empty_reference}",
            f"average_cer: {summary.average_cer:.6f}",
            f"average_cer_percent: {summary.average_cer * 100:.2f}%",
        ]

        summary_path.write_text("\n".join(lines), encoding="utf-8")
        print(f"summary saved: {summary_path}")