from api.exceptions.handlers import InternalProcessingException, InvalidRequestException
from llm.service.text_summarizer import SummarizationError, TextSummarizer
from processing.task_storage import InMemoryTaskStorage


class SummaryService:
    def __init__(
        self,
        task_storage: InMemoryTaskStorage,
        summarizer: TextSummarizer
    ) -> None:
        self.task_storage = task_storage
        self.summarizer = summarizer

    def create_task(self, task_id: int, text: str) -> dict:
        if text.strip() == "":
            raise InvalidRequestException("Text for summarization must not be empty")

        self.task_storage.create_summary_task(task_id)

        try:
            summary_result = self.summarizer.summarize(text)

            self.task_storage.mark_summary_completed(
                task_id,
                content=summary_result.text
            )
        except SummarizationError as error:
            self.task_storage.mark_summary_failed(task_id, str(error))
            raise InternalProcessingException(str(error)) from error
        except Exception as error:
            self.task_storage.mark_summary_failed(task_id, str(error))
            raise InternalProcessingException(str(error)) from error

        return {
            "taskId": task_id,
            "status": "ACCEPTED"
        }

    def get_task(self, task_id: int) -> dict:
        return self.task_storage.get_summary_task(task_id)