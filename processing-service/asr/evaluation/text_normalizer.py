import re
import string
from dataclasses import dataclass
from typing import Optional


@dataclass
class NormalizationConfig:
    lowercase: bool = True
    replace_yo: bool = True
    remove_punctuation: bool = True
    collapse_spaces: bool = True


class TextNormalizer:
    def __init__(self, config: Optional[NormalizationConfig] = None) -> None:
        self.config = config or NormalizationConfig()
        punctuation_chars = string.punctuation + "«»—…"
        self.translation_table = str.maketrans("", "", punctuation_chars)

    def normalize(self, text: Optional[str]) -> str:
        if not text:
            return ""

        result = text

        if self.config.lowercase:
            result = result.lower()

        if self.config.replace_yo:
            result = result.replace("ё", "е")

        if self.config.remove_punctuation:
            result = result.translate(self.translation_table)

        if self.config.collapse_spaces:
            result = re.sub(r"\s+", " ", result).strip()

        return result