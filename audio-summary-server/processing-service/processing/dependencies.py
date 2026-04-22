from asr.whisper.whisper_transcriber import WhisperTranscriber
from llm.service.text_summarizer import TextSummarizer
from processing.summary_service import SummaryService
from processing.task_storage import InMemoryTaskStorage
from processing.transcription_service import TranscriptionService
# from asr.tone.tone_transcriber import ToneTranscriber

task_storage = InMemoryTaskStorage()

transcriber = WhisperTranscriber(
    model_size="small",
    device="cpu",
    compute_type="int8"
)

text_summarizer = TextSummarizer()

transcription_service = TranscriptionService(
    task_storage=task_storage,
    transcriber=transcriber,
    language="ru"
)

summary_service = SummaryService(task_storage, text_summarizer)