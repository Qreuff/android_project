package com.example.android_app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SocketsActivity : AppCompatActivity() {

    private val logTag = "ZMQ_CLIENT"
    private lateinit var tvSockets: TextView
    private lateinit var tvLat: TextView
    private lateinit var tvLon: TextView
    private lateinit var tvAlt: TextView
    private lateinit var tvTime: TextView
    private lateinit var handler: Handler

    private val serverAddress = "tcp://172.20.10.14"
    private val maxRetries = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_socket)

        tvSockets = findViewById(R.id.tvSockets)
        tvLat = findViewById(R.id.tvLatitude)
        tvLon = findViewById(R.id.tvLongitude)
        tvAlt = findViewById(R.id.tvAltitude)
        tvTime = findViewById(R.id.tvTime)

        handler = Handler(Looper.getMainLooper())

        val lat = intent.getDoubleExtra("latitude", 0.0)
        val lon = intent.getDoubleExtra("longitude", 0.0)
        val alt = intent.getDoubleExtra("altitude", 0.0)
        val time = intent.getStringExtra("time") ?: ""
        val timestamp = intent.getLongExtra("timestamp", 0)

        if (lat != 0.0) {
            tvLat.text = "Latitude: $lat"
            tvLon.text = "Longitude: $lon"
            tvAlt.text = "Altitude: ${String.format("%.2f м", alt)}"
            tvTime.text = "Time: $time"
        }

        findViewById<Button>(R.id.btnSend).setOnClickListener {
            sendLocationData(lat, lon, alt, timestamp)
        }
    }

    private fun sendLocationData(lat: Double, lon: Double, alt: Double, timestamp: Long) {
        Thread {
            var retryCount = 0
            var success = false

            while (retryCount < maxRetries && !success) {
                try {
                    retryCount++

                    Log.d(logTag, "Попытка отправки $retryCount из $maxRetries")

                    val context = ZContext()
                    val socket = context.createSocket(SocketType.REQ)

                    socket.setReceiveTimeOut(5000)
                    socket.setSendTimeOut(5000)
                    socket.setLinger(0)

                    socket.connect(serverAddress)

                    val jsonData = JSONObject().apply {
                        put("latitude", lat)
                        put("longitude", lon)
                        put("altitude", alt)
                        put("time", timestamp)
                        put("device_id", android.os.Build.MODEL)
                        put("provider", "gps")
                    }

                    val request = jsonData.toString()
                    Log.d(logTag, "Отправка: $request")

                    socket.send(request.toByteArray(ZMQ.CHARSET), 0)

                    val replyBytes = socket.recv(0)

                    if (replyBytes != null) {
                        val reply = String(replyBytes, ZMQ.CHARSET)
                        Log.d(logTag, "Получено: $reply")

                        handler.post {
                            tvSockets.text = "Ответ сервера:\n$reply\n(попытка $retryCount)"
                            Toast.makeText(this@SocketsActivity, "Данные отправлены", Toast.LENGTH_SHORT).show()
                        }

                        success = true
                    } else {
                        throw Exception("Нет ответа от сервера")
                    }

                    socket.close()
                    context.close()

                } catch (e: Exception) {
                    Log.e(logTag, "Ошибка отправки (попытка $retryCount): ${e.message}")

                    if (retryCount >= maxRetries) {
                        handler.post {
                            tvSockets.text = "Ошибка: не удалось отправить данные после $maxRetries попыток\n${e.message}"
                            Toast.makeText(this@SocketsActivity, "Ошибка соединения", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Thread.sleep(2000)
                    }
                }
            }
        }.start()
    }
}