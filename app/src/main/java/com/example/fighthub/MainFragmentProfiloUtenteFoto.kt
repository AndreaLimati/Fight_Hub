package com.example.fighthub

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.example.fighthub.viewModel.UtenteViewModel
import androidx.fragment.app.DialogFragment // Cambiato da Fragment a DialogFragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.fighthub.controllori.ControlloreDB
import kotlin.getValue

class MainFragmentProfiloUtenteFoto : DialogFragment() { // Estende DialogFragment
    private val utenteViewModel : UtenteViewModel by activityViewModels()
    private lateinit var profileImage: ImageView
    private lateinit var indicatorContainer: LinearLayout
    private var urls: List<String> = emptyList()
    private var indiceAttuale = 0

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        if (window != null) {
            // 1. Rimuove il background predefinito del sistema (che è nero/bianco)
            window.setBackgroundDrawableResource(android.R.color.transparent)

            // 2. Opzionale: Rimuove l'oscuramento (dim) se vuoi che sia chiarissimo
            // window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

            // 3. Forza il dialog a occupare tutto lo schermo
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    // AGGIUNTO: Imposta lo stile per la trasparenza e il tutto schermo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Usa il layout che abbiamo aggiornato con l'ID rootLayout
        return inflater.inflate(R.layout.fragment_main_profilo_utente_foto, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //val urls = utenteViewModel.getFoto()

        profileImage = view.findViewById(R.id.profileImage)
        indicatorContainer = view.findViewById(R.id.indicatorContainer)
        //if nell'intent c'è qualcosa prendi url foto avversario, altrimenti questo sotto
        val uidAvversario = arguments?.getString("uid_avversario")
        if(uidAvversario!=null){
            ControlloreDB.getDatiUtente(uidAvversario){ u ->
                if(u != null){
                    urls = u.urlFoto
                    setupIndicators()
                    val immagine = view.findViewById<ImageView>(R.id.profileImage)
                    if(!urls.isNullOrEmpty()){
                        Glide.with(requireContext()).load(urls[indiceAttuale]).into(immagine)
                    }
                }
            }
        } else {
            urls = utenteViewModel.getFoto() ?: emptyList()
            setupIndicators()
            val immagine = view.findViewById<ImageView>(R.id.profileImage)
            if(!urls.isNullOrEmpty()){
                Glide.with(requireContext()).load(urls[indiceAttuale]).into(immagine)
            }
        }

        // AGGIUNTO: Logica per chiudere il dialog toccando lo sfondo scuro
        // Assicurati che nel tuo XML il ConstraintLayout principale abbia android:id="@+id/rootLayout"
        val rootLayout = view.findViewById<View>(R.id.rootLayout)
        rootLayout?.setOnClickListener {
            dismiss() // Chiude il Dialog e torna al profilo
        }

        // Inizializza le lineette in alto


        // Gestione Click sull'immagine per cambiare foto (Mantenuta tua logica)
        profileImage.setOnClickListener {
            indiceAttuale = (indiceAttuale + 1) % urls.size
            aggiornaInterfaccia()
        }

        // Evita che il click sulla foto chiuda il dialog (ferma il click al livello dell'immagine)
        profileImage.isClickable = true

        // Imposta foto

    }

    private fun setupIndicators() {
        indicatorContainer.removeAllViews()
        urls.forEachIndexed { index, _ ->
            val viewS = View(context)
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            params.setMargins(8, 0, 8, 0)
            viewS.layoutParams = params

            viewS.setBackgroundColor(if (index == 0) Color.WHITE else Color.parseColor("#80FFFFFF"))
            indicatorContainer.addView(viewS)
        }
    }

    private fun aggiornaInterfaccia() {
        if (urls.isEmpty()) return

        Glide.with(this)
            .load(urls[indiceAttuale])
            .centerCrop()
            .into(profileImage)

        for (i in 0 until indicatorContainer.childCount) {
            val indicator = indicatorContainer.getChildAt(i)
            if (i == indiceAttuale) {
                indicator.setBackgroundColor(Color.WHITE)
            } else {
                indicator.setBackgroundColor(Color.parseColor("#80FFFFFF"))
            }
        }
    }
}