from processing.task_storage import InMemoryTaskStorage


class TranscriptionService:
    def __init__(self, task_storage: InMemoryTaskStorage) -> None:
        self.task_storage = task_storage

    def create_task(self, task_id: int) -> dict:
        self.task_storage.create_transcription_task(task_id)
        return {
            "taskId": task_id,
            "status": "ACCEPTED"
        }

    def get_task(self, task_id: int) -> dict:
        return self.task_storage.get_transcription_task(task_id)