package com.example.fighthub

import MainFragmentProfiloUtente
import MainFragmentMenu
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.fighthub.controllori.ControlloreDB
import com.example.fighthub.viewModel.UtenteViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView


class MyPagerAdapter(activity: MainActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3 // Numero dei tuoi fragment

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MainFragmentChat()
            1 -> MainFragmentMenu()
            2 -> MainFragmentProfiloUtente()
            else -> MainFragmentMenu()
        }
    }
}
class MainActivity : AppCompatActivity() {
    private val utenteViewModel : UtenteViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() //per la navigation bar
        setContentView(R.layout.activity_main)

        val uid = intent.getStringExtra("uid")

        ControlloreDB.getDatiUtente(uid){ user ->
            if(user!=null){
                utenteViewModel.updateTutto(user)
                Log.d("Utente main", "${user.uid}")
            }else{
                Toast.makeText(this, "Macello con utente", Toast.LENGTH_SHORT).show()
            }
        }

        //per scorrimento tra fragment
        val viewPager = findViewById<ViewPager2>(R.id.fragment_main_container)
        val navBar = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        //per status bar sopra bianca.
        val window = window
        val controller = WindowCompat.getInsetsController(window, window.decorView)

        // FALSE = Icone bianche (per sfondi scuri)
        // TRUE = Icone scure (per sfondi chiari)
        controller.isAppearanceLightStatusBars = false


        window.navigationBarColor = android.graphics.Color.BLACK
        //fine  status bar
        // per navigation bar
    // Imposta l'adapter per scorrimento tra fragment
        viewPager.adapter = MyPagerAdapter(this)

    // 1. Sincronizza Swipe -> BottomNav
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                navBar.menu.getItem(position).isChecked = true
            }
        })

    // 2. Sincronizza BottomNav -> Swipe
        navBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> viewPager.currentItem = 0
                R.id.nav_fight -> viewPager.currentItem = 1
                R.id.nav_profilo -> viewPager.currentItem = 2
            }
            true
        }
        //per far in modo che ad apertura applicazione siamo su fight:
        // Seleziona graficamente l'icona "nav_fight" sulla Bottom Bar
        navBar.selectedItemId = R.id.nav_fight
        // Sposta il ViewPager sulla pagina 1 senza mostrare l'animazione di transizione iniziale (smoothScroll = false)
        viewPager.setCurrentItem(1, false)
    //fine adapter scorrimento

        // Gestione dei padding per i bordi dello schermo (Edge-to-Edge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top+20, systemBars.right, 0)
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