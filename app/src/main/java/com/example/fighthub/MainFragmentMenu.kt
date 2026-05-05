

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import com.example.fighthub.R

class MainFragmentMenu : Fragment() {

    private lateinit var profileImage: ImageView
    private lateinit var indicatorContainer: LinearLayout

    // 1. Lista delle tue foto
    private val listaFoto = listOf(
        R.drawable.chuck_norris,
        R.drawable.example_2,
        R.drawable.example_3
    )
    private var indiceAttuale = 0

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

        //tasto info
        val imageButton = view.findViewById<ImageButton>(R.id.imageButton)
        val motionLayout = view.findViewById<MotionLayout>(R.id.motionLayout)

        // Inizializza le lineette in alto
        setupIndicators()

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
        profileImage.setImageResource(listaFoto[indiceAttuale])

        // Cambia il colore delle lineette
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