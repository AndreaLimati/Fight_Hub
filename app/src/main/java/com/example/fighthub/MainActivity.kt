package com.example.fighthub

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Gestione dei padding per i bordi dello schermo (Edge-to-Edge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Tasto indietro per andare nel login, non nella registrazione.
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Sostituisci "AltraActivity::class.java" con il nome della tua Activity di destinazione
                val intent = Intent(this@MainActivity, LoginActivity::class.java)

                // Opzionale: pulisce lo stack per evitare di accumulare pagine
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

                startActivity(intent)
                finish() // Chiude MainActivity
            }
        }
        // Aggiunge il callback al dispatcher
        onBackPressedDispatcher.addCallback(this, callback)
    }
}