from pathlib import Path

from fastapi import UploadFile

from api.exceptions.handlers import InternalProcessingException, InvalidRequestException
from processing.runtime_cleanup import clear_directory
from processing.task_storage import InMemoryTaskStorage
from processing.transcriber_factory import create_transcriber


class TranscriptionService:
    def __init__(
        self,
        task_storage: InMemoryTaskStorage,
        upload_dir: str = "data/uploads",
        language: str = "ru",
        max_file_size_mb: int = 50
    ) -> None:
        self.task_storage = task_storage
        self.upload_dir = Path(upload_dir)
        self.language = language
        self.max_file_size_bytes = max_file_size_mb * 1024 * 1024
        self.allowed_extensions = {".mp3", ".wav", ".m4a"}

        clear_directory(str(self.upload_dir))

    async def create_task(
        self,
        task_id: int,
        file: UploadFile,
        model_name: str = "tone"
    ) -> dict:
        if file.filename is None or file.filename == "":
            raise InvalidRequestException("Uploaded file must have a filename")

        original_file_path = Path(file.filename)
        extension = original_file_path.suffix.lower()

        if extension not in self.allowed_extensions:
            raise InvalidRequestException(
                "Unsupported audio format. Allowed formats: .mp3, .wav, .m4a"
            )

        content = await file.read()

        if len(content) == 0:
            raise InvalidRequestException("Uploaded file is empty")

        if len(content) > self.max_file_size_bytes:
            raise InvalidRequestException(
                f"Uploaded audio file exceeds the maximum allowed size of "
                f"{self.max_file_size_bytes // (1024 * 1024)} MB"
            )

        self.task_storage.create_transcription_task(task_id)

        saved_file_path = self.upload_dir / f"{task_id}{extension}"

        try:
            saved_file_path.write_bytes(content)

            transcriber = create_transcriber(model_name)

            result = transcriber.transcribe(
                str(saved_file_path),
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
        finally:
            if saved_file_path.exists():
                saved_file_path.unlink()

        return {
            "taskId": task_id,
            "status": "ACCEPTED"
        }

    def get_task(self, task_id: int) -> dict:
        return self.task_storage.get_transcription_task(task_id)