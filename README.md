# Spring AI OpenClaw Starter

Spring Boot Starter for [OpenClaw Gateway](https://github.com/openclaw-ai/openclaw) integration with Spring AI.

## Features

- **Chat Completions API** - OpenAI-compatible `/v1/chat/completions` endpoint
- **OpenResponses API** - Enriched input support with text, images, and files via `/v1/responses`
- **EmbeddingModel** - Text embedding generation support
- **Agent-first routing** - Target specific agents via model field (`openclaw/default`, `openclaw/<agentId>`)

## Dependencies

```xml
<dependency>
    <groupId>io.github.partmeai</groupId>
    <artifactId>spring-ai-starter-model-openclaw</artifactId>
    <version>3.5.x.20260623-SNAPSHOT</version>
</dependency>
```

## Configuration

```yaml
spring:
  ai:
    openclaw:
      enabled: true
      base-url: http://localhost:18789
      # Chat Completions options
      chat:
        model: openclaw/default
        temperature: 0.7
        max-completion-tokens: 2048
      # Embedding options
      embedding:
        model: openclaw/default
      # Responses API options
      responses:
        model: openclaw/default
        temperature: 0.7
```

### Configuration Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `spring.ai.openclaw.enabled` | boolean | `true` | Enable/disable auto-configuration |
| `spring.ai.openclaw.base-url` | String | `http://localhost:18789` | OpenClaw Gateway URL |
| `spring.ai.openclaw.chat.model` | String | `openclaw/default` | Default model/agent target |
| `spring.ai.openclaw.chat.temperature` | Double | - | Sampling temperature (0.0-2.0) |
| `spring.ai.openclaw.chat.max-completion-tokens` | Integer | - | Max completion tokens |
| `spring.ai.openclaw.chat.max-tokens` | Integer | - | Max tokens (legacy) |
| `spring.ai.openclaw.chat.top-p` | Double | - | Nucleus sampling probability |
| `spring.ai.openclaw.chat.frequency-penalty` | Double | - | Frequency penalty |
| `spring.ai.openclaw.chat.presence-penalty` | Double | - | Presence penalty |
| `spring.ai.openclaw.chat.stop` | List<String> | - | Stop sequences |
| `spring.ai.openclaw.chat.model-override` | String | - | Backend model override |
| `spring.ai.openclaw.embedding.model` | String | `openclaw/default` | Embedding model |
| `spring.ai.openclaw.responses.model` | String | `openclaw/default` | Responses API model |

## Usage

### ChatModel

```java
@Autowired
private ChatModel chatModel;

public String chat(String message) {
    ChatResponse response = chatModel.call(new UserMessage(message));
    return response.getResult().getOutput().getText();
}
```

### EmbeddingModel

```java
@Autowired
private EmbeddingModel embeddingModel;

public float[] embed(String text) {
    EmbeddingResponse response = embeddingModel.embed(new EmbeddingRequest(List.of(text), EmbeddingOptions.DEFAULT_MODEL));
    return response.getResult().getOutput().get(0).getOutput();
}
```

### OpenClawResponsesModel

```java
@Autowired
private OpenClawResponsesModel responsesModel;

public String responsesApi(String prompt) {
    ResponseResult result = responsesModel.call(prompt);
    return OpenClawResponsesModel.extractText(result);
}
```

## License

Apache License 2.0
