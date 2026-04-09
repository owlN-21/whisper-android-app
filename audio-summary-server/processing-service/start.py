from service.audio_transcriber import AudioTranscriber
from storage.transcription_writer import TranscriptionWriter

def main() -> None:
    print("1. start")

    audio_path = "audio.mp3"
    output_path = "output.txt"
    print(f"2. audio path = {audio_path}")

    transcriber = AudioTranscriber(
        model_size="small",
        device="cpu",
        compute_type="int8"
    )
    print("3. model created")

    writer = TranscriptionWriter()

    result = transcriber.transcribe(audio_path, language="ru")
    print("4. transcription finished")

    writer.write_plain_text(result, output_path)
    print("5. record finished")


if __name__ == "__main__":
    main()