from pathlib import Path
from typing import Iterable, Optional

from asr.evaluation.parquet_dataset_reader import DatasetRecord
from asr.evaluation.text_normalizer import TextNormalizer
from asr.storage.transcription_writer import TranscriptionWriter


class DatasetReferencePreparer:
    def __init__(self, result_dir: str) -> None:
        self.result_dir = Path(result_dir)
        self.result_dir.mkdir(parents=True, exist_ok=True)
        self.writer = TranscriptionWriter()
        self.normalizer = TextNormalizer()

    def prepare_references(
        self,
        records: Iterable[DatasetRecord],
        limit: Optional[int] = None,
    ) -> None:
        processed_count = 0

        for record in records:
            if limit is not None and processed_count >= limit:
                print(f"limit reached: {limit}")
                break

            output_path = self.result_dir / f"{record.sample_id}.txt"

            if output_path.exists():
                print(f"skip existing: {output_path.name}")
                processed_count += 1
                continue

            normalized_text = self.normalizer.normalize(record.reference_text)
            self.writer.write_text(normalized_text, str(output_path))

            print(f"saved reference: {output_path}")
            processed_count += 1

        print(f"done, prepared references: {processed_count}")