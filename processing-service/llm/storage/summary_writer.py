from llm.dto.summary_result import SummaryResult


class SummaryWriteError(Exception):
    """Ошибка записи конспекта в файл."""


class SummaryWriter:
    def write_text(self, result: SummaryResult, output_path: str) -> None:
        try:
            with open(output_path, "w", encoding="utf-8") as file:
                file.write(result.text)
        except OSError as error:
            raise SummaryWriteError(
                f"Cannot write summary to file: {output_path}"
            ) from error