# English I+1 Sentence Generator API

REST API in Java / Spring Boot that helps studying English using the **i+1**
vocabulary acquisition technique.

The idea: you upload a PDF with the English words you already know, then you ask
for a **target word** you want to learn. The API uses OpenAI to generate one
natural sentence that uses the target word while introducing **at most one**
unknown word, and validates the result before returning it.

## Objective

1. Import a PDF containing English words the user already knows.
2. Extract the words automatically and store them in memory as the known vocabulary.
3. Receive a new word the user wants to learn.
4. Generate a grammatically correct English sentence using that word.
5. Keep the number of unknown words (besides the target word) as low as possible.
6. Validate the generated sentence automatically (i+1 rule).

## Tech stack

| Concern        | Choice                          |
| -------------- | ------------------------------- |
| Language       | Java 21                         |
| Framework      | Spring Boot 4.x                 |
| Build          | Maven (wrapper included)        |
| PDF processing | Apache PDFBox                   |
| AI integration | OpenAI Chat Completions API     |
| Tests          | JUnit 5, Mockito, JaCoCo (>=80%) |

> Persistence: this first version keeps the known vocabulary **in memory** for
> the lifetime of the application. A database can be added later.

## Project structure

```
src/main/java/com/arturrodrigues/english/iplusone/api
├── client      # OpenAI client abstraction + RestClient based implementation
├── config      # OpenAI configuration properties and beans
├── controller  # REST controllers
├── exception   # Custom exceptions + global @RestControllerAdvice
├── model       # Request/response records
├── service     # PDF extraction, vocabulary store, sentence generation
├── util        # Shared text tokenizer
└── validator   # i+1 sentence validation
```

## Requirements

- Java 21
- (Optional) Maven — the project ships with the Maven Wrapper (`./mvnw`).

## Configuring the OpenAI key

The key is read from configuration and must **never** be hard coded.

Set the `OPENAI_API_KEY` environment variable:

```bash
export OPENAI_API_KEY="sk-..."
```

Optional overrides (with their defaults):

| Property / env var               | Default                       |
| -------------------------------- | ----------------------------- |
| `OPENAI_API_KEY`                 | _(empty — required at runtime)_ |
| `OPENAI_BASE_URL`                | `https://api.openai.com/v1`   |
| `OPENAI_MODEL`                   | `gpt-4o-mini`                 |

These are bound in [`application.yml`](src/main/resources/application.yml) under
the `openai.*` namespace.

## Running

```bash
# run the application (listens on http://localhost:8080)
OPENAI_API_KEY="sk-..." ./mvnw spring-boot:run

# or build a jar and run it
./mvnw clean package
OPENAI_API_KEY="sk-..." java -jar target/english.iplusone.api-0.0.1-SNAPSHOT.jar
```

## Running the tests

```bash
./mvnw clean verify
```

`verify` also runs JaCoCo and **fails the build if line coverage drops below
80%**. The HTML coverage report is generated at
`target/site/jacoco/index.html`.

## API

### 1. Import a PDF — `POST /api/vocabulary/import`

`multipart/form-data` with a `file` part.

```bash
curl -X POST http://localhost:8080/api/vocabulary/import \
  -F "file=@my-vocabulary.pdf"
```

Response `201 Created`:

```json
{
  "importedWords": 5237,
  "knownWords": 5237
}
```

### 2. Vocabulary stats — `GET /api/vocabulary/stats`

```bash
curl http://localhost:8080/api/vocabulary/stats
```

```json
{
  "knownWords": 5237
}
```

### 3. List vocabulary — `GET /api/vocabulary`

```bash
curl http://localhost:8080/api/vocabulary
```

```json
{
  "words": ["i", "house", "play", "soccer"]
}
```

### 4. Generate an i+1 sentence — `POST /api/sentences`

```bash
curl -X POST http://localhost:8080/api/sentences \
  -H "Content-Type: application/json" \
  -d '{"targetWord": "although"}'
```

Response `200 OK`:

```json
{
  "targetWord": "although",
  "sentence": "I like soccer although my friends play every week.",
  "unknownWords": [],
  "attempts": 1
}
```

## How it works

### Vocabulary extraction

1. Read the whole text content of the PDF (formatting, page breaks and tabs are ignored).
2. Lower-case everything.
3. Remove punctuation and special characters.
4. Split into words.
5. Remove duplicates and store in a `Set<String>` for ~O(1) lookups.

### Sentence generation & validation (i+1)

1. A prompt is built dynamically with the known vocabulary, the target word and
   a set of rules (one sentence, use the target word, prefer known words, 8–15
   words, return only the sentence...).
2. The sentence returned by the AI is normalized (lower-case, punctuation
   removed) and split into words.
3. For every word: if it is in the known vocabulary **or** it is the target
   word, it is valid; otherwise it counts as an **unknown** word.
4. A sentence is **accepted** when it uses the target word and has **at most one**
   extra unknown word.
5. If the sentence is rejected, the API asks the AI to regenerate, explicitly
   listing the unknown words to avoid — up to **3 attempts**.

## Error handling

Errors are handled globally via a `@RestControllerAdvice` and returned with a
consistent JSON body:

```json
{
  "timestamp": "2025-01-01T00:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Uploaded file is not a valid PDF"
}
```

| Situation                          | HTTP status |
| ---------------------------------- | ----------- |
| Invalid / empty PDF, blank target  | `400`       |
| Could not produce a valid sentence | `422`       |
| OpenAI communication failure/timeout | `502`     |
| Unexpected error                   | `500`       |
