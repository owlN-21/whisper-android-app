from dataclasses import dataclass
from typing import List

from dto.transcription_segment import TranscriptionSegment


@dataclass
class TranscriptionResult:
    language: str
    language_probability: float
    segments: List[TranscriptionSegment]