from typing import Literal, Union

from pydantic import BaseModel


class CreateSummaryRequest(BaseModel):
    taskId: int
    text: str


class SummaryInProgressResponse(BaseModel):
    taskId: int
    status: Literal["IN_PROGRESS"]


class SummaryCompletedResponse(BaseModel):
    taskId: int
    status: Literal["COMPLETED"]
    content: str


class SummaryFailedResponse(BaseModel):
    taskId: int
    status: Literal["FAILED"]
    errorMessage: str


SummaryResultResponse = Union[
    SummaryInProgressResponse,
    SummaryCompletedResponse,
    SummaryFailedResponse
]