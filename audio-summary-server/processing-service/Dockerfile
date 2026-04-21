FROM tinkoffcreditsystems/t-one:0.1.0

USER root
WORKDIR /app

COPY requirements.txt /app/requirements.txt

RUN python -m ensurepip --upgrade \
    && python -m pip install --no-cache-dir --default-timeout=300 --retries 10 -r /app/requirements.txt

COPY . /app

ENTRYPOINT ["python"]