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
            "status": "IN_PROGRESS"
        }

    def get_transcription_task(self, task_id: int) -> dict:
        task = self.transcriptions.get(task_id)

        if task is None:
            raise TaskNotFoundException(task_id)

        return task

    def create_summary_task(self, task_id: int) -> None:
        if task_id in self.summaries:
            raise TaskAlreadyExistsException(task_id)

        self.summaries[task_id] = {
            "taskId": task_id,
            "status": "IN_PROGRESS"
        }

    def get_summary_task(self, task_id: int) -> dict:
        task = self.summaries.get(task_id)

        if task is None:
            raise TaskNotFoundException(task_id)

        return task