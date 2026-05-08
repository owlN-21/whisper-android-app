from typing import List


class CerCalculator:
    def __init__(self, ignore_spaces: bool = True) -> None:
        self.ignore_spaces = ignore_spaces

    def calculate(self, reference_text: str, hypothesis_text: str) -> float:
        reference_chars = self._tokenize(reference_text)
        hypothesis_chars = self._tokenize(hypothesis_text)

        if not reference_chars:
            return 0.0 if not hypothesis_chars else 1.0

        distance = self._levenshtein_distance(reference_chars, hypothesis_chars)
        cer = distance / len(reference_chars)

        return min(cer, 1.0)

    def _tokenize(self, text: str) -> List[str]:
        if not text:
            return []

        if self.ignore_spaces:
            text = text.replace(" ", "")

        return list(text)

    def _levenshtein_distance(
        self,
        reference_chars: List[str],
        hypothesis_chars: List[str],
    ) -> int:
        rows = len(reference_chars) + 1
        cols = len(hypothesis_chars) + 1

        matrix = [[0] * cols for _ in range(rows)]

        for row in range(rows):
            matrix[row][0] = row

        for col in range(cols):
            matrix[0][col] = col

        for row in range(1, rows):
            for col in range(1, cols):
                substitution_cost = 0
                if reference_chars[row - 1] != hypothesis_chars[col - 1]:
                    substitution_cost = 1

                matrix[row][col] = min(
                    matrix[row - 1][col] + 1,
                    matrix[row][col - 1] + 1,
                    matrix[row - 1][col - 1] + substitution_cost,
                )

        return matrix[-1][-1]