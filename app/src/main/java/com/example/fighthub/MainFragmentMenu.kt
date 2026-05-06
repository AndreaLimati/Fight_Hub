// MANTIENI QUI IL TUO PACKAGE ORIGINALE (es. package com.example.fighthub)

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.fighthub.R
import com.example.fighthub.controllori.ControlloreDB
import com.example.fighthub.model.User
import com.google.android.material.card.MaterialCardView
import kotlin.math.abs

class MainFragmentMenu : Fragment() {

    private lateinit var profileImage: ImageView
    private lateinit var indicatorContainer: LinearLayout
    private lateinit var motionLayout: MotionLayout
    private lateinit var profileCard: MaterialCardView

    private var listaFoto = emptyList<String>()
    private var indiceAttuale = 0
    private var utenteMatch = User()

    // Variabili per distinguere Click da Swipe
    private var startX = 0f
    private var startY = 0f
    private val CLICK_THRESHOLD = 15f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileImage = view.findViewById(R.id.profileImage)
        indicatorContainer = view.findViewById(R.id.indicatorContainer)
        motionLayout = view.findViewById(R.id.motionLayout)
        profileCard = view.findViewById(R.id.profileCard)
        val imageButton = view.findViewById<ImageButton>(R.id.imageButton)

        caricaDatiSupaBase()

        // --- GESTIONE TOUCH (CLICK + SWIPE) ---
        profileCard.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    v.parent.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_UP -> {
                    val endX = event.x
                    val endY = event.y
                    val distanceX = abs(endX - startX)
                    val distanceY = abs(endY - startY)

                    // Se il tocco è un click (poco movimento)
                    if (distanceX < CLICK_THRESHOLD && distanceY < CLICK_THRESHOLD) {
                        if (motionLayout.currentState == R.id.start && listaFoto.isNotEmpty()) {
                            indiceAttuale = (indiceAttuale + 1) % listaFoto.size
                            aggiornaInterfaccia()
                            return@setOnTouchListener true
                        }
                    }
                    v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }

        imageButton.setOnClickListener {
            if (motionLayout.currentState == R.id.end) motionLayout.transitionToStart()
            else motionLayout.transitionToEnd()
        }

        val backCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                motionLayout.transitionToStart()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        // --- LISTENER MOTIONLAYOUT ---
        motionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(p0: MotionLayout?, startId: Int, endId: Int) {}
            override fun onTransitionChange(p0: MotionLayout?, startId: Int, endId: Int, progress: Float) {}

            override fun onTransitionCompleted(p0: MotionLayout?, currentId: Int) {
                backCallback.isEnabled = (currentId == R.id.end)

                if (currentId == R.id.like || currentId == R.id.pass) {
                    // Reset istantaneo per la nuova card
                    motionLayout.setTransitionListener(null)

                    // Riporta a START e carica nuovo utente
                    motionLayout.setTransition(R.id.start, R.id.end)
                    motionLayout.progress = 0f

                    caricaNuovoUtente()

                    motionLayout.post {
                        motionLayout.setTransitionListener(this)
                    }
                }
            }
            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {}
        })
    }

    private fun caricaDatiSupaBase() {
        ControlloreDB.getUidUtenteMatch { uidAvversario ->
            if (!uidAvversario.isNullOrEmpty() && uidAvversario != "vuoto") {
                ControlloreDB.getDatiUtente(uidAvversario) { dati ->
                    if (dati != null) {
                        utenteMatch = dati.copy()
                        listaFoto = if (utenteMatch.urlFoto.isNotEmpty()) utenteMatch.urlFoto
                        else listOf("https://guebusnndyspxxmlmltl.supabase.co/storage/v1/object/public/foto_fighthub/img_945a3fa2-aaf6-4544-8447-f666808806f0.jpg")

                        indiceAttuale = 0
                        setupIndicators()
                        aggiornaInterfaccia()
                    }
                }
            }
        }
    }

    private fun setupIndicators() {
        indicatorContainer.removeAllViews()
        listaFoto.forEachIndexed { index, _ ->
            val viewS = View(context)
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            params.setMargins(8, 0, 8, 0)
            viewS.layoutParams = params
            viewS.setBackgroundColor(if (index == 0) Color.WHITE else Color.parseColor("#80FFFFFF"))
            indicatorContainer.addView(viewS)
        }
    }

    private fun aggiornaInterfaccia() {
        if (listaFoto.isEmpty()) return

        Glide.with(requireContext())
            .load(listaFoto[indiceAttuale])
            .centerCrop()
            .into(profileImage)

        for (i in 0 until indicatorContainer.childCount) {
            val indicator = indicatorContainer.getChildAt(i)
            indicator.setBackgroundColor(if (i == indiceAttuale) Color.WHITE else Color.parseColor("#80FFFFFF"))
        }

        view?.findViewById<TextView>(R.id.txtName)?.text = "${utenteMatch.nome} ${utenteMatch.cognome}"
        view?.findViewById<TextView>(R.id.txtBio)?.text = utenteMatch.descrizione
        visualizzaArtiMarziali(utenteMatch.artiPraticate)
    }

    private fun visualizzaArtiMarziali(lista: List<String>) {
        val mappaId = mapOf(
            "Judo" to R.id.judo, "Karate" to R.id.karate, "Boxe" to R.id.boxe,
            "Muay Thai" to R.id.muaythai, "MMA" to R.id.mma, "Altro..." to R.id.altro
        )
        mappaId.values.forEach { view?.findViewById<TextView>(it)?.visibility = View.GONE }
        lista.forEach { arte -> mappaId[arte]?.let { view?.findViewById<TextView>(it)?.visibility = View.VISIBLE } }
    }

    private fun caricaNuovoUtente() {
        indiceAttuale = 0
        caricaDatiSupaBase()
    }
}