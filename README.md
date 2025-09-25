# 📝 Spring Boot LLM Text Summarizer

A simple REST API built with **Spring Boot** that generates concise summaries for input text.  
The service supports multiple LLM providers (Google Gemini by default, optional OpenAI).

---

## 🚀 Features
- `/api/summarize` REST endpoint
- Accepts JSON input text with configurable `maxSentences`
- Returns a summarized response
- Built with **Spring Boot 3 + Java 17 + Gradle**
- Ready to support multiple LLM providers (Gemini, OpenAI)

---

## 🛠️ Tech Stack
- **Java 17**
- **Spring Boot 3**
- **Gradle**
- **Google Gemini API** (default provider, free tier available)
- **(Optional) OpenAI GPT API**
- **WebFlux (WebClient)** for non-blocking HTTP calls
- **Lombok** for reducing boilerplate

---

## ⚙️ Setup Instructions

### 1. Clone this repository
```bash
git clone https://github.com/your-username/spring-boot-llm-text-summarizer.git
cd spring-boot-llm-text-summarizer
```

### 2. Update API Keys in `application.yml`
Your project already has an `application.yml` under `src/main/resources`.  
Update it with the following configuration:

```yaml
server:
  port: 8080

llm:
  temperature: 0.2
  max-tokens: 200

gemini:
  base-url: https://generativelanguage.googleapis.com
  api-key: ${GEMINI_API_KEY}
  model: gemini-1.5-flash

openai:
  base-url: https://api.openai.com/v1
  api-key: ${OPENAI_API_KEY}
  model: gpt-4o-mini
```

👉 For local dev with Gemini (free tier):
```bash
export GEMINI_API_KEY=your_key_here
```

---

## ▶️ Run the Application
```bash
./gradlew bootRun
```

---

## 📡 Example Request

**POST** `http://localhost:8080/api/summarize`

Request body:
```json
{
  "text": "Spring Boot is an open source Java-based framework used to create stand-alone, production-grade applications. It simplifies development by handling boilerplate configurations.",
  "maxSentences": 2
}
```

Response:
```json
{
  "summary": "Spring Boot is a Java framework that simplifies creating production-ready applications by reducing configuration overhead."
}
```

---

## ✅ Next Steps
- Add tests to validate summarizer logic
- Extend support for more providers (Anthropic, Cohere, etc.)
- Deploy to cloud (Heroku / AWS / GCP)

---

📌 *This project is for educational and portfolio purposes. Do not commit real API keys to GitHub.*
