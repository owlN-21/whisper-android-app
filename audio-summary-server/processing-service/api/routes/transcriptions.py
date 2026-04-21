from fastapi import APIRouter, File, Form, UploadFile, status

router = APIRouter(prefix="/api/v1/transcriptions", tags=["Transcriptions"])


@router.post("", status_code=status.HTTP_202_ACCEPTED)
async def create_transcription(
    taskId: int = Form(...),
    file: UploadFile = File(...)
) -> dict:
    return {
        "taskId": taskId,
        "status": "ACCEPTED"
    }


@router.get("/{taskId}")
async def get_transcription_by_task_id(taskId: int) -> dict:
    return {
        "taskId": taskId,
        "status": "IN_PROGRESS"
    }