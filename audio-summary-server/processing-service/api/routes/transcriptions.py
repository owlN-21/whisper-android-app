from fastapi import APIRouter, File, Form, UploadFile, status

from api.schemas.common_schema import ProcessingAcceptedResponse
from api.schemas.error_schema import ApiErrorResponse
from api.schemas.transcription_schema import (
    TranscriptionCompletedResponse,
    TranscriptionFailedResponse,
    TranscriptionInProgressResponse,
    TranscriptionResultResponse
)
from processing.dependencies import transcription_service
from typing import Literal

router = APIRouter(prefix="/api/v1/transcriptions", tags=["Transcriptions"])


@router.post(
    "",
    status_code=status.HTTP_202_ACCEPTED,
    response_model=ProcessingAcceptedResponse,
    responses={
        400: {"model": ApiErrorResponse, "description": "Invalid transcription request"},
        409: {"model": ApiErrorResponse, "description": "Task already exists"},
        500: {"model": ApiErrorResponse, "description": "Internal processing error"}
    }
)
async def create_transcription(
    taskId: int = Form(...),
    file: UploadFile = File(...),
    model: Literal["tone", "whisper"] = Form("tone")
) -> ProcessingAcceptedResponse:
    result = await transcription_service.create_task(taskId, file, model)

    return ProcessingAcceptedResponse(
        taskId=result["taskId"],
        status=result["status"]
    )

@router.get(
    "/{taskId}",
    response_model=TranscriptionResultResponse,
    responses={
        404: {"model": ApiErrorResponse, "description": "Task not found"},
        500: {"model": ApiErrorResponse, "description": "Internal processing error"}
    }
)
async def get_transcription_by_task_id(taskId: int) -> TranscriptionResultResponse:
    task = transcription_service.get_task(taskId)

    if task["status"] == "IN_PROGRESS":
        return TranscriptionInProgressResponse(
            taskId=task["taskId"],
            status=task["status"]
        )

    if task["status"] == "COMPLETED":
        return TranscriptionCompletedResponse(
            taskId=task["taskId"],
            status=task["status"],
            text=task["text"]
        )

    return TranscriptionFailedResponse(
        taskId=task["taskId"],
        status=task["status"],
        errorMessage=task["errorMessage"]
    )