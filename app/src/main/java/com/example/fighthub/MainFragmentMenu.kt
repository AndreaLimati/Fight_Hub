

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
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
import io.github.jan.supabase.auth.api.AuthenticatedApiConfig
import org.w3c.dom.Text

class MainFragmentMenu : Fragment() {

    private lateinit var profileImage: ImageView
    private lateinit var indicatorContainer: LinearLayout

    // 1. Lista delle tue foto
    private var listaFoto = emptyList<String>()
    private var indiceAttuale = 0

    private var utenteMatch = User()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val controllore = ControlloreDB()

        controllore.getUidUtenteMatch { uidAvversario ->
            if(uidAvversario!=null && uidAvversario!="vuoto"){
                Log.d("prova_uid", "$uidAvversario")
                controllore.getDatiUtente(uidAvversario){datiUtenteMatch ->
                    if(datiUtenteMatch!=null){
                        utenteMatch = datiUtenteMatch.copy()
                        Log.d("entrato dentro utente", "${utenteMatch.nome}")
                        if(!utenteMatch.urlFoto.isEmpty()){
                            listaFoto = utenteMatch.urlFoto
                        }else{
                            listaFoto += "https://guebusnndyspxxmlmltl.supabase.co/storage/v1/object/public/foto_fighthub/img_945a3fa2-aaf6-4544-8447-f666808806f0.jpg"
                        }
                    }
                    setupIndicators()
                    aggiornaInterfaccia()
                }
            }
        }

        profileImage = view.findViewById(R.id.profileImage)
        indicatorContainer = view.findViewById(R.id.indicatorContainer)

        //tasto info
        val imageButton = view.findViewById<ImageButton>(R.id.imageButton)
        val motionLayout = view.findViewById<MotionLayout>(R.id.motionLayout)

        // Inizializza le lineette in alto


        // Gestione Click sull'immagine per cambiare foto
        profileImage.setOnClickListener {
            indiceAttuale = (indiceAttuale + 1) % listaFoto.size
            aggiornaInterfaccia()
        }

        //per tasto info
        var isOpen = false
        imageButton.setOnClickListener {
            if (!isOpen) {
                motionLayout.transitionToEnd()
            } else {
                motionLayout.transitionToStart()
            }
            isOpen = !isOpen
        }

        // Creiamo il callback per il tasto back
        val backCallback = object : OnBackPressedCallback(false) { // Inizialmente disattivato (false)
            override fun handleOnBackPressed() {
                // Se l'utente preme back, torniamo allo stato iniziale
                motionLayout.transitionToStart()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        // Monitoriamo lo stato del MotionLayout per attivare/disattivare il tasto back
        motionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(p0: MotionLayout?, startId: Int, endId: Int) {}
            override fun onTransitionChange(p0: MotionLayout?, startId: Int, endId: Int, progress: Float) {}
            override fun onTransitionCompleted(p0: MotionLayout?, currentId: Int) {
                // Se siamo nello stato END (info aperte), attiviamo il callback del tasto back
                // Se siamo nello stato START (foto grande), lo disattiviamo così il back fa l'azione normale
                backCallback.isEnabled = (currentId == R.id.end)
            }
            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {}
        })
        //fine configurazione tasto back
    }

    private fun setupIndicators() {
        indicatorContainer.removeAllViews()
        listaFoto.forEachIndexed { index, _ ->
            val viewS = View(context)
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            params.setMargins(8, 0, 8, 0) // Spazio tra le lineette
            viewS.layoutParams = params

            // Colore iniziale (bianco per la prima, grigio per le altre)
            viewS.setBackgroundColor(if (index == 0) Color.WHITE else Color.parseColor("#80FFFFFF"))
            indicatorContainer.addView(viewS)
        }
    }

    private fun aggiornaInterfaccia() {
        // Cambia la foto
        Glide.with(requireContext()).load(listaFoto[indiceAttuale]).into(profileImage)

        // Cambia il colore delle lineette
        for (i in 0 until indicatorContainer.childCount) {
            val indicator = indicatorContainer.getChildAt(i)
            if (i == indiceAttuale) {
                indicator.setBackgroundColor(Color.WHITE)
            } else {
                indicator.setBackgroundColor(Color.parseColor("#80FFFFFF"))
            }
        }

        //cambia le scritte

        Log.d("cambiamo utente", "${utenteMatch.nome}")

        val titolo = view?.findViewById<TextView>(R.id.txtName)
        val desc = view?.findViewById<TextView>(R.id.txtBio)
        titolo?.text = "${utenteMatch.nome}"+" "+"${utenteMatch.cognome}"
        desc?.text = "${utenteMatch.descrizione}"
        visualizzaArtiMarziali(utenteMatch.artiPraticate)
    }

    private fun visualizzaArtiMarziali(lista: List<String>){
        val mappaId = mapOf(
            "Judo" to view?.findViewById<TextView>(R.id.judo),
            "Karate" to view?.findViewById<TextView>(R.id.karate),
            "Boxe" to view?.findViewById<TextView>(R.id.boxe),
            "Muay Thai" to view?.findViewById<TextView>(R.id.muaythai),
            "MMA" to view?.findViewById<TextView>(R.id.mma),
            "Altro..." to view?.findViewById<TextView>(R.id.altro)
        )

        mappaId.values.forEach { it?.visibility = View.GONE }

        lista.forEach { arte ->
            mappaId[arte]?.apply{
                visibility = View.VISIBLE
            }
        }
    }
}