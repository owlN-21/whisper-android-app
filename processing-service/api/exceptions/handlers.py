from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from api.schemas.error_schema import ApiErrorResponse


class ApiException(Exception):
    def __init__(
        self,
        status_code: int,
        description: str,
        code: str,
        exception_name: str | None = None,
        exception_message: str | None = None
    ) -> None:
        self.status_code = status_code
        self.description = description
        self.code = code
        self.exception_name = exception_name
        self.exception_message = exception_message


class TaskNotFoundException(ApiException):
    def __init__(self, task_id: int) -> None:
        super().__init__(
            status_code=404,
            description="Task not found",
            code="TASK_NOT_FOUND",
            exception_name="TaskNotFoundException",
            exception_message=f"Task with id {task_id} was not found"
        )


class TaskAlreadyExistsException(ApiException):
    def __init__(self, task_id: int) -> None:
        super().__init__(
            status_code=409,
            description="Task already exists",
            code="TASK_ALREADY_EXISTS",
            exception_name="TaskAlreadyExistsException",
            exception_message=f"Task with id {task_id} already exists"
        )


class InvalidRequestException(ApiException):
    def __init__(self, message: str) -> None:
        super().__init__(
            status_code=400,
            description="Invalid request",
            code="INVALID_REQUEST",
            exception_name="InvalidRequestException",
            exception_message=message
        )


class InternalProcessingException(ApiException):
    def __init__(self, message: str) -> None:
        super().__init__(
            status_code=500,
            description="Internal processing error",
            code="INTERNAL_PROCESSING_ERROR",
            exception_name="InternalProcessingException",
            exception_message=message
        )


def register_exception_handlers(app: FastAPI) -> None:
    @app.exception_handler(ApiException)
    async def handle_api_exception(
        request: Request,
        exc: ApiException
    ) -> JSONResponse:
        response = ApiErrorResponse(
            description=exc.description,
            code=exc.code,
            exceptionName=exc.exception_name,
            exceptionMessage=exc.exception_message,
            stacktrace=None
        )

        return JSONResponse(
            status_code=exc.status_code,
            content=response.model_dump()
        )