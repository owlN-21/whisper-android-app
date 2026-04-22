from api.exceptions.handlers import TaskAlreadyExistsException, TaskNotFoundException


class InMemoryTaskStorage:
    def __init__(self) -> None:
        self.transcriptions: dict[int, dict] = {}
        self.summaries: dict[int, dict] = {}

    def create_transcription_task(self, task_id: int) -> None:
        if task_id in self.transcriptions:
            raise TaskAlreadyExistsException(task_id)

        self.transcriptions[task_id] = {
            "taskId": task_id,
            "status": "IN_PROGRESS",
            "text": None,
            "errorMessage": None
        }

    def get_transcription_task(self, task_id: int) -> dict:
        task = self.transcriptions.get(task_id)

        if task is None:
            raise TaskNotFoundException(task_id)

        return task

    def mark_transcription_completed(self, task_id: int, text: str) -> None:
        task = self.get_transcription_task(task_id)
        task["status"] = "COMPLETED"
        task["text"] = text
        task["errorMessage"] = None

    def mark_transcription_failed(self, task_id: int, error_message: str) -> None:
        task = self.get_transcription_task(task_id)
        task["status"] = "FAILED"
        task["text"] = None
        task["errorMessage"] = error_message

    def create_summary_task(self, task_id: int) -> None:
        if task_id in self.summaries:
            raise TaskAlreadyExistsException(task_id)

        self.summaries[task_id] = {
            "taskId": task_id,
            "status": "IN_PROGRESS",
            "content": None,
            "errorMessage": None
        }

    def get_summary_task(self, task_id: int) -> dict:
        task = self.summaries.get(task_id)

        if task is None:
            raise TaskNotFoundException(task_id)

        return task

    def mark_summary_completed(self, task_id: int, content: str) -> None:
        task = self.get_summary_task(task_id)
        task["status"] = "COMPLETED"
        task["content"] = content
        task["errorMessage"] = None

    def mark_summary_failed(self, task_id: int, error_message: str) -> None:
        task = self.get_summary_task(task_id)
        task["status"] = "FAILED"
        task["content"] = None
        task["errorMessage"] = error_message