from pathlib import Path

from fastapi import UploadFile

from api.exceptions.handlers import InternalProcessingException, InvalidRequestException
from processing.task_storage import InMemoryTaskStorage
from processing.transcriber_factory import create_transcriber


class TranscriptionService:
    def __init__(
        self,
        task_storage: InMemoryTaskStorage,
        upload_dir: str = "data/uploads",
        language: str = "ru"
    ) -> None:
        self.task_storage = task_storage
        self.upload_dir = Path(upload_dir)
        self.language = language
        self.upload_dir.mkdir(parents=True, exist_ok=True)

    async def create_task(
        self,
        task_id: int,
        file: UploadFile,
        model_name: str = "tone"
    ) -> dict:
        if file.filename is None or file.filename == "":
            raise InvalidRequestException("Uploaded file must have a filename")

        self.task_storage.create_transcription_task(task_id)

        try:
            file_path = self.upload_dir / f"{task_id}_{file.filename}"

            content = await file.read()
            file_path.write_bytes(content)

            transcriber = create_transcriber(model_name)

            result = transcriber.transcribe(
                str(file_path),
                language=self.language
            )

            self.task_storage.mark_transcription_completed(
                task_id,
                text=result.text
            )
        except ValueError as error:
            self.task_storage.mark_transcription_failed(task_id, str(error))
            raise InvalidRequestException(str(error)) from error
        except Exception as error:
            self.task_storage.mark_transcription_failed(task_id, str(error))
            raise InternalProcessingException(str(error)) from error

        return {
            "taskId": task_id,
            "status": "ACCEPTED"
        }

    def get_task(self, task_id: int) -> dict:
        return self.task_storage.get_transcription_task(task_id)