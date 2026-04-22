from llm.service.text_summarizer import TextSummarizer
from processing.summary_service import SummaryService
from processing.task_storage import InMemoryTaskStorage
from processing.transcription_service import TranscriptionService

task_storage = InMemoryTaskStorage()

text_summarizer = TextSummarizer(model_name="qwen2.5:7b")

transcription_service = TranscriptionService(task_storage)
summary_service = SummaryService(task_storage, text_summarizer)