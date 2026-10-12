# ADR 0003: AI integration

## Status

Accepted (2026-10-12)

## Context

The AI feature of this topic converts free-text meeting notes into candidate tasks with a title, a suggested owner and a rough estimate. The project lead then accepts or rejects each suggestion individually, and nothing is created in the plan without the lead's approval.

There were some things that needed to be considered before the implementation could start:

- The feature must be reachable through both a screen and a REST endpoint
- Tests must use a **stub**, never the real AI service, to avoid costs and rate limits
- In the deployed instance, the feature must work against the real provider, with a request cap, and the API key must never appear in the repository or in a response
- A model call takes several seconds, can fail, and can return output that doesn't make sense. The application has to handle all of these cases

## Decision

### 1. Google Gemini, free tier

The application uses a Gemini Flash model through the Gemini API free tier.

> The exact model name is set in configuration, not in code, so it can be changed without a new build.

The API key comes from the environment variable `GEMINI_API_KEY` and is only read in the `prod` profile.

### 2. Spring AI behind an interface, with a stub

The AI is exposed to the application only through an interface:

```java
public interface MeetingNoteAnalyzer {
    List<TaskCandidate> analyze(String notes, List<String> memberNames);
}
```

There are two implementations:

- `GeminiMeetingNoteAnalyzer` which uses Spring AI's `ChatClient`. The model's answer is parsed directly into Java records: `TaskCandidate(title, ownerName, estimateHours, sourceExcerpt)`. Spring AI generates the JSON schema from the record and sends it with the request.
- `StubMeetingNoteAnalyzer` returns fixed suggestions. It is used in all tests and in the `stub` profile, so every screen works without the provider.

The prompt is stored in a resource file, not in Java code, so changes to it are visible in Git. It tells the model to extract only tasks that are actually mentioned in the notes, to choose owners only from the list of project members, and to give estimates in hours.

> **The output is never trusted as is**. Every suggestion is validated: the title must not be empty, the estimate must be a reasonable number of hours (or empty), and there can be at most 20 suggestions. Invalid suggestions are dropped. The owner name is matched against the project's members; if there is no match, `suggestedOwner` stays empty and the lead chooses someone.

### 3. Background processing

When the lead submits notes:

1. The `MeetingNote` is saved with `aiStatus = PENDING`, and the lead immediately sees the note with an "analysing…" status.
2. After the transaction commits, an event starts the analysis on a separate small thread pool (`@TransactionalEventListener + @Async`).
3. The result is saved as `TaskSuggestion` rows, and the note becomes `DONE`. If something goes wrong, it becomes `FAILED`, with the reason in `aiErrorMessage`.

The page refreshes the status until the analysis is finished. The model call has a timeout, and a temporary error (timeout or server error) is retried once. A `429` from Google (limit reached) is not retried; the note is marked `FAILED` with a clear message instead.

If the application restarts during an analysis, the note would stay `PENDING` forever, so a scheduled job marks notes that have been `PENDING` for too long as `FAILED`.

### 4. Request cap and review

- Each user can analyse at most 10 notes per day. This is counted from existing `MeetingNote` rows (`createdBy`, `createdAt`), so no extra table is needed.
- A note can have at most **10,000 characters**.
- A global daily limit, set in configuration, stays below Google's own free-tier limit.

When a limit is reached, the screen shows a message and the REST API returns `429` with a `ProblemDetail`.

**Review**: suggestions start as `PENDING`. Only the project lead can accept or reject them, one at a time, and can edit the title, owner and estimate before accepting. Accepting creates the `Task` and records `reviewedBy`, `reviewedAt` and `createdTask` on the suggestion.

## Alternatives rejected

### 1. A paid provider (Anthropic or OpenAI) with prepaid credits

Prepaid credits would remove the risk of an unexpected bill, and these providers don't have the EEA restriction described in the consequences. They were rejected because the project is a university assignment, and the free tier of Gemini is sufficient for the expected usage.

### 2. A local model with Ollama

Free, and the notes would never leave the server. But a language model needs far more memory than the free hosting plan offers (512 MB), and the deployed instance must use a real provider.

### 3. A plain HTTP client instead of Spring AI

This would mean writing the requests, the JSON schema and the response parsing by hand, and the code would be tied to one provider. With Spring AI, switching providers is mostly a configuration change.

### 4. Calling the model during the request

Simpler, but the lead would wait several seconds with the page loading, and a slow response could make the whole request time out.


### 5. A message queue (RabbitMQ, Kafka) for the background work

More robust, but it is additional infrastructure that isn't available on free hosting and is far more than this feature needs.

### 6. Asking the model for plain text and parsing it

Free text would have to be parsed with string operations or regular expressions, which breaks as soon as the model changes its wording. Structured output with a schema is much more reliable.

### 7. Creating tasks directly from the AI output

This would break the main rule of the topic (nothing is created without the lead's click), and AI output written into the domain without review is also a deduction in the grading rules.

## Consequences

### Gained

1. The provider can be changed through configuration, without touching the rest of the application.
2. Tests never call the real provider, so they are fast, free and always give the same result.
3. The lead never waits for the model, and every failure is visible as `FAILED` with a reason.
4. Model output is validated before it is saved, and nothing reaches the plan without a recorded human decision.
5. The request cap protects the free quota from a single user or a bug.

### Accepted costs

1. Free-tier limits are low. If Google's limit is reached during a demo, the feature fails, so the stub profile is kept ready as a fallback.
2. Google's terms ask for paid services when an app is made available to users in the EEA. Croatia is in the EEA, so using the free tier for a public deployment has to be confirmed with the course teacher. Until then, this is a known limitation. Only demo data is used in the deployed instance.
3. Background processing is more complex than a direct call: the status has to be shown and refreshed, and analyses interrupted by a restart have to be cleaned up by a scheduled job.
4. The model's answers are not deterministic, so its quality can't be checked with exact assertions. It is evaluated separately in docs/ai-evaluation.md.
