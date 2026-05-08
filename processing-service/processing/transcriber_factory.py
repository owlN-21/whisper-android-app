from asr.base_transcriber import BaseTranscriber
from asr.tone.tone_transcriber import ToneTranscriber
from asr.whisper.whisper_transcriber import WhisperTranscriber


def create_transcriber(model_name: str = "tone") -> BaseTranscriber:
    if model_name == "whisper":
        return WhisperTranscriber(
            model_size="small",
            device="cpu",
            compute_type="int8"
        )

    if model_name == "tone":
        return ToneTranscriber(
            model_dir="/models",
            use_beam_search=True
        )

    raise ValueError(f"unsupported ASR model: {model_name}")