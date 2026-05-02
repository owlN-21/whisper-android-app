import argparse

from asr.storage.transcription_writer import TranscriptionWriter
from asr.tone.tone_transcriber import ToneTranscriber
from asr.whisper.whisper_transcriber import WhisperTranscriber
from llm.service.text_summarizer import TextSummarizer, SummarizationError
from llm.storage.summary_writer import SummaryWriter


def create_transcriber(model_name: str):
    if model_name == "whisper":
        return WhisperTranscriber(
            model_size="small",
            device="cpu",
            compute_type="int8"
        )

    if model_name == "tone":
        return ToneTranscriber(
            model_dir="/models",
            use_beam_search=True
        )

    raise ValueError(f"unsupported ASR model: {model_name}")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--asr", choices=["whisper", "tone"], default="tone")
    parser.add_argument("--audio", default="audio.mp3")
    parser.add_argument("--transcription-output", default="output.txt")
    parser.add_argument("--summary-output", default="summary.txt")
    parser.add_argument("--language", default="ru")
    args = parser.parse_args()

    print("1. start")
    print(f"2. audio path = {args.audio}")

    transcriber = create_transcriber(args.asr)
    print(f"3. ASR model created: {args.asr}")

    transcription_writer = TranscriptionWriter()

    try:
        result = transcriber.transcribe(args.audio, language=args.language)
        print("4. transcription finished")
    except Exception as error:
        print(f"transcription failed: {error}")
        return

    try:
        transcription_writer.write_plain_text(result, args.transcription_output)
        print("5. transcription saved")
    except OSError as error:
        print(f"transcription saving failed: {error}")
        return

    summarizer = TextSummarizer(model_name="qwen2.5:7b")
    print("6. LLM model ready")

    summary_writer = SummaryWriter()

    try:
        summary_result = summarizer.summarize(result.text)
        print("7. summarization finished")
    except SummarizationError as error:
        print(f"summarization failed: {error}")
        return

    try:
        summary_writer.write_text(summary_result, args.summary_output)
        print("8. summary saved")
    except OSError as error:
        print(f"summary saving failed: {error}")
        return

    print("9. done")


if __name__ == "__main__":
    main()