import argparse
from pathlib import Path

from asr.evaluation.cer.model_cer_evaluation_runner import ModelCerEvaluationRunner
from asr.evaluation.model_evaluation_runner import ModelEvaluationRunner
from asr.tone.tone_transcriber import ToneTranscriber
from asr.whisper.whisper_transcriber import WhisperTranscriber


def create_transcriber(model_name: str):
    if model_name == "tone":
        return ToneTranscriber(
            model_dir="/models",
            use_beam_search=True,
        )

    if model_name == "whisper":
        return WhisperTranscriber(
            model_size="small",
            device="cpu",
            compute_type="int8",
        )

    raise ValueError(f"unsupported model: {model_name}")


def create_runner(
    metric_name: str,
    transcriber,
    data_dir: str,
    prediction_dir: str,
    report_dir: str,
):
    if metric_name == "wer":
        return ModelEvaluationRunner(
            transcriber=transcriber,
            data_dir=data_dir,
            prediction_dir=prediction_dir,
            report_dir=report_dir,
        )

    if metric_name == "cer":
        return ModelCerEvaluationRunner(
            transcriber=transcriber,
            data_dir=data_dir,
            prediction_dir=prediction_dir,
            report_dir=report_dir,
        )

    raise ValueError(f"unsupported metric: {metric_name}")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--asr", required=True, choices=["tone", "whisper"])
    parser.add_argument("--metric", required=True, choices=["wer", "cer"])
    parser.add_argument("--limit", type=int, default=None)
    parser.add_argument("--language", default="ru")
    parser.add_argument("--data-dir", default="/app/data")
    parser.add_argument("--artifacts-dir", default="/app/artifacts")
    args = parser.parse_args()

    print(f"1. start {args.model} {args.metric.upper()} evaluation pipeline")

    transcriber = create_transcriber(args.model)
    print(f"2. model created: {args.model}")

    prediction_dir = (
        Path(args.artifacts_dir) / "metrics" / "predictions" / args.model
    )
    report_dir = (
        Path(args.artifacts_dir) / "metrics" / "reports" / args.metric / args.model
    )

    prediction_dir.mkdir(parents=True, exist_ok=True)
    report_dir.mkdir(parents=True, exist_ok=True)

    runner = create_runner(
        metric_name=args.metric,
        transcriber=transcriber,
        data_dir=args.data_dir,
        prediction_dir=str(prediction_dir),
        report_dir=str(report_dir),
    )

    summary = runner.run(
        language=args.language,
        limit=args.limit,
    )

    print(f"3. predictions saved to: {prediction_dir}")
    print(f"4. report saved to: {report_dir}")

    if args.metric == "wer":
        print(f"average WER: {summary.average_wer:.6f}")
        print(f"average WER percent: {summary.average_wer * 100:.2f}%")

    elif args.metric == "cer":
        print(f"average CER: {summary.average_cer:.6f}")
        print(f"average CER percent: {summary.average_cer * 100:.2f}%")

    print("5. done")


if __name__ == "__main__":
    main()