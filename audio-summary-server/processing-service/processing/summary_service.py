from api.exceptions.handlers import InternalProcessingException, InvalidRequestException
from llm.service.text_summarizer import SummarizationError, TextSummarizer
from processing.task_storage import InMemoryTaskStorage


class SummaryService:
    def __init__(
        self,
        task_storage: InMemoryTaskStorage,
        summarizer: TextSummarizer,
        min_text_length: int = 20,
        max_text_length: int = 100_000
    ) -> None:
        self.task_storage = task_storage
        self.summarizer = summarizer
        self.min_text_length = min_text_length
        self.max_text_length = max_text_length

    def create_task(self, task_id: int, text: str) -> dict:
        cleaned_text = text.strip()

        if cleaned_text == "":
            raise InvalidRequestException("Text for summarization must not be empty")

        if len(cleaned_text) < self.min_text_length:
            raise InvalidRequestException(
                f"Text for summarization is too short. Minimum length is {self.min_text_length} characters"
            )

        if len(cleaned_text) > self.max_text_length:
            raise InvalidRequestException(
                f"Text for summarization is too large. Maximum length is {self.max_text_length} characters"
            )

        self.task_storage.create_summary_task(task_id)

        try:
            summary_result = self.summarizer.summarize(cleaned_text)

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