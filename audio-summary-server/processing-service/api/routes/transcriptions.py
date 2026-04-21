from fastapi import APIRouter, File, Form, UploadFile, status

from api.schemas.common_schema import ProcessingAcceptedResponse
from api.schemas.transcription_schema import (
    TranscriptionInProgressResponse,
    TranscriptionResultResponse
)

router = APIRouter(prefix="/api/v1/transcriptions", tags=["Transcriptions"])


@router.post(
    "",
    status_code=status.HTTP_202_ACCEPTED,
    response_model=ProcessingAcceptedResponse
)
async def create_transcription(
    taskId: int = Form(...),
    file: UploadFile = File(...)
) -> ProcessingAcceptedResponse:
    return ProcessingAcceptedResponse(
        taskId=taskId,
        status="ACCEPTED"
    )


@router.get(
    "/{taskId}",
    response_model=TranscriptionResultResponse
)
async def get_transcription_by_task_id(taskId: int) -> TranscriptionResultResponse:
    return TranscriptionInProgressResponse(
        taskId=taskId,
        status="IN_PROGRESS"
    )