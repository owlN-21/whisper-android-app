from fastapi import APIRouter, status

from api.schemas.common_schema import ProcessingAcceptedResponse
from api.schemas.error_schema import ApiErrorResponse
from api.schemas.summary_schema import (
    CreateSummaryRequest,
    SummaryInProgressResponse,
    SummaryResultResponse
)
from processing.dependencies import summary_service

router = APIRouter(prefix="/api/v1/summaries", tags=["Summaries"])


@router.post(
    "",
    status_code=status.HTTP_202_ACCEPTED,
    response_model=ProcessingAcceptedResponse,
    responses={
        400: {"model": ApiErrorResponse, "description": "Invalid summary request"},
        409: {"model": ApiErrorResponse, "description": "Task already exists"},
        500: {"model": ApiErrorResponse, "description": "Internal summarization error"}
    }
)
async def create_summary(
    request: CreateSummaryRequest
) -> ProcessingAcceptedResponse:
    result = summary_service.create_task(request.taskId)

    return ProcessingAcceptedResponse(
        taskId=result["taskId"],
        status=result["status"]
    )


@router.get(
    "/{taskId}",
    response_model=SummaryResultResponse,
    responses={
        404: {"model": ApiErrorResponse, "description": "Task not found"},
        500: {"model": ApiErrorResponse, "description": "Internal summarization error"}
    }
)
async def get_summary_by_task_id(taskId: int) -> SummaryResultResponse:
    task = summary_service.get_task(taskId)

    return SummaryInProgressResponse(
        taskId=task["taskId"],
        status=task["status"]
    )