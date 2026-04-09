from dto.transcription_result import TranscriptionResult


class TranscriptionWriter:

    def write_plain_text(self, result: TranscriptionResult, file_path: str) -> None:
        """
        Сохраняет транскрибацию в файл без таймкодов
        """

        with open(file_path, "w", encoding="utf-8") as f:
            for segment in result.segments:
                f.write(segment.text + " ")

        print(f"Transcription saved to {file_path}")