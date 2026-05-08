from llm.service.text_summarizer import TextSummarizer
from processing.summary_service import SummaryService
from processing.task_storage import InMemoryTaskStorage
from processing.transcription_service import TranscriptionService

task_storage = InMemoryTaskStorage()

text_summarizer = TextSummarizer()

transcription_service = TranscriptionService(
    task_storage=task_storage,
    language="ru"
)

summary_service = SummaryService(task_storage, text_summarizer)