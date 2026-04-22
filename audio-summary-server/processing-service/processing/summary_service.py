from processing.task_storage import InMemoryTaskStorage


class SummaryService:
    def __init__(self, task_storage: InMemoryTaskStorage) -> None:
        self.task_storage = task_storage

    def create_task(self, task_id: int, text: str) -> dict:
        self.task_storage.create_summary_task(task_id)

        try:
            self.task_storage.mark_summary_completed(
                task_id,
                content="1. Тестовая тема\n2. Тестовые ключевые понятия\n3. Тестовый вывод"
            )
        except Exception as error:
            self.task_storage.mark_summary_failed(task_id, str(error))
            raise

        return {
            "taskId": task_id,
            "status": "ACCEPTED"
        }

    def get_task(self, task_id: int) -> dict:
        return self.task_storage.get_summary_task(task_id)