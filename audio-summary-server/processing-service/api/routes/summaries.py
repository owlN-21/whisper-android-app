from fastapi import APIRouter, status

from api.schemas.summary_schema import (
    CreateSummaryRequest,
    ProcessingAcceptedResponse,
    SummaryCompletedResponse,
    SummaryInProgressResponse,
    SummaryResultResponse
)

router = APIRouter(prefix="/api/v1/summaries", tags=["Summaries"])


@router.post(
    "",
    status_code=status.HTTP_202_ACCEPTED,
    response_model=ProcessingAcceptedResponse
)
async def create_summary(
    request: CreateSummaryRequest
) -> ProcessingAcceptedResponse:
    return ProcessingAcceptedResponse(
        taskId=request.taskId,
        status="ACCEPTED"
    )


@router.get(
    "/{taskId}",
    response_model=SummaryResultResponse
)
async def get_summary_by_task_id(taskId: int) -> SummaryResultResponse:
    return SummaryInProgressResponse(
        taskId=taskId,
        status="IN_PROGRESS"
    )