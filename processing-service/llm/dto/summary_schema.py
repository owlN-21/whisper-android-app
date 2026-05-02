from pydantic import BaseModel, Field


class SummarySchema(BaseModel):
    topic: str = Field(description="Тема текста")
    short_summary: str = Field(description="Краткое содержание текста")
    main_ideas: list[str] = Field(description="Список основных идей")
    conclusion: str = Field(description="Итоговый вывод")