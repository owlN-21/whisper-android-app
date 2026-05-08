import time

from asr.base_transcriber import BaseTranscriber
from asr.evaluation.dataset_transcriber import DatasetTranscriber
from asr.evaluation.dataset_wer_evaluator import DatasetWerEvaluator, DatasetWerSummary
from asr.evaluation.parquet_dataset_reader import ParquetDatasetReader


class ModelEvaluationRunner:
    def __init__(
        self,
        transcriber: BaseTranscriber,
        data_dir: str,
        prediction_dir: str,
        report_dir: str,
    ) -> None:
        self.transcriber = transcriber
        self.data_dir = data_dir
        self.prediction_dir = prediction_dir
        self.report_dir = report_dir
        self.reader = ParquetDatasetReader()

    def run(
        self,
        language: str = "ru",
        limit: int | None = None,
    ) -> DatasetWerSummary:
        model_name = self.transcriber.__class__.__name__
        total_start = time.perf_counter()

        print(f"2. start dataset transcription for {model_name}")

        transcription_start = time.perf_counter()

        dataset_transcriber = DatasetTranscriber(
            transcriber=self.transcriber,
            result_dir=self.prediction_dir,
        )
        dataset_transcriber.transcribe_records(
            records=self.reader.iter_records(self.data_dir),
            language=language,
            limit=limit,
        )

        transcription_time = time.perf_counter() - transcription_start
        print(f"3. transcription finished in {transcription_time:.2f} sec")

        print(f"4. start WER evaluation for {model_name}")

        evaluation_start = time.perf_counter()

        evaluator = DatasetWerEvaluator(
            prediction_dir=self.prediction_dir,
            report_dir=self.report_dir,
        )
        summary = evaluator.evaluate_records(
            records=self.reader.iter_records(self.data_dir),
            limit=limit,
        )

        evaluation_time = time.perf_counter() - evaluation_start
        total_time = time.perf_counter() - total_start

        print(f"5. WER evaluation finished in {evaluation_time:.2f} sec")
        print(f"6. total pipeline time for {model_name}: {total_time:.2f} sec")

        return summary