from fastapi import APIRouter
from pydantic import BaseModel
from fastapi import status

router = APIRouter(prefix="/api/v1/summaries", tags=["Summaries"])


class CreateSummaryRequest(BaseModel):
    taskId: int
    text: str


@router.post("", status_code=status.HTTP_202_ACCEPTED)
async def create_summary(request: CreateSummaryRequest) -> dict:
    return {
        "taskId": request.taskId,
        "status": "ACCEPTED"
    }


@router.get("/{taskId}")
async def get_summary_by_task_id(taskId: int) -> dict:
    return {
        "taskId": taskId,
        "status": "IN_PROGRESS"
    }