

import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
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
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.fighthub.R
import com.example.fighthub.controllori.ControlloreDB
import com.example.fighthub.model.Risposta
import com.example.fighthub.model.User
import com.example.fighthub.viewModel.UtenteViewModel
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.auth.api.AuthenticatedApiConfig
import org.w3c.dom.Text
import java.util.PriorityQueue
import kotlin.getValue
import kotlin.math.roundToInt

class MainFragmentMenu : Fragment() {
    private val utenteViewModel : UtenteViewModel by activityViewModels()

    private val auth = FirebaseAuth.getInstance()
    private lateinit var profileImage: ImageView
    private lateinit var indicatorContainer: LinearLayout
    private var listaFoto = emptyList<String>()
    private var indiceAttuale = 0
    private var utenteMatch = User()
    private var codaUtenti = PriorityQueue<Pair<User, Double>>()
    private lateinit var clickDetector: GestureDetector

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_menu, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        riempiCoda()

        profileImage = view.findViewById(R.id.profileImage)
        indicatorContainer = view.findViewById(R.id.indicatorContainer)

        //tasto info
        val imageButton = view.findViewById<ImageButton>(R.id.imageButton)
        val motionLayout = view.findViewById<MotionLayout>(R.id.motionLayout)
        //tasti like e pass
        val btnLike = view.findViewById<ImageButton>(R.id.btnYes)
        val btnPass = view.findViewById<ImageButton>(R.id.btnNo)

        // Inizializza le lineette in alto

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

        clickDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                indiceAttuale = (indiceAttuale + 1) % listaFoto.size
                aggiornaInterfaccia()
                Log.d("GESTURE", "Click rilevato! Apro i dettagli...")
                return true
            }
        })

        // Impedisce all'Activity di intercettare lo swipe mentre trascini la card
        profileImage.setOnTouchListener { v, event ->
            clickDetector.onTouchEvent(event)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Dice all'Activity (e al ViewPager se presente) di non toccare questo gesto
                    v.parent.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            motionLayout.onTouchEvent(event)
            false // Importante: false così il MotionLayout riceve comunque l'evento
        }

        // Creiamo il callback per il tasto back
        val backCallback = object : OnBackPressedCallback(false) { // Inizialmente disattivato (false)
            override fun handleOnBackPressed() {
                // Se l'utente preme back, torniamo allo stato iniziale
                motionLayout.transitionToStart()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        btnLike.setOnClickListener {
            // Forza il MotionLayout a spostarsi verso lo stato "like"
            motionLayout.transitionToState(R.id.like)
        }

        btnPass.setOnClickListener {
            // Forza il MotionLayout a spostarsi verso lo stato "pass"
            motionLayout.transitionToState(R.id.pass)
        }

        // Monitoriamo lo stato del MotionLayout per attivare/disattivare il tasto back
        motionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(p0: MotionLayout?, startId: Int, endId: Int) {}
            override fun onTransitionChange(p0: MotionLayout?, startId: Int, endId: Int, progress: Float) {}
            override fun onTransitionCompleted(p0: MotionLayout?, currentId: Int) {
                // Se siamo nello stato END (info aperte), attiviamo il callback del tasto back
                // Se siamo nello stato START (foto grande), lo disattiviamo così il back fa l'azione normale
                backCallback.isEnabled = (currentId == R.id.end)

                if (currentId == R.id.like || currentId == R.id.pass) {
                    if (currentId == R.id.like) {
                        Log.d("SWIPE", "Like!")
                        registraScelta("LIKE")
                    } else {
                        Log.d("SWIPE", "Pass!")
                        registraScelta("PASS")
                    }

                    val profileCard = p0?.findViewById<View>(R.id.profileCard)
                    // RESET FISICO: Forza la posizione e la rotazione a zero
                    profileCard?.let {
                        it.rotation = 0f
                        it.translationX = 0f
                        it.translationY = 0f
                    }
                    // Reset istantaneo e ricarica
                    motionLayout.progress = 0f
                    motionLayout.setTransition(R.id.start, R.id.end)
                    indiceAttuale = 0
                    getNextAvversario2()
                }
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

    @SuppressLint("SetTextI18n")
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

        //cambia le scritte occhio che cambia ogni volta che clicchi la foto dovrei solo cambiare foto ricordati!!!!
        Log.d("cambiamo utente", "${utenteMatch.nome}")

        val titolo = view?.findViewById<TextView>(R.id.txtName)
        val desc = view?.findViewById<TextView>(R.id.txtBio)
        val descAvanzata = view?.findViewById<TextView>(R.id.descrizioneAvanzata)
        val distanza = calcolaDistanza()?.roundToInt()
        titolo?.text = "${utenteMatch.nome} ${utenteMatch.cognome}"
        desc?.text = "${utenteMatch.descrizione}"
        visualizzaArtiMarziali(utenteMatch.artiPraticate)
        descAvanzata?.text = "Nato il: ${utenteMatch.dataNascita}\n" +
                            "Peso: ${utenteMatch.peso}kg\n" +
                            "Altezza: ${utenteMatch.altezza}cm\n" +
                            "Distanza: ${distanza}km"
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

    private fun calcolaDistanza(): Float?{
        val results = FloatArray(1)
        val lat1 = utenteViewModel.getLat()
        val lon1 = utenteViewModel.getLon()
        val lat2 = utenteMatch.lat
        val lon2 = utenteMatch.lon
        if(lat1!=null && lat2!=null && lon1!=null && lon2!=null){
            Location.distanceBetween(lat1, lon1, lat2, lon2, results)
            return (results[0]/1000)
        }else{
            return null
        }
    }

    private fun getNextAvversario(){
        ControlloreDB.getUidUtenteMatch { uidAvversario ->
            if(uidAvversario!=null && uidAvversario!="vuoto"){
                Log.d("prova_uid", "$uidAvversario")
                ControlloreDB.getDatiUtente(uidAvversario){datiUtenteMatch ->
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
    }

    private fun getNextAvversario2(){
        if(codaUtenti.isNotEmpty()){
            utenteMatch = codaUtenti.poll()!!.first
            if(!utenteMatch.urlFoto.isEmpty()){
                listaFoto = utenteMatch.urlFoto
            }else{
                listaFoto += "https://guebusnndyspxxmlmltl.supabase.co/storage/v1/object/public/foto_fighthub/img_945a3fa2-aaf6-4544-8447-f666808806f0.jpg"
            }
            setupIndicators()
            aggiornaInterfaccia()
        } else {
            utenteMatch = User()
            listaFoto += "https://guebusnndyspxxmlmltl.supabase.co/storage/v1/object/public/foto_fighthub/img_945a3fa2-aaf6-4544-8447-f666808806f0.jpg"
            setupIndicators()
            aggiornaInterfaccia()
        }
    }

    private fun riempiCoda(){
        if(utenteViewModel.getUser()!=null){
            ControlloreDB.getUidUtenteMatch2(utenteViewModel.getUser()!!){ coda ->
                codaUtenti = PriorityQueue(coda)
                getNextAvversario2()
            }
        }
    }

    private fun registraScelta(risp: String){
        if(auth.currentUser!=null){
            val risposta = Risposta(auth.currentUser?.uid, utenteMatch.uid, risp)
            ControlloreDB.salvaRispostaUtente(risposta)
        }
    }
}