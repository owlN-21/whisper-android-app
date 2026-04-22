from datetime import datetime, timedelta, UTC

from api.exceptions.handlers import TaskAlreadyExistsException, TaskNotFoundException


class InMemoryTaskStorage:
    def __init__(self, ttl_minutes: int = 60) -> None:
        self.transcriptions: dict[int, dict] = {}
        self.summaries: dict[int, dict] = {}
        self.ttl = timedelta(minutes=ttl_minutes)

    def create_transcription_task(self, task_id: int) -> None:
        self._cleanup_expired_tasks()

        if task_id in self.transcriptions:
            raise TaskAlreadyExistsException(task_id)

        self.transcriptions[task_id] = {
            "taskId": task_id,
            "status": "IN_PROGRESS",
            "text": None,
            "errorMessage": None,
            "createdAt": datetime.now(UTC)
        }

    def get_transcription_task(self, task_id: int) -> dict:
        self._cleanup_expired_tasks()

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
        self._cleanup_expired_tasks()

        if task_id in self.summaries:
            raise TaskAlreadyExistsException(task_id)

        self.summaries[task_id] = {
            "taskId": task_id,
            "status": "IN_PROGRESS",
            "content": None,
            "errorMessage": None,
            "createdAt": datetime.now(UTC)
        }

    def get_summary_task(self, task_id: int) -> dict:
        self._cleanup_expired_tasks()

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

    def _cleanup_expired_tasks(self) -> None:
        now = datetime.now(UTC)

        expired_transcription_ids = [
            task_id
            for task_id, task in self.transcriptions.items()
            if now - task["createdAt"] > self.ttl
        ]

        for task_id in expired_transcription_ids:
            del self.transcriptions[task_id]

        expired_summary_ids = [
            task_id
            for task_id, task in self.summaries.items()
            if now - task["createdAt"] > self.ttl
        ]

        for task_id in expired_summary_ids:
            del self.summaries[task_id]