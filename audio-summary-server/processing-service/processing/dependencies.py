from processing.summary_service import SummaryService
from processing.task_storage import InMemoryTaskStorage
from processing.transcription_service import TranscriptionService

task_storage = InMemoryTaskStorage()

transcription_service = TranscriptionService(task_storage)
summary_service = SummaryService(task_storage)