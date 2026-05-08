from pathlib import Path

from asr.base_transcriber import BaseTranscriber
from asr.dto.transcription_result import TranscriptionResult
from asr.dto.transcription_segment import TranscriptionSegment
from tone import DecoderType, StreamingCTCPipeline, read_audio


class ToneTranscriber(BaseTranscriber):
    def __init__(
        self,
        model_dir: str = "/models",
        use_beam_search: bool = True
    ) -> None:
        decoder_type = (
            DecoderType.BEAM_SEARCH
            if use_beam_search
            else DecoderType.GREEDY
        )

        if model_dir:
            self.pipeline = StreamingCTCPipeline.from_local(
                Path(model_dir),
                decoder_type=decoder_type
            )
        else:
            self.pipeline = StreamingCTCPipeline.from_hugging_face(
                decoder_type=decoder_type
            )

    def transcribe(self, audio_path: str, language: str = "ru") -> TranscriptionResult:
        audio = read_audio(audio_path)
        phrases = self.pipeline.forward_offline(audio)

        segments = [
            TranscriptionSegment(
                start=phrase.start_time,
                end=phrase.end_time,
                text=phrase.text.strip()
            )
            for phrase in phrases
            if phrase.text.strip()
        ]

        full_text = "\n".join(segment.text for segment in segments).strip()

        return TranscriptionResult(
            text=full_text,
            segments=segments,
            language=language,
            language_probability=None
        )