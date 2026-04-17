package com.example.fighthub


import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fighthub.controlloreDB.ControlloreDB
import com.example.fighthub.viewModel.RegistrazioneViewModel

class RegistrationActivity : AppCompatActivity() {
    private val registrazioneViewModel : RegistrazioneViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registration)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val videoView = findViewById<VideoView>(R.id.videoView)
        val path = "android.resource://" + packageName + "/" + R.raw.mgs3_video
        val uri = Uri.parse(path)

        val email = intent.getStringExtra("email")!!
        val passw = intent.getStringExtra("passw")!!

        registrazioneViewModel.updateEmailPassw(email, passw)


        //video in fondo
        videoView.setVideoURI(uri)
        videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
        }
        videoView.start()

        //fragment container
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fragment_registrazione_container, RegistrazioneFragment1())
                .commit()
        }

    }

    fun navigaAlSecondoStep() {
        supportFragmentManager.beginTransaction()
            // Animazione: entra da destra, esce a sinistra
            .setCustomAnimations(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
            .replace(R.id.fragment_registrazione_container, RegistrazioneFragment2()) // Carica il secondo fragment
            .addToBackStack(null) // Permette di tornare indietro col tasto back
            .commit()
    }

    fun navigaAlTerzoStep() {
        supportFragmentManager.beginTransaction()
            // Animazione: entra da destra esce a sinistra
            .setCustomAnimations(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
            .replace(R.id.fragment_registrazione_container, RegistrazioneFragment3()) // Carica il terzo fragment
            .addToBackStack(null) // Permette di tornare indietro col tasto back
            .commit()
    }

    fun navigaAlQuartoStep() {
        supportFragmentManager.beginTransaction()
            // Animazione: entra da destra esce a sinistra
            .setCustomAnimations(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
            .replace(R.id.fragment_registrazione_container, RegistrazioneFragment4()) // Carica il quarto fragment
            .addToBackStack(null) // Permette di tornare indietro col tasto back
            .commit()
    }
}

