from pathlib import Path

from asr.dto.transcription_result import TranscriptionResult


class TranscriptionWriter:
    def write_plain_text(self, result: TranscriptionResult, output_path: str) -> None:
        Path(output_path).write_text(result.text.strip(), encoding="utf-8")

    def write_text(self, text: str, output_path: str) -> None:
        Path(output_path).write_text(text, encoding="utf-8")