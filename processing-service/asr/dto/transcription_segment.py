from dataclasses import dataclass


@dataclass(slots=True)
class TranscriptionSegment:
    start: float
    end: float
    text: str