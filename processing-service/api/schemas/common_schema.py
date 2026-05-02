from typing import Literal

from pydantic import BaseModel


class ProcessingAcceptedResponse(BaseModel):
    taskId: int
    status: Literal["ACCEPTED"]