from dataclasses import dataclass, field

from asr.dto.transcription_segment import TranscriptionSegment


@dataclass(slots=True)
class TranscriptionResult:
    text: str
    segments: list[TranscriptionSegment] = field(default_factory=list)
    language: str | None = None
    language_probability: float | None = None