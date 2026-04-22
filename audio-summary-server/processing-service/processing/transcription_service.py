from pathlib import Path

from fastapi import UploadFile

from api.exceptions.handlers import InvalidRequestException
from processing.task_storage import InMemoryTaskStorage


class TranscriptionService:
    def __init__(
        self,
        task_storage: InMemoryTaskStorage,
        upload_dir: str = "data/uploads"
    ) -> None:
        self.task_storage = task_storage
        self.upload_dir = Path(upload_dir)
        self.upload_dir.mkdir(parents=True, exist_ok=True)

    async def create_task(self, task_id: int, file: UploadFile) -> dict:
        if not file.filename:
            raise InvalidRequestException("Uploaded file must have a filename")

        self.task_storage.create_transcription_task(task_id)

        file_path = self.upload_dir / f"{task_id}_{file.filename}"

        content = await file.read()
        file_path.write_bytes(content)

        return {
            "taskId": task_id,
            "status": "ACCEPTED"
        }

    def get_task(self, task_id: int) -> dict:
        return self.task_storage.get_transcription_task(task_id)