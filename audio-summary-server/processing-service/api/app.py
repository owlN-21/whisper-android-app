from fastapi import FastAPI

from api.routes.summaries import router as summaries_router
from api.routes.transcriptions import router as transcriptions_router

app = FastAPI(
    title="Processing Service API",
    version="1.0.0",
    description="API for two-step processing audio to transcript, transcript to summary"
)

app.include_router(transcriptions_router)
app.include_router(summaries_router)