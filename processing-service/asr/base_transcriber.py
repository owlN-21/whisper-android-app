from abc import ABC, abstractmethod

from asr.dto.transcription_result import TranscriptionResult


class BaseTranscriber(ABC):
    @abstractmethod
    def transcribe(self, audio_path: str, language: str = "ru") -> TranscriptionResult:
        pass