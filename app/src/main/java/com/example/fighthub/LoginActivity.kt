package com.example.fighthub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.TraceCompat.isEnabled
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.OnBackPressedCallback

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //video
        val videoView = findViewById<VideoView>(R.id.videoView)
        val path = "android.resource://" + packageName + "/" + R.raw.mgs3_video
        val uri = Uri.parse(path)

        //layout
        val layoutBottoni = findViewById<LinearLayout>(R.id.layout_bottoni)
        val layoutLogin = findViewById<LinearLayout>(R.id.layout_form_login)
        val layoutRegistrazione = findViewById<LinearLayout>(R.id.layout_form_registrazione)

        //bottoni
        val btnLogin = findViewById<Button>(R.id.button_login)
        val btnRegistrazione = findViewById<Button>(R.id.button_registrazione)
        val button = findViewById<Button>(R.id.btn_conferma_registrazione)

        //video in fondo
        videoView.setVideoURI(uri)
        videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
        }
        videoView.start()

        //funzione per settare tasto indietro per form
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if (layoutLogin.visibility == View.VISIBLE) {
                    layoutLogin.visibility = View.GONE
                    layoutBottoni.visibility = View.VISIBLE
                }

                else if (layoutRegistrazione.visibility == View.VISIBLE) {
                    layoutRegistrazione.visibility = View.GONE
                    layoutBottoni.visibility = View.VISIBLE
                }

                else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        //Listener per bottone da login a form login
        btnLogin.setOnClickListener {
            layoutBottoni.visibility = View.GONE
            layoutLogin.visibility = View.VISIBLE
            layoutRegistrazione.visibility = View.GONE
        }

        //Lister per bottone da registrazione a form registrazione
        btnRegistrazione.setOnClickListener {
            layoutBottoni.visibility = View.GONE
            layoutRegistrazione.visibility = View.VISIBLE
            layoutLogin.visibility = View.GONE
        }

        //btn_conferma_registrazione
        button.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            intent.putExtra("email", findViewById<EditText>(R.id.register_email).text.toString())
            intent.putExtra("passw", findViewById<EditText>(R.id.register_password).text.toString())
            startActivity(intent)
        }
    }
}