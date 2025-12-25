/*
 * Copyright (c) 2024, NullBot
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.osrschatlogger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles sending chat messages to the OSRS Chatlogger server.
 * Features: batching, buffering, retry logic, connection status tracking.
 */
@Slf4j
@Singleton
public class ChatSender
{
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final int MAX_BUFFER_SIZE = 1000;
    private static final String USER_AGENT = "OSRS-Chatlogger-RuneLite/1.0";

    private final OkHttpClient httpClient;
    private final Gson gson;
    private final OsrsChatloggerConfig config;

    // Message buffer for batching
    private final List<ChatData> messageBuffer = new ArrayList<>();
    private final Object bufferLock = new Object();

    // Scheduled executor for batch sending
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> batchTask;

    // Connection state
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final AtomicBoolean validated = new AtomicBoolean(false);
    private final AtomicInteger sentCount = new AtomicInteger(0);
    private final AtomicInteger pendingCount = new AtomicInteger(0);

    private volatile String username = null;

    @Inject
    public ChatSender(OsrsChatloggerConfig config)
    {
        this.config = config;
        this.gson = new Gson();

        // Configure HTTP client with timeouts
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

        startScheduler();
    }

    private void startScheduler()
    {
        if (scheduler != null && !scheduler.isShutdown())
        {
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "osrs-chatlogger-sender");
            t.setDaemon(true);
            return t;
        });

        // Schedule periodic batch sending
        int delayMs = Math.max(500, Math.min(10000, config.batchDelayMs()));
        batchTask = scheduler.scheduleAtFixedRate(
            this::sendBatch,
            delayMs,
            delayMs,
            TimeUnit.MILLISECONDS
        );
    }

    /**
     * Validate the API key with the server.
     */
    public void validateApiKey()
    {
        String apiKey = config.apiKey();
        if (apiKey == null || apiKey.isEmpty())
        {
            log.warn("No API key configured");
            validated.set(false);
            connected.set(false);
            return;
        }

        if (!apiKey.startsWith("oscl_"))
        {
            log.warn("Invalid API key format - should start with 'oscl_'");
            validated.set(false);
            connected.set(false);
            return;
        }

        String url = config.serverUrl() + "/api/v1/auth/validate";

        Request request = new Request.Builder()
            .url(url)
            .post(RequestBody.create("{}", JSON_MEDIA_TYPE))
            .addHeader("Authorization", "Bearer " + apiKey)
            .addHeader("User-Agent", USER_AGENT)
            .build();

        httpClient.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                log.error("Failed to validate API key: {}", e.getMessage());
                connected.set(false);
                validated.set(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                try (ResponseBody body = response.body())
                {
                    if (response.isSuccessful() && body != null)
                    {
                        String responseBody = body.string();
                        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                        
                        if (json.has("valid") && json.get("valid").getAsBoolean())
                        {
                            username = json.has("username") ? json.get("username").getAsString() : null;
                            validated.set(true);
                            connected.set(true);
                            log.info("API key validated for user: {}", username);
                        }
                        else
                        {
                            validated.set(false);
                            connected.set(false);
                            log.warn("API key validation failed - key may be invalid or revoked");
                        }
                    }
                    else
                    {
                        validated.set(false);
                        connected.set(false);
                        log.warn("API key validation failed: HTTP {}", response.code());
                    }
                }
            }
        });
    }

    /**
     * Queue a message to be sent.
     */
    public void queueMessage(ChatData chatData)
    {
        if (!validated.get())
        {
            if (config.debugMode())
            {
                log.debug("Not queueing message - API key not validated");
            }
            return;
        }

        synchronized (bufferLock)
        {
            // Don't buffer too many messages
            if (messageBuffer.size() >= MAX_BUFFER_SIZE)
            {
                log.warn("Message buffer full, dropping oldest message");
                messageBuffer.remove(0);
            }

            messageBuffer.add(chatData);
            pendingCount.set(messageBuffer.size());

            if (config.debugMode())
            {
                log.debug("Queued message: {} (buffer size: {})", chatData, messageBuffer.size());
            }

            // Send immediately if batch is full
            int batchSize = Math.max(1, Math.min(50, config.batchSize()));
            if (messageBuffer.size() >= batchSize)
            {
                scheduler.submit(this::sendBatch);
            }
        }
    }

    /**
     * Send current batch of messages.
     */
    private void sendBatch()
    {
        List<ChatData> toSend;

        synchronized (bufferLock)
        {
            if (messageBuffer.isEmpty())
            {
                return;
            }

            // Copy and clear buffer
            toSend = new ArrayList<>(messageBuffer);
            messageBuffer.clear();
            pendingCount.set(0);
        }

        sendMessages(toSend, config.retryAttempts());
    }

    /**
     * Send a list of messages to the server.
     */
    private void sendMessages(List<ChatData> messages, int retriesLeft)
    {
        if (messages.isEmpty() || !validated.get())
        {
            return;
        }

        String url = config.serverUrl() + "/api/v1/chat/batch";

        // Build batch request
        JsonObject requestBody = new JsonObject();
        requestBody.add("messages", gson.toJsonTree(messages));

        // Include RSN and world from first message
        ChatData first = messages.get(0);
        if (first.getRsn() != null)
        {
            requestBody.addProperty("rsn", first.getRsn());
        }
        if (first.getWorld() > 0)
        {
            requestBody.addProperty("world", first.getWorld());
        }

        if (config.debugMode())
        {
            log.debug("Sending batch of {} messages", messages.size());
        }

        Request request = new Request.Builder()
            .url(url)
            .post(RequestBody.create(requestBody.toString(), JSON_MEDIA_TYPE))
            .addHeader("Authorization", "Bearer " + config.apiKey())
            .addHeader("User-Agent", USER_AGENT)
            .build();

        httpClient.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                log.error("Failed to send messages: {}", e.getMessage());
                connected.set(false);
                handleSendFailure(messages, retriesLeft, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                try (ResponseBody body = response.body())
                {
                    if (response.isSuccessful())
                    {
                        connected.set(true);
                        sentCount.addAndGet(messages.size());

                        if (config.debugMode())
                        {
                            log.debug("Successfully sent {} messages (total: {})", 
                                messages.size(), sentCount.get());
                        }
                    }
                    else if (response.code() == 429)
                    {
                        // Rate limited
                        String retryAfter = response.header("Retry-After", "60");
                        int waitSeconds = Integer.parseInt(retryAfter);
                        log.warn("Rate limited, waiting {} seconds", waitSeconds);

                        // Re-queue messages for later
                        scheduler.schedule(() -> {
                            synchronized (bufferLock)
                            {
                                messageBuffer.addAll(0, messages);
                                pendingCount.set(messageBuffer.size());
                            }
                        }, waitSeconds, TimeUnit.SECONDS);
                    }
                    else if (response.code() == 401)
                    {
                        // Invalid API key
                        validated.set(false);
                        connected.set(false);
                        log.error("API key rejected by server - please check your key");
                    }
                    else
                    {
                        log.warn("Server returned HTTP {}", response.code());
                        connected.set(false);
                        handleSendFailure(messages, retriesLeft, "HTTP " + response.code());
                    }
                }
            }
        });
    }

    private void handleSendFailure(List<ChatData> messages, int retriesLeft, String reason)
    {
        if (retriesLeft > 0)
        {
            // Exponential backoff
            int attempt = config.retryAttempts() - retriesLeft + 1;
            int delayMs = attempt * 2000;

            if (config.debugMode())
            {
                log.debug("Retry {} in {}ms: {}", attempt, delayMs, reason);
            }

            scheduler.schedule(() -> sendMessages(messages, retriesLeft - 1),
                delayMs, TimeUnit.MILLISECONDS);
        }
        else
        {
            // Re-buffer messages for later attempt
            log.warn("Failed to send {} messages after all retries, re-buffering", messages.size());
            synchronized (bufferLock)
            {
                // Add to front of buffer (oldest first)
                for (int i = messages.size() - 1; i >= 0; i--)
                {
                    if (messageBuffer.size() < MAX_BUFFER_SIZE)
                    {
                        messageBuffer.add(0, messages.get(i));
                    }
                }
                pendingCount.set(messageBuffer.size());
            }
        }
    }

    /**
     * Flush all buffered messages immediately.
     */
    public void flush()
    {
        sendBatch();
    }

    /**
     * Shutdown the sender gracefully.
     */
    public void shutdown()
    {
        log.info("Shutting down ChatSender...");
        
        // Send any remaining messages
        flush();

        if (batchTask != null)
        {
            batchTask.cancel(false);
        }

        if (scheduler != null)
        {
            scheduler.shutdown();
            try
            {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS))
                {
                    scheduler.shutdownNow();
                }
            }
            catch (InterruptedException e)
            {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    // Status getters
    public boolean isConnected()
    {
        return connected.get() && validated.get();
    }

    public boolean isValidated()
    {
        return validated.get();
    }

    public int getSentCount()
    {
        return sentCount.get();
    }

    public int getPendingCount()
    {
        return pendingCount.get();
    }

    public String getUsername()
    {
        return username;
    }
}
