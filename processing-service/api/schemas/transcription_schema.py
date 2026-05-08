from typing import Literal, Union

from pydantic import BaseModel


class TranscriptionInProgressResponse(BaseModel):
    taskId: int
    status: Literal["IN_PROGRESS"]


class TranscriptionCompletedResponse(BaseModel):
    taskId: int
    status: Literal["COMPLETED"]
    text: str


class TranscriptionFailedResponse(BaseModel):
    taskId: int
    status: Literal["FAILED"]
    errorMessage: str


TranscriptionResultResponse = Union[
    TranscriptionInProgressResponse,
    TranscriptionCompletedResponse,
    TranscriptionFailedResponse
]