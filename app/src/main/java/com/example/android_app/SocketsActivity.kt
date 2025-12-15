package com.example.android_app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.android_app.R
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ

class SocketsActivity : AppCompatActivity() {

    private val logTag = "ZMQ_CLIENT"
    private lateinit var tvSockets: TextView
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_socket)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvSockets = findViewById(R.id.tvSockets)
        handler = Handler(Looper.getMainLooper())

        findViewById<Button>(R.id.btnSend).setOnClickListener {
            Thread { startClient() }.start()
        }
    }

    private fun startClient() {
        val context = ZContext()
        val socket = context.createSocket(SocketType.REQ)

        socket.connect("tcp://172.20.10.14:8080")

        val request = "Hello from Android!"
        Log.d(logTag, "Отправлено: $request")

        socket.send(request.toByteArray(ZMQ.CHARSET), 0)

        val replyBytes = socket.recv(0)
        val reply = String(replyBytes, ZMQ.CHARSET)

        Log.d(logTag, "Получено: $reply")

        handler.post {
            tvSockets.text = "Ответ сервера:\n$reply"
        }

        socket.close()
        context.close()
    }
}