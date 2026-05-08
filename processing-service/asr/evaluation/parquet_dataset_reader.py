from dataclasses import dataclass
from pathlib import Path
from typing import Generator, Optional

import pandas as pd


@dataclass
class DatasetRecord:
    sample_id: str
    reference_text: str
    speaker_name: Optional[str]
    audio_bytes: bytes


class ParquetDatasetReader:
    def iter_records(self, data_dir: str) -> Generator[DatasetRecord, None, None]:
        data_path = Path(data_dir)

        if not data_path.exists():
            raise FileNotFoundError(f"data directory not found: {data_path}")

        parquet_files = sorted(data_path.glob("*.parquet"))

        if not parquet_files:
            raise FileNotFoundError(f"no parquet files found in: {data_path}")

        for parquet_path in parquet_files:
            dataframe = pd.read_parquet(parquet_path)

            for row_index, row in dataframe.iterrows():
                audio = row.get("audio")
                audio_bytes = None

                if isinstance(audio, dict):
                    audio_bytes = audio.get("bytes")

                if not audio_bytes:
                    continue

                sample_id = f"{parquet_path.stem}_{row_index:06d}"

                yield DatasetRecord(
                    sample_id=sample_id,
                    reference_text=str(row.get("text", "")).strip(),
                    speaker_name=row.get("speaker_name"),
                    audio_bytes=audio_bytes,
                )