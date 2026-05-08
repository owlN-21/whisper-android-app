from pathlib import Path


def clear_directory(directory_path: str) -> None:
    directory = Path(directory_path)
    directory.mkdir(parents=True, exist_ok=True)

    for path in directory.iterdir():
        if path.is_file():
            path.unlink()