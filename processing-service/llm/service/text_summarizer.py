import json
import os

import httpx
from ollama import Client, ResponseError

from llm.dto.summary_result import SummaryResult


class SummarizationError(Exception):
    """Базовая ошибка суммаризации."""


class SummarizerUnavailableError(SummarizationError):
    """Ollama недоступна."""


class SummarizerTimeoutError(SummarizationError):
    """Превышено время ожидания ответа от модели."""


class SummarizerModelError(SummarizationError):
    """Ошибка модели или запроса к модели."""


class SummarizerEmptyResponseError(SummarizationError):
    """Модель вернула пустой ответ."""


class SummarizerFormatError(SummarizationError):
    """Модель вернула ответ в неверном формате."""


class SummarizerLanguageError(SummarizationError):
    """Модель вернула текст не на русском языке."""


class TextSummarizer:
    def __init__(
        self,
        model_name: str = "qwen2.5:7b",
        host: str | None = None,
        timeout: float = 3000.0
    ) -> None:
        self.model_name = model_name
        if host is None:
            host = os.getenv("OLLAMA_HOST", "http://localhost:11434")

        self.host = host
        self.client = Client(host=host, timeout=timeout)

    def summarize(self, source_text: str) -> SummaryResult:
        cleaned_text = source_text.strip()
        if not cleaned_text:
            raise ValueError("source_text is empty")

        prompt = self._build_prompt(cleaned_text)
        schema = self._build_schema()

        raw_content = self._request_summary(prompt, schema)
        data = self._parse_json(raw_content)

        self._validate_summary_data(data)

        final_text = self._render_summary(data)
        return SummaryResult(text=final_text)

    def _request_summary(self, prompt: str, schema: dict) -> str:
        try:
            response = self.client.chat(
                model=self.model_name,
                messages=[
                    {
                        "role": "system",
                        "content": (
                            "Ты помощник, который делает структурированные конспекты. "
                            "Отвечай строго только на русском языке. "
                            "Верни только JSON по заданной схеме. "
                            "Не добавляй пояснений, markdown и лишнего текста."
                        )
                    },
                    {
                        "role": "user",
                        "content": prompt
                    }
                ],
                format=schema,
                options={
                    "temperature": 0.0,
                    "seed": 42
                }
            )
        except httpx.ConnectError as error:
            raise SummarizerUnavailableError(
                f"Ollama is not available at {self.host}"
            ) from error
        except httpx.TimeoutException as error:
            raise SummarizerTimeoutError(
                "Ollama response timed out"
            ) from error
        except ResponseError as error:
            raise SummarizerModelError(
                f"Ollama returned an error: {error.error}"
            ) from error
        except httpx.HTTPError as error:
            raise SummarizerUnavailableError(
                f"HTTP error while contacting Ollama: {error}"
            ) from error

        content = response.message.content.strip()
        if not content:
            raise SummarizerEmptyResponseError("Model returned empty content")

        return content

    def _build_prompt(self, source_text: str) -> str:
        return f"""
        Сделай структурированный конспект текста.

        Правила:
        1. Пиши строго на русском языке.
        2. Не используй китайский, английский и другие языки.
        3. Не вставляй иероглифы, иностранные слова и фразы.
        4. Сохрани только важную информацию.
        5. Убери повторы и лишние детали.
        6. Не добавляй ничего от себя.
        7. Верни только JSON по схеме ниже.
        8. В поле ideas верни список ключевых моментов без жёсткого ограничения по количеству.


        JSON schema:
        {json.dumps(self._build_schema(), ensure_ascii=False, indent=2)}

        Исходный текст:
        {source_text}
        """.strip()

    def _build_schema(self) -> dict:
        return {
            "type": "object",
            "properties": {
                "topic": {"type": "string"},
                "summary": {"type": "string"},
                "ideas": {
                    "type": "array",
                    "items": {"type": "string"}
                },
                "conclusion": {"type": "string"}
            },
            "required": ["topic", "summary", "ideas", "conclusion"]
        }

    def _parse_json(self, raw_content: str) -> dict:
        try:
            return json.loads(raw_content)
        except json.JSONDecodeError as error:
            raise SummarizerFormatError(
                f"Model returned invalid JSON: {error}"
            ) from error

    def _validate_summary_data(self, data: dict) -> None:
        if not isinstance(data, dict):
            raise SummarizerFormatError("Summary JSON must be an object")

        topic = data.get("topic", "").strip()
        summary = data.get("summary", "").strip()
        ideas = data.get("ideas", [])
        conclusion = data.get("conclusion", "").strip()

        if not topic or not summary or not conclusion:
            raise SummarizerFormatError("Required fields are empty")

        if not isinstance(ideas, list) or not ideas:
            raise SummarizerFormatError("ideas must be a non-empty list")

        if any(not isinstance(item, str) or not item.strip() for item in ideas):
            raise SummarizerFormatError("Each item in ideas must be non-empty text")

        joined_text = " ".join([topic, summary, conclusion, *ideas])

        if self._contains_cjk(joined_text):
            raise SummarizerLanguageError(
                "Model returned Chinese characters"
            )

        if "assistant:" in joined_text.lower():
            raise SummarizerFormatError(
                "Model returned dialogue artifact: Assistant:"
            )

    def _render_summary(self, data: dict) -> str:
        ideas_text = "\n".join(
            f"{index}. {item.strip()}"
            for index, item in enumerate(data["ideas"], start=1)
        )

        return (
            f"Тема:\n{data['topic'].strip()}\n\n"
            f"Краткое содержание:\n{data['summary'].strip()}\n\n"
            f"Ключевые моменты:\n{ideas_text}\n\n"
            f"Вывод:\n{data['conclusion'].strip()}"
        )

    def _contains_cjk(self, text: str) -> bool:
        return any('\u4e00' <= char <= '\u9fff' for char in text)