from pydantic import BaseModel


class ApiErrorResponse(BaseModel):
    description: str
    code: str
    exceptionName: str | None = None
    exceptionMessage: str | None = None
    stacktrace: list[str] | None = None