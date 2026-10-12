package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.example.model.Message
import com.example.model.MessageStatus
import com.example.model.MessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class SupabaseService {
    private val tag = "SupabaseService"
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val scope = CoroutineScope(Dispatchers.IO)
    private var webSocket: WebSocket? = null

    private val baseUrl: String get() = BuildConfig.SUPABASE_URL.trimEnd('/')
    private val anonKey: String get() = BuildConfig.SUPABASE_ANON_KEY

    val isConfigured: Boolean
        get() = baseUrl.isNotBlank() && anonKey.isNotBlank() && baseUrl.startsWith("http")

    fun sendMessage(message: Message, onSuccess: (() -> Unit)? = null, onError: ((String) -> Unit)? = null) {
        if (!isConfigured) {
            onSuccess?.invoke()
            return
        }

        val jsonBody = JSONObject().apply {
            put("id", message.id)
            put("chat_id", message.chatId)
            put("sender_id", message.senderId)
            put("sender_name", message.senderName)
            put("text", message.text)
            put("type", message.type.name)
            put("status", message.status.name)
            put("media_url", message.mediaUrl ?: JSONObject.NULL)
            put("media_caption", message.mediaCaption ?: JSONObject.NULL)
            put("is_view_once", message.isViewOnce)
        }.toString()

        val request = Request.Builder()
            .url("$baseUrl/rest/v1/messages")
            .addHeader("apikey", anonKey)
            .addHeader("Authorization", "Bearer $anonKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "return=minimal")
            .post(jsonBody.toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(tag, "Erro ao enviar mensagem para o Supabase: ${e.message}")
                onError?.invoke(e.message ?: "Falha na rede")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        Log.d(tag, "Mensagem enviada com sucesso para o Supabase: ${message.id}")
                        onSuccess?.invoke()
                    } else {
                        val errorBody = response.body?.string() ?: ""
                        Log.e(tag, "Falha no Supabase HTTP ${response.code}: $errorBody")
                        onError?.invoke("HTTP ${response.code}: $errorBody")
                    }
                }
            }
        })
    }

    fun fetchMessages(chatId: String, onResult: (List<Message>) -> Unit) {
        if (!isConfigured) return

        val request = Request.Builder()
            .url("$baseUrl/rest/v1/messages?chat_id=eq.$chatId&order=created_at.asc")
            .addHeader("apikey", anonKey)
            .addHeader("Authorization", "Bearer $anonKey")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(tag, "Erro ao buscar mensagens do Supabase: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        val body = response.body?.string() ?: "[]"
                        try {
                            val jsonArray = JSONArray(body)
                            val list = mutableListOf<Message>()
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)
                                list.add(parseMessageFromJson(item))
                            }
                            onResult(list)
                        } catch (e: Exception) {
                            Log.e(tag, "Erro ao parsear mensagens: ${e.message}")
                        }
                    }
                }
            }
        })
    }

    fun startRealtimeListener(onNewMessage: (Message) -> Unit) {
        if (!isConfigured) return

        val host = baseUrl.removePrefix("https://").removePrefix("http://")
        val wsUrl = "wss://$host/realtime/v1/websocket?apikey=$anonKey&vsn=1.0.0"

        val request = Request.Builder().url(wsUrl).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(tag, "Supabase Realtime WebSocket conectado!")
                // Join Phoenix Channel for public:messages
                val joinPayload = JSONObject().apply {
                    put("topic", "realtime:public:messages")
                    put("event", "phx_join")
                    put("payload", JSONObject())
                    put("ref", "1")
                }.toString()
                webSocket.send(joinPayload)

                // Start heartbeat loop every 25 seconds
                startHeartbeat(webSocket)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val root = JSONObject(text)
                    val event = root.optString("event")
                    if (event == "INSERT") {
                        val payload = root.optJSONObject("payload")
                        val record = payload?.optJSONObject("record")
                        if (record != null) {
                            val newMsg = parseMessageFromJson(record)
                            onNewMessage(newMsg)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Erro ao processar mensagem do WebSocket: ${e.message}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(tag, "WebSocket falhou: ${t.message}. Tentando reconectar em 5s...")
                scope.launch {
                    delay(5000)
                    startRealtimeListener(onNewMessage)
                }
            }
        })
    }

    private fun startHeartbeat(ws: WebSocket) {
        scope.launch {
            while (true) {
                delay(25000)
                try {
                    val heartbeat = JSONObject().apply {
                        put("topic", "phoenix")
                        put("event", "heartbeat")
                        put("payload", JSONObject())
                        put("ref", "hb")
                    }.toString()
                    ws.send(heartbeat)
                } catch (e: Exception) {
                    break
                }
            }
        }
    }

    private fun parseMessageFromJson(item: JSONObject): Message {
        val typeStr = item.optString("type", "TEXT")
        val msgType = try {
            MessageType.valueOf(typeStr)
        } catch (_: Exception) {
            MessageType.TEXT
        }

        return Message(
            id = item.optString("id"),
            chatId = item.optString("chat_id"),
            senderId = item.optString("sender_id"),
            senderName = item.optString("sender_name", "Usuário"),
            text = item.optString("text", ""),
            timestamp = System.currentTimeMillis(),
            type = msgType,
            status = MessageStatus.READ,
            isFromMe = item.optString("sender_id") == "user_me",
            mediaUrl = item.optString("media_url", null),
            mediaCaption = item.optString("media_caption", null),
            isViewOnce = item.optBoolean("is_view_once", false)
        )
    }
}
