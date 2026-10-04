package com.hotel.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * GeminiService - Centralized service for communicating with the Google Gemini API (v1beta).
 * Supports official Google AI Studio Gemini API authentication via x-goog-api-key header.
 * 
 * Secure configuration precedence:
 *  1. Environment variable: GEMINI_API_KEY
 *  2. System property: gemini.api.key
 *  3. Fallback environment variable: GOOGLE_API_KEY
 *  4. Fallback system property: google.api.key
 *  5. Windows Registry (HKCU / HKLM Environment)
 *  6. Local uncommitted .env file in workspace or user directory
 * 
 * Never hardcodes or logs API keys.
 */
public class GeminiService {

    private static final String DEFAULT_MODEL = "gemini-3.5-flash-lite";
    private static final String BASE_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(20);

    private static final String[] FALLBACK_MODELS = new String[] {
            "gemini-3.5-flash-lite",
            "gemini-flash-lite-latest",
            "gemini-3.5-flash",
            "gemini-flash-latest",
            "gemini-2.5-flash"
    };

    // Shared persistent HTTP/2 client for fast TCP connection and TLS session reuse
    private static final HttpClient SHARED_HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private final String apiKey;
    private final String model;
    private final String endpoint;
    private final HttpClient httpClient;

    public GeminiService() {
        // 1. Resolve API key securely across all supported scopes
        String resolvedKey = resolveKey();
        this.apiKey = (resolvedKey != null) ? resolvedKey.trim() : "";

        // 2. Resolve model name (configurable via GEMINI_MODEL, defaults to gemini-3.5-flash-lite)
        String envModel = System.getenv("GEMINI_MODEL");
        if (envModel == null || envModel.trim().isEmpty()) {
            envModel = System.getProperty("gemini.model");
        }
        if (envModel == null || envModel.trim().isEmpty()) {
            envModel = resolveFromWindowsRegistry("GEMINI_MODEL");
        }
        if (envModel == null || envModel.trim().isEmpty()) {
            envModel = resolveFromDotEnv("GEMINI_MODEL");
        }
        this.model = (envModel != null && !envModel.trim().isEmpty()) ? envModel.trim() : DEFAULT_MODEL;

        // 3. Resolve endpoint
        String envEndpoint = System.getenv("GEMINI_ENDPOINT");
        if (envEndpoint == null || envEndpoint.trim().isEmpty()) {
            envEndpoint = System.getProperty("gemini.endpoint");
        }
        if (envEndpoint != null && !envEndpoint.trim().isEmpty()) {
            this.endpoint = envEndpoint.trim();
        } else {
            this.endpoint = BASE_API_URL + this.model + ":generateContent";
        }

        // 4. Reuse persistent HTTP/2 Client for zero TLS-handshake overhead
        this.httpClient = SHARED_HTTP_CLIENT;
    }

    /**
     * Parameterized constructor for testing or custom configuration.
     */
    public GeminiService(String apiKey, String model) {
        this.apiKey = (apiKey != null) ? apiKey.trim() : "";
        this.model = (model != null && !model.trim().isEmpty()) ? model.trim() : DEFAULT_MODEL;
        this.endpoint = BASE_API_URL + this.model + ":generateContent";
        this.httpClient = SHARED_HTTP_CLIENT;
    }

    /**
     * Resolves the Gemini API key from environment, system properties, Windows registry, or ignored .env file.
     */
    private static String resolveKey() {
        // Priority 1: GEMINI_API_KEY environment variable (standard for deployed hosting & local CLI)
        String key = System.getenv("GEMINI_API_KEY");
        if (key != null && !key.trim().isEmpty()) {
            return key;
        }

        // Priority 2: gemini.api.key Java System Property
        key = System.getProperty("gemini.api.key");
        if (key != null && !key.trim().isEmpty()) {
            return key;
        }

        // Priority 3: GOOGLE_API_KEY environment variable (standard Google client fallback)
        key = System.getenv("GOOGLE_API_KEY");
        if (key != null && !key.trim().isEmpty()) {
            return key;
        }

        // Priority 4: google.api.key Java System Property
        key = System.getProperty("google.api.key");
        if (key != null && !key.trim().isEmpty()) {
            return key;
        }

        // Priority 5: Windows User/Machine Registry (enables picking up new user environment variables without full restart)
        key = resolveFromWindowsRegistry("GEMINI_API_KEY");
        if (key != null && !key.trim().isEmpty()) {
            return key;
        }
        key = resolveFromWindowsRegistry("GOOGLE_API_KEY");
        if (key != null && !key.trim().isEmpty()) {
            return key;
        }

        // Priority 6: Local uncommitted .env file (for local development, excluded by .gitignore)
        key = resolveFromDotEnv("GEMINI_API_KEY");
        if (key != null && !key.trim().isEmpty()) {
            return key;
        }
        key = resolveFromDotEnv("GOOGLE_API_KEY");
        if (key != null && !key.trim().isEmpty()) {
            return key;
        }

        return "";
    }

    /**
     * Queries Windows Registry directly to find environment variables in User/System scope.
     */
    private static String resolveFromWindowsRegistry(String varName) {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (!os.contains("win")) {
            return null;
        }

        // Check HKCU\Environment (User Environment Variables)
        try {
            Process process = new ProcessBuilder("reg", "query", "HKCU\\Environment", "/v", varName).start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(varName)) {
                        String[] parts = line.trim().split("\\s+", 3);
                        if (parts.length >= 3 && !parts[2].trim().isEmpty()) {
                            return parts[2].trim();
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        // Check HKLM\...\Environment (System Environment Variables)
        try {
            Process process = new ProcessBuilder("reg", "query", "HKLM\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment", "/v", varName).start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(varName)) {
                        String[] parts = line.trim().split("\\s+", 3);
                        if (parts.length >= 3 && !parts[2].trim().isEmpty()) {
                            return parts[2].trim();
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        return null;
    }

    /**
     * Safely reads a key-value pair from a local .env file if it exists.
     */
    private static String resolveFromDotEnv(String variableName) {
        String catalinaBase = System.getProperty("catalina.base");
        String catalinaHome = System.getProperty("catalina.home");

        String[] possiblePaths = new String[] {
            "D:\\projects\\new2\\.env",
            ".env",
            "../.env",
            (catalinaBase != null) ? catalinaBase + File.separator + ".env" : null,
            (catalinaHome != null) ? catalinaHome + File.separator + ".env" : null,
            System.getProperty("user.dir") + File.separator + ".env",
            System.getProperty("user.home") + File.separator + ".env"
        };

        for (String path : possiblePaths) {
            if (path == null) continue;
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.startsWith("#") || !line.contains("=")) {
                            continue;
                        }
                        int eqIdx = line.indexOf('=');
                        String k = line.substring(0, eqIdx).trim();
                        String v = line.substring(eqIdx + 1).trim();
                        if (v.startsWith("\"") && v.endsWith("\"") && v.length() >= 2) {
                            v = v.substring(1, v.length() - 1);
                        } else if (v.startsWith("'") && v.endsWith("'") && v.length() >= 2) {
                            v = v.substring(1, v.length() - 1);
                        }
                        if (k.equalsIgnoreCase(variableName)) {
                            return v;
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    /**
     * Checks if a valid Google Gemini API key is configured.
     */
    public boolean isApiKeyConfigured() {
        return !apiKey.isEmpty() && !apiKey.equalsIgnoreCase("your_gemini_api_key_here");
    }

    /**
     * Returns a safely masked preview of the API key for diagnostic reporting (e.g. AIza...4xyz).
     * Never exposes the full secret to the UI, browser, or logs.
     */
    public String getMaskedApiKey() {
        if (!isApiKeyConfigured()) {
            return "NOT CONFIGURED";
        }
        if (apiKey.length() <= 8) {
            return "AIza****";
        }
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }

    public String getModel() {
        return model;
    }

    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Sends a generation request to the Google Gemini API with system instructions and user prompt.
     * 
     * @param systemInstruction Instructions defining role, tone, and hotel domain knowledge
     * @param userPrompt Guest or operational query text
     * @return AI-generated text response
     * @throws IOException If network or HTTP communication fails
     * @throws InterruptedException If the HTTP request thread is interrupted
     */
    public String callGenerateContent(String systemInstruction, String userPrompt) throws IOException, InterruptedException {
        if (!isApiKeyConfigured()) {
            throw new IllegalStateException("GEMINI_API_KEY environment variable is not configured. Please set GEMINI_API_KEY in your system environment or server configuration.");
        }

        // Build list of models to try in sequence: primary model first, followed by fallbacks
        java.util.List<String> modelsToTry = new java.util.ArrayList<>();
        if (this.model != null && !this.model.trim().isEmpty()) {
            modelsToTry.add(this.model.trim());
        }
        for (String fb : FALLBACK_MODELS) {
            if (!modelsToTry.contains(fb)) {
                modelsToTry.add(fb);
            }
        }

        IOException lastException = null;
        for (String targetModel : modelsToTry) {
            try {
                String reply = executeGenerateWithModel(targetModel, systemInstruction, userPrompt);
                if (reply != null && !reply.trim().isEmpty()) {
                    if (!targetModel.equalsIgnoreCase(this.model)) {
                        System.out.println("[GeminiService] Primary model '" + this.model + "' failed over to '" + targetModel + "' successfully.");
                    }
                    return reply;
                }
            } catch (IOException e) {
                lastException = e;
                System.err.println("[GeminiService] Model '" + targetModel + "' failed: " + e.getMessage() + ". Attempting next candidate in cascade...");
            }
        }

        if (lastException != null) {
            throw lastException;
        }
        throw new IOException("All candidate Gemini models failed to generate content.");
    }

    private String executeGenerateWithModel(String targetModel, String systemInstruction, String userPrompt)
            throws IOException, InterruptedException {
        // 1. Build Gemini JSON payload with Gson
        JsonObject requestJson = new JsonObject();

        // System Instruction block (if provided)
        if (systemInstruction != null && !systemInstruction.trim().isEmpty()) {
            JsonObject sysInstObj = new JsonObject();
            JsonArray sysParts = new JsonArray();
            JsonObject sysPart = new JsonObject();
            sysPart.addProperty("text", systemInstruction.trim());
            sysParts.add(sysPart);
            sysInstObj.add("parts", sysParts);
            requestJson.add("systemInstruction", sysInstObj);
        }

        // Contents block (user message)
        JsonArray contentsArray = new JsonArray();
        JsonObject contentObj = new JsonObject();
        contentObj.addProperty("role", "user");
        JsonArray partsArray = new JsonArray();
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", (userPrompt != null && !userPrompt.trim().isEmpty()) ? userPrompt.trim() : "Hello");
        partsArray.add(textPart);
        contentObj.add("parts", partsArray);
        contentsArray.add(contentObj);
        requestJson.add("contents", contentsArray);

        // Generation Config - optimized for low-latency, concise responses
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.2);
        generationConfig.addProperty("maxOutputTokens", 1024);

        // Only add thinkingConfig for models that support it (2.5 or 3.5 non-lite)
        boolean isLite = targetModel.toLowerCase().contains("lite");
        boolean supportsThinking = (targetModel.contains("2.5") || targetModel.equals("gemini-3.5-flash")) && !isLite;
        if (supportsThinking) {
            JsonObject thinkingConfig = new JsonObject();
            thinkingConfig.addProperty("thinkingBudget", 0);
            generationConfig.add("thinkingConfig", thinkingConfig);
        }

        requestJson.add("generationConfig", generationConfig);
        String jsonBody = requestJson.toString();

        String targetEndpoint = BASE_API_URL + targetModel + ":generateContent";

        // 2. Build HTTP POST Request with official x-goog-api-key header
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetEndpoint))
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // 3. Send HTTP request synchronously
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        int statusCode = response.statusCode();
        String responseBody = response.body();

        // 4. Handle HTTP Response
        if (statusCode == 200) {
            try {
                JsonObject resObj = JsonParser.parseString(responseBody).getAsJsonObject();
                if (resObj.has("candidates")) {
                    JsonArray candidates = resObj.getAsJsonArray("candidates");
                    if (candidates != null && candidates.size() > 0) {
                        JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
                        if (firstCandidate.has("content")) {
                            JsonObject content = firstCandidate.getAsJsonObject("content");
                            if (content.has("parts")) {
                                JsonArray parts = content.getAsJsonArray("parts");
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < parts.size(); i++) {
                                    JsonObject part = parts.get(i).getAsJsonObject();
                                    if (part.has("text")) {
                                        sb.append(part.get("text").getAsString());
                                    }
                                }
                                String resultText = sb.toString().trim();
                                if (!resultText.isEmpty()) {
                                    return resultText;
                                }
                            }
                        }
                        if (firstCandidate.has("finishReason")) {
                            String finishReason = firstCandidate.get("finishReason").getAsString();
                            if (!"STOP".equalsIgnoreCase(finishReason)) {
                                return "Gemini finished with reason: " + finishReason;
                            }
                        }
                    }
                }
                return "No response text generated by Gemini.";
            } catch (Exception e) {
                throw new IOException("Failed to parse Gemini API JSON response for " + targetModel + ": " + e.getMessage(), e);
            }
        } else {
            String errorMsg = "Google Gemini API error (HTTP " + statusCode + ", model " + targetModel + ")";
            try {
                JsonObject errorJson = JsonParser.parseString(responseBody).getAsJsonObject();
                if (errorJson.has("error")) {
                    JsonObject err = errorJson.getAsJsonObject("error");
                    if (err.has("message")) {
                        errorMsg += ": " + err.get("message").getAsString();
                    }
                }
            } catch (Exception ignored) {
                if (statusCode == 400) {
                    errorMsg += ": Invalid request or unsupported parameter.";
                } else if (statusCode == 403) {
                    errorMsg += ": Permission denied or API access restricted.";
                } else if (statusCode == 404) {
                    errorMsg += ": Model endpoint '" + targetModel + "' not found.";
                } else if (statusCode == 429) {
                    errorMsg += ": Quota or rate limit exceeded.";
                } else if (statusCode >= 500) {
                    errorMsg += ": Google Gemini server temporary error.";
                }
            }
            throw new IOException(errorMsg);
        }
    }

    /**
     * Diagnostic ping method to verify live Gemini API communication.
     */
    public String testConnection() {
        if (!isApiKeyConfigured()) {
            return "API Key Missing: Set GEMINI_API_KEY in your system environment.";
        }
        try {
            return callGenerateContent(
                "You are an AI Smart Hotel Assistant diagnostic bot. Respond only with 'Connection Successful'.",
                "Ping test."
            );
        } catch (Exception e) {
            return "Connection Failed: " + e.getMessage();
        }
    }
}
