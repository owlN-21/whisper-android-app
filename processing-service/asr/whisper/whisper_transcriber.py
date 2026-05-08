from faster_whisper import WhisperModel

from asr.base_transcriber import BaseTranscriber
from asr.dto.transcription_result import TranscriptionResult
from asr.dto.transcription_segment import TranscriptionSegment


class WhisperTranscriber(BaseTranscriber):
    def __init__(
        self,
        model_size: str = "small",
        device: str = "cpu",
        compute_type: str = "int8"
    ) -> None:
        self.model = WhisperModel(
            model_size,
            device=device,
            compute_type=compute_type
        )

    def transcribe(self, audio_path: str, language: str = "ru") -> TranscriptionResult:
        segments, info = self.model.transcribe(
            audio_path,
            language=language,
            word_timestamps=True
        )

        result_segments = [
            TranscriptionSegment(
                start=segment.start,
                end=segment.end,
                text=segment.text.strip()
            )
            for segment in segments
        ]

        full_text = "\n".join(
            segment.text for segment in result_segments
            if segment.text
        ).strip()

        return TranscriptionResult(
            text=full_text,
            language=info.language,
            language_probability=info.language_probability,
            segments=result_segments
        )