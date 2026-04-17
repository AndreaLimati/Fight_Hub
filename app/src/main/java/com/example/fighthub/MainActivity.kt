package com.example.fighthub

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

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

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fragment_main_container, MainFragmentMenu())
                .commit()
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

        //navbar
        val navBar = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        navBar.selectedItemId = R.id.nav_fight
        navBar.setOnItemSelectedListener { item ->
           when(item.itemId) {
                R.id.nav_chat -> {
                    navigaAllaChat()
                    true
                }
                R.id.nav_fight -> {
                    navigaAlMenu()
                    true
                }
                R.id.nav_profilo -> {
                    navigaAlProfilo()
                    true
                }
                else -> false
            }

        }

        // Aggiunge il callback al dispatcher
        onBackPressedDispatcher.addCallback(this, callback)
    }

    fun navigaAllaChat() {
        supportFragmentManager.beginTransaction()
            // Animazione: entra da destra, esce a sinistra
            .setCustomAnimations(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
            .replace(R.id.fragment_main_container, MainFragmentChat()) // Carica il secondo fragment
            .addToBackStack(null) // Permette di tornare indietro col tasto back
            .commit()
    }

    fun navigaAlProfilo() {
        supportFragmentManager.beginTransaction()
            // Animazione: entra da destra, esce a sinistra
            .setCustomAnimations(
                android.R.anim.slide_out_right,
                android.R.anim.slide_in_left
            )
            .replace(R.id.fragment_main_container, MainFragmentProfiloUtente()) // Carica il secondo fragment
            .addToBackStack(null) // Permette di tornare indietro col tasto back
            .commit()
    }

    fun navigaAlMenu() {
        supportFragmentManager.beginTransaction()
            // Animazione: entra da destra, esce a sinistra
            .setCustomAnimations(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
            .replace(R.id.fragment_main_container, MainFragmentMenu()) // Carica il secondo fragment
            .addToBackStack(null) // Permette di tornare indietro col tasto back
            .commit()
    }
}