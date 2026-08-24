# LLM Prompts and Fallbacks

## Pre-visit prompt

```text
You are a clinical intake assistant, not a diagnosing clinician. Return only JSON with keys urgencyLevel (LOW, MEDIUM, or HIGH), chiefComplaint, suggestedQuestions (exactly 3 strings). Flag emergency red symptoms HIGH. Do not diagnose or invent facts. Symptoms: <patient symptoms>
```

Validated response contract:

```json
{"urgencyLevel":"LOW|MEDIUM|HIGH","chiefComplaint":"string","suggestedQuestions":["string","string","string"]}
```

## Post-visit prompt

```text
Convert the clinical notes into plain, patient-friendly language. Return only JSON with keys visitSummary, medicationSchedule (string array), followUpSteps (string array). Preserve dosages exactly, never add medication or advice, and state when information is missing. Clinical notes: <notes>
Prescription: <clinician-entered prescription>
```

## Failure handling

1. Primary Gemini model with 3-second connect and 12-second response timeout.
2. One controlled retry with short backoff.
3. Secondary Gemini model and the same validation.
4. Exact-input SHA-256 cache.
5. Deterministic rules: red-flag urgency vocabulary, extractive complaint/notes, fixed safe questions and follow-up language.
6. Appointment-service local fallback if the AI microservice itself is unreachable.

Invalid JSON, missing required fields, the wrong question count, API errors, timeouts, empty responses, and missing API keys all enter the fallback chain. After repeated provider failure, a 60-second circuit cooldown prevents repeated slow calls. The application still books the appointment and visibly labels the output source.

No prompt asks the model to diagnose. Medication is generated only from clinician-entered prescription text. Every summary carries a clinician-review notice.
