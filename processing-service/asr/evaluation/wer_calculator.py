from typing import List


class WerCalculator:
    def calculate(self, reference_text: str, hypothesis_text: str) -> float:
        reference_words = self._tokenize(reference_text)
        hypothesis_words = self._tokenize(hypothesis_text)

        if not reference_words:
            return 0.0 if not hypothesis_words else 1.0

        distance = self._levenshtein_distance(reference_words, hypothesis_words)
        wer = distance / len(reference_words)

        return min(wer, 1.0)

    def _tokenize(self, text: str) -> List[str]:
        if not text:
            return []
        return text.split()

    def _levenshtein_distance(
        self,
        reference_words: List[str],
        hypothesis_words: List[str],
    ) -> int:
        rows = len(reference_words) + 1
        cols = len(hypothesis_words) + 1

        matrix = [[0] * cols for _ in range(rows)]

        for row in range(rows):
            matrix[row][0] = row

        for col in range(cols):
            matrix[0][col] = col

        for row in range(1, rows):
            for col in range(1, cols):
                substitution_cost = 0
                if reference_words[row - 1] != hypothesis_words[col - 1]:
                    substitution_cost = 1

                matrix[row][col] = min(
                    matrix[row - 1][col] + 1,
                    matrix[row][col - 1] + 1,
                    matrix[row - 1][col - 1] + substitution_cost,
                )

        return matrix[-1][-1]