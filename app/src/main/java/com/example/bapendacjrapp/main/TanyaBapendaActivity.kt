package com.example.bapendacjrapp.main

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bapendacjrapp.BuildConfig
import com.example.bapendacjrapp.R
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TanyaBapendaActivity : AppCompatActivity() {

    private lateinit var rvChat: RecyclerView
    private lateinit var etChatMessage: EditText
    private lateinit var btnSendMessage: ImageButton
    private lateinit var ivBack: ImageView
    private lateinit var chatAdapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()

    private lateinit var generativeModel: GenerativeModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tanya_bapenda)

        // Inisialisasi GenerativeModel dengan API key dari BuildConfig
        val apiKey = BuildConfig.API_KEY
        generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey
        )

        rvChat = findViewById(R.id.rvChat)
        etChatMessage = findViewById(R.id.etChatMessage)
        btnSendMessage = findViewById(R.id.btnSendMessage)
        ivBack = findViewById(R.id.ivBack)

        chatAdapter = ChatAdapter(chatMessages)
        rvChat.layoutManager = LinearLayoutManager(this)
        rvChat.adapter = chatAdapter

        // Tambahkan pesan sambutan dari AI
        chatMessages.add(ChatMessage("Halo! Saya asisten AI Bapenda Cianjur. Ada yang bisa saya bantu?", false))
        chatAdapter.notifyItemInserted(0)

        btnSendMessage.setOnClickListener {
            val userMessage = etChatMessage.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                sendMessageToAI(userMessage)
                etChatMessage.text.clear()
            }
        }

        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun sendMessageToAI(message: String) {
        // Tampilkan pesan pengguna
        chatMessages.add(ChatMessage(message, true))
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        rvChat.scrollToPosition(chatMessages.size - 1)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = generativeModel.generateContent(message)
                val aiResponse = response.text ?: "Maaf, saya tidak dapat memproses permintaan ini."
                withContext(Dispatchers.Main) {
                    chatMessages.add(ChatMessage(aiResponse, false))
                    chatAdapter.notifyItemInserted(chatMessages.size - 1)
                    rvChat.scrollToPosition(chatMessages.size - 1)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    chatMessages.add(ChatMessage("Terjadi kesalahan: ${e.message}", false))
                    chatAdapter.notifyItemInserted(chatMessages.size - 1)
                    rvChat.scrollToPosition(chatMessages.size - 1)
                }
            }
        }
    }

    data class ChatMessage(val text: String, val isUser: Boolean)

    class ChatAdapter(private val messages: List<ChatMessage>) :
        RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_message, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val message = messages[position]
            holder.tvChatMessage.text = message.text

            val layoutParams = holder.tvChatMessage.layoutParams as LinearLayout.LayoutParams
            if (message.isUser) {
                layoutParams.gravity = Gravity.END
                holder.tvChatMessage.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.rounded_chat_bubble_user)
                holder.tvChatMessage.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
            } else {
                layoutParams.gravity = Gravity.START
                holder.tvChatMessage.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.rounded_chat_bubble_ai)
                holder.tvChatMessage.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.black))
            }
            holder.tvChatMessage.layoutParams = layoutParams
        }

        override fun getItemCount() = messages.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvChatMessage: TextView = view.findViewById(R.id.tvChatMessage)
        }
    }
}