import tempfile
from pathlib import Path
from typing import Iterable, Optional

from asr.evaluation.parquet_dataset_reader import DatasetRecord
from asr.evaluation.text_normalizer import TextNormalizer
from asr.storage.transcription_writer import TranscriptionWriter


class DatasetTranscriber:
    def __init__(
        self,
        transcriber,
        result_dir: str,
        normalizer: Optional[TextNormalizer] = None,
    ) -> None:
        self.transcriber = transcriber
        self.result_dir = Path(result_dir)
        self.result_dir.mkdir(parents=True, exist_ok=True)
        self.writer = TranscriptionWriter()
        self.normalizer = normalizer or TextNormalizer()

    def transcribe_records(
        self,
        records: Iterable[DatasetRecord],
        language: str = "ru",
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

            temp_audio_path = self._write_temp_audio(record.audio_bytes)

            try:
                print(f"transcribing: {record.sample_id}")
                result = self.transcriber.transcribe(str(temp_audio_path), language=language)

                normalized_text = self.normalizer.normalize(result.text)

                if not normalized_text:
                    print(f"empty transcription after normalization: {record.sample_id}")
                    processed_count += 1
                    continue

                self.writer.write_text(normalized_text, str(output_path))
                print(f"saved: {output_path}")

            except Exception as error:
                print(f"failed for {record.sample_id}: {error}")
            finally:
                if temp_audio_path.exists():
                    temp_audio_path.unlink()

            processed_count += 1

        print(f"done, processed: {processed_count}")

    def _write_temp_audio(self, audio_bytes: bytes) -> Path:
        with tempfile.NamedTemporaryFile(delete=False, suffix=".wav") as temp_file:
            temp_file.write(audio_bytes)
            return Path(temp_file.name)