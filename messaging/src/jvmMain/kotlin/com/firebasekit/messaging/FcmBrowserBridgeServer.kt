package com.firebasekit.messaging

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.BindException
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

private const val DEFAULT_BRIDGE_PORT = 45777
private const val FIREBASE_JS_SDK_VERSION = "10.13.2"
private const val LOOPBACK_HOST = "127.0.0.1"

internal class FcmBrowserBridgeServer(
    private val session: String,
    private val configProvider: () -> DesktopMessagingConfig,
    private val onToken: (String) -> Unit,
    private val onDelete: () -> Unit,
    private val onMessage: (String) -> Unit,
    private val onError: (String) -> Unit,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val lock = Any()

    @Volatile
    private var server: HttpServer? = null

    @Volatile
    private var baseUrl: String? = null

    fun start(): String = synchronized(lock) {
        baseUrl?.let { return@synchronized it }

        val httpServer = createHttpServer()
        httpServer.createContext("/") { exchange -> handle(exchange) }
        httpServer.executor = Executors.newSingleThreadExecutor { task ->
            Thread(task, "FirebaseKitMessagingBridge").apply { isDaemon = true }
        }
        httpServer.start()

        server = httpServer
        "http://$LOOPBACK_HOST:${httpServer.address.port}".also { baseUrl = it }
    }

    private fun createHttpServer(): HttpServer = try {
        HttpServer.create(InetSocketAddress(LOOPBACK_HOST, DEFAULT_BRIDGE_PORT), 0)
    } catch (_: BindException) {
        HttpServer.create(InetSocketAddress(LOOPBACK_HOST, 0), 0)
    }

    private fun handle(exchange: HttpExchange) {
        runCatching {
            when (exchange.requestURI.path) {
                "/" -> exchange.respond(
                    status = 200,
                    body = registrationPage(),
                    contentType = "text/html; charset=utf-8",
                )
                "/firebase-messaging-sw.js" -> exchange.respond(
                    status = 200,
                    body = serviceWorkerScript(),
                    contentType = "application/javascript; charset=utf-8",
                )
                "/token" -> handleToken(exchange)
                "/delete" -> handleDelete(exchange)
                "/message" -> handleMessage(exchange)
                "/error" -> handleError(exchange)
                else -> exchange.respond(status = 404, body = "Not found")
            }
        }.onFailure { error ->
            exchange.respond(status = 500, body = error.message ?: "Firebase Messaging bridge failed")
        }
    }

    private fun handleToken(exchange: HttpExchange) {
        exchange.requireSession()
        exchange.requirePost()

        val body = exchange.readRequestBody()
        val token = json.parseToJsonElement(body)
            .jsonObject["token"]
            ?.jsonPrimitive
            ?.contentOrNull
            ?.takeIf { it.isNotBlank() }
            ?: error("FCM token was not returned by the browser")

        onToken(token)
        exchange.respond(status = 200, body = "OK")
    }

    private fun handleDelete(exchange: HttpExchange) {
        exchange.requireSession()
        exchange.requirePost()

        onDelete()
        exchange.respond(status = 200, body = "OK")
    }

    private fun handleMessage(exchange: HttpExchange) {
        exchange.requireSession()
        exchange.requirePost()

        onMessage(exchange.readRequestBody())
        exchange.respond(status = 200, body = "OK")
    }

    private fun handleError(exchange: HttpExchange) {
        exchange.requireSession()
        exchange.requirePost()

        val body = exchange.readRequestBody()
        val message = runCatching {
            json.parseToJsonElement(body).jsonObject["message"]?.jsonPrimitive?.contentOrNull
        }.getOrNull() ?: "Firebase Messaging browser registration failed"

        onError(message)
        exchange.respond(status = 200, body = "OK")
    }

    private fun registrationPage(): String {
        val config = configProvider()
        val firebaseConfig = config.toFirebaseConfigJson()
        val vapidKey = Json.encodeToString(JsonPrimitive.serializer(), JsonPrimitive(config.webVapidKey))

        return """
            <!doctype html>
            <html lang="en">
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>FirebaseKit Messaging</title>
                <style>
                    :root { color-scheme: light dark; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif; }
                    body { margin: 0; min-height: 100vh; display: grid; place-items: center; background: Canvas; color: CanvasText; }
                    main { width: min(560px, calc(100vw - 32px)); }
                    h1 { font-size: 22px; margin: 0 0 12px; }
                    p { line-height: 1.5; margin: 0 0 12px; }
                    code { overflow-wrap: anywhere; }
                    .status { padding: 12px 0; font-weight: 600; }
                </style>
            </head>
            <body>
                <main>
                    <h1>FirebaseKit Messaging</h1>
                    <p class="status" id="status">Preparing browser notification registration...</p>
                    <p id="details">This tab will close after Firebase Cloud Messaging registration completes.</p>
                    <code id="token"></code>
                </main>
                <script type="module">
                    import { initializeApp } from "https://www.gstatic.com/firebasejs/$FIREBASE_JS_SDK_VERSION/firebase-app.js";
                    import {
                        deleteToken,
                        getMessaging,
                        getToken,
                        onMessage
                    } from "https://www.gstatic.com/firebasejs/$FIREBASE_JS_SDK_VERSION/firebase-messaging.js";

                    const firebaseConfig = $firebaseConfig;
                    const vapidKey = $vapidKey;
                    const params = new URLSearchParams(window.location.search);
                    const session = params.get("session");
                    const action = params.get("action") || "token";
                    const status = document.getElementById("status");
                    const details = document.getElementById("details");
                    const tokenNode = document.getElementById("token");

                    async function post(path, body) {
                        await fetch(`${'$'}{path}?session=${'$'}{encodeURIComponent(session)}`, {
                            method: "POST",
                            headers: { "Content-Type": "application/json" },
                            body: JSON.stringify(body)
                        });
                    }

                    function closeRegistrationTab() {
                        status.textContent = "Firebase Cloud Messaging registration is complete.";
                        details.textContent = "You may close this tab if it remains open.";
                        setTimeout(() => window.close(), 350);
                    }

                    function showBrowserNotification(payload) {
                        if (Notification.permission !== "granted") return;

                        const notification = payload.notification || {};
                        const title = notification.title || "Firebase notification";
                        const options = {
                            body: notification.body || "",
                            icon: notification.icon,
                            data: payload.data || {}
                        };
                        new Notification(title, options);
                    }

                    try {
                        if (!session) throw new Error("Missing FirebaseKit browser bridge session.");
                        if (!("serviceWorker" in navigator)) throw new Error("This browser does not support service workers.");
                        if (!("Notification" in window)) throw new Error("This browser does not support notifications.");

                        const app = initializeApp(firebaseConfig);
                        const messaging = getMessaging(app);
                        const registration = await navigator.serviceWorker.register("/firebase-messaging-sw.js");
                        await registration.update();
                        const permission = await Notification.requestPermission();

                        if (permission !== "granted") {
                            throw new Error("Notification permission was not granted.");
                        }

                        if (action === "delete") {
                            status.textContent = "Deleting Firebase Cloud Messaging token...";
                            const deleted = await deleteToken(messaging);
                            await post("/delete", { deleted });
                            status.textContent = "Firebase Cloud Messaging token deleted.";
                            closeRegistrationTab();
                        } else {
                            status.textContent = "Registering Firebase Cloud Messaging token...";
                            const token = await getToken(messaging, { vapidKey, serviceWorkerRegistration: registration });
                            tokenNode.textContent = token;
                            await post("/token", { token });
                            status.textContent = "Desktop notifications are ready.";
                            closeRegistrationTab();

                            onMessage(messaging, async (payload) => {
                                showBrowserNotification(payload);
                                await post("/message", payload);
                            });
                        }
                    } catch (error) {
                        status.textContent = error.message || "Firebase Messaging registration failed.";
                        await post("/error", { message: status.textContent });
                    }
                </script>
            </body>
            </html>
        """.trimIndent()
    }

    private fun serviceWorkerScript(): String {
        val firebaseConfig = configProvider().toFirebaseConfigJson()
        val encodedSession = Json.encodeToString(JsonPrimitive.serializer(), JsonPrimitive(session))

        return """
            importScripts("https://www.gstatic.com/firebasejs/$FIREBASE_JS_SDK_VERSION/firebase-app-compat.js");
            importScripts("https://www.gstatic.com/firebasejs/$FIREBASE_JS_SDK_VERSION/firebase-messaging-compat.js");

            firebase.initializeApp($firebaseConfig);

            const messaging = firebase.messaging();
            const session = $encodedSession;

            async function postMessageToDesktop(payload) {
                try {
                    await fetch(`/message?session=${'$'}{encodeURIComponent(session)}`, {
                        method: "POST",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify(payload)
                    });
                } catch (error) {
                    console.warn("[FirebaseKit] Failed to forward background message to desktop app.", error);
                }
            }

            messaging.onBackgroundMessage((payload) => {
                return postMessageToDesktop(payload);
            });

            self.addEventListener("notificationclick", (event) => {
                event.notification.close();
                event.waitUntil(clients.openWindow("/"));
            });
        """.trimIndent()
    }

    private fun DesktopMessagingConfig.toFirebaseConfigJson(): String {
        val config = buildJsonObject {
            put("apiKey", apiKey)
            put("authDomain", authDomain)
            put("projectId", projectId)
            storageBucket?.takeIf { it.isNotBlank() }?.let { put("storageBucket", it) }
            put("messagingSenderId", messagingSenderId)
            put("appId", appId)
            measurementId?.takeIf { it.isNotBlank() }?.let { put("measurementId", it) }
        }

        return Json.encodeToString(JsonObject.serializer(), config)
    }

    private fun HttpExchange.requireSession() {
        require(queryParameters()["session"] == session) { "Unauthorized Firebase Messaging bridge request" }
    }

    private fun HttpExchange.requirePost() {
        require(requestMethod.equals("POST", ignoreCase = true)) { "Expected POST request" }
    }

    private fun HttpExchange.queryParameters(): Map<String, String> {
        val query = requestURI.rawQuery ?: return emptyMap()

        return query.split("&")
            .filter { it.isNotBlank() }
            .associate { part ->
                val keyValue = part.split("=", limit = 2)
                val key = keyValue[0].urlDecode()
                val value = keyValue.getOrElse(1) { "" }.urlDecode()
                key to value
            }
    }

    private fun String.urlDecode(): String = URLDecoder.decode(this, StandardCharsets.UTF_8)

    private fun HttpExchange.readRequestBody(): String =
        requestBody.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }

    private fun HttpExchange.respond(
        status: Int,
        body: String,
        contentType: String = "text/plain; charset=utf-8",
    ) {
        val bytes = body.toByteArray(StandardCharsets.UTF_8)
        responseHeaders.set("Content-Type", contentType)
        sendResponseHeaders(status, bytes.size.toLong())
        responseBody.use { it.write(bytes) }
    }
}
