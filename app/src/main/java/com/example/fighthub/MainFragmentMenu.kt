

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
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fighthub.MainFragmentMenuFiltra
import com.example.fighthub.MainFragmentProfiloUtenteFoto
import com.example.fighthub.R
import com.example.fighthub.controllori.ControlloreDB
import com.example.fighthub.controllori.ControlloreInterno
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

        parentFragmentManager.setFragmentResultListener("FILTRI_AGGIORNATI_KEY", viewLifecycleOwner) { _, _ ->
            indiceAttuale = 0
            codaUtenti.clear()
            riempiCoda()
        }

        riempiCoda()

        profileImage = view.findViewById(R.id.profileImage)
        indicatorContainer = view.findViewById(R.id.indicatorContainer)

        //tasto info
        val imageButton = view.findViewById<ImageButton>(R.id.imageButton)
        val motionLayout = view.findViewById<MotionLayout>(R.id.motionLayout)
        //tasti like e pass
        val btnLike = view.findViewById<ImageButton>(R.id.btnYes)
        val btnPass = view.findViewById<ImageButton>(R.id.btnNo)
        val btnFiltra = view.findViewById<ImageButton>(R.id.btn_filtra)

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
                return true
            }
        })

        // Impedisce all'Activity di intercettare lo swipe mentre trascini la card
        profileImage.setOnTouchListener { v, event -> //v view, event il MotionEvent
            clickDetector.onTouchEvent(event)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> { //action down premere
                    // Dice all'Activity (e al ViewPager se presente) di non toccare questo gesto
                    v.parent.requestDisallowInterceptTouchEvent(true) //gestisce la funzione lo swipe
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { //action down rilasciare il dito,
                    v.parent.requestDisallowInterceptTouchEvent(false) //finisce a gestire lo swipe
                }
            }
            motionLayout.onTouchEvent(event)
            false // Importante: false così il MotionLayout riceve comunque l'evento
        }

        // Creiamo il callback per il tasto back, per tornare dai dettagli dell'utente alla visuale normale
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
            override fun onTransitionStarted(p0: MotionLayout?, startId: Int, endId: Int) {} //quando implemento un interfaccia devo ridefinire i metodi anche se non li uso
            override fun onTransitionChange(p0: MotionLayout?, startId: Int, endId: Int, progress: Float) {}
            override fun onTransitionCompleted(p0: MotionLayout?, currentId: Int) {
                // Se siamo nello stato END (info aperte), attiviamo il callback del tasto back
                // Se siamo nello stato START (foto grande), lo disattiviamo così il back fa l'azione normale
                backCallback.isEnabled = (currentId == R.id.end) //un if abbreviato che controlla se sto nella sezione info

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
                    motionLayout.setTransition(R.id.start, R.id.end)//ridefinisce la transizione dallo start all'end
                    indiceAttuale = 0
                    getNextAvversario()
                }
            }
            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {}
        })
        //fine configurazione tasto back

        btnFiltra.setOnClickListener {
            //  apriProfilo(it) // Ora la funzione sotto diventerà colorata!
            val filtro = MainFragmentMenuFiltra()  //Creazione istanza
            filtro.show(parentFragmentManager, "foto_gallery") //apre dialog fragment
        }
    }

    private fun setupIndicators() {
        indicatorContainer.removeAllViews() //rimuove tutte le view nel contenitore
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
        val titolo = view?.findViewById<TextView>(R.id.txtName)
        val desc = view?.findViewById<TextView>(R.id.txtBio)
        val descAvanzata = view?.findViewById<TextView>(R.id.descrizioneAvanzata)
        val distanza = ControlloreInterno.calcolaDistanza(utenteViewModel.getLat(), utenteViewModel.getLon(), utenteMatch.lat, utenteMatch.lon)?.roundToInt()
        if(codaUtenti.isEmpty()){
            titolo?.text = "Utenti terminati"
            desc?.text = ""
            descAvanzata?.text = ""
            visualizzaArtiMarziali(emptyList())
        } else {
            // Cambia la foto
            Glide.with(requireContext()).load(listaFoto[indiceAttuale]).into(profileImage)

            // Cambia il colore delle lineette
            for (i in 0 until indicatorContainer.childCount) { //scorre tutti gli elementi di indicatorContainer
                val indicator = indicatorContainer.getChildAt(i)
                if (i == indiceAttuale) {
                    indicator.setBackgroundColor(Color.WHITE)
                } else {
                    indicator.setBackgroundColor(Color.parseColor("#80FFFFFF"))
                }
            }

            Log.d("cambiamo utente", "${utenteMatch.nome}")

            titolo?.text = "${utenteMatch.nome} ${utenteMatch.cognome}"
            desc?.text = "${utenteMatch.descrizione}"
            visualizzaArtiMarziali(utenteMatch.artiPraticate)
            descAvanzata?.text = "Nato il: ${utenteMatch.dataNascita}\n" +
                    "Peso: ${utenteMatch.peso}kg\n" +
                    "Altezza: ${utenteMatch.altezza}cm\n" +
                    "Distanza: ${distanza}km"

            val rvRecensioni = view?.findViewById<RecyclerView>(R.id.rvRecensioni)
            rvRecensioni?.layoutManager = LinearLayoutManager(requireContext())
            val uidAvv = utenteMatch.uid
            if(uidAvv!=null){
                ControlloreDB.getListaRecensioni(uidAvv){ listaRecensioni ->
                    rvRecensioni?.adapter = RecensioniAdapter(requireContext(), listaRecensioni)
                }
            }
        }
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

        mappaId.values.forEach { it?.visibility = View.GONE } //non si vedono tutte le arti

        lista.forEach { arte -> //itero e attivo solo le arti interessate
            mappaId[arte]?.apply{
                visibility = View.VISIBLE
            }
        }
    }

    private fun getNextAvversario(){
        if(codaUtenti.isNotEmpty()){
            utenteMatch = codaUtenti.poll()!!.first //pool rimuove il primo elemento in cima alla coda e dato che codaUtenti sono coppie di dati .first indica proprio questo
            if(!utenteMatch.urlFoto.isEmpty()){ //se l'url della foto di utenteMatch non è vuoto
                listaFoto = utenteMatch.urlFoto //url foto è una lista
            }else{
                Toast.makeText(requireContext(), "nessuna foto", Toast.LENGTH_SHORT).show()
            }
            setupIndicators()
            aggiornaInterfaccia()
        } else {
            utenteMatch = User()
            Toast.makeText(requireContext(), "nessun utente", Toast.LENGTH_SHORT).show()
            setupIndicators()
            aggiornaInterfaccia()
        }
    }

    private fun riempiCoda(){
        if(utenteViewModel.getUser()?.uid!=null){
            ControlloreDB.getUidUtenteMatch(utenteViewModel.getUser()!!){ coda ->
                codaUtenti = PriorityQueue(coda)
                getNextAvversario()
            }
        }
    }

    private fun registraScelta(risp: String){
        if(auth.currentUser!=null && utenteMatch.uid!=null){
            val risposta = Risposta(auth.currentUser?.uid, utenteMatch.uid, risp)
            ControlloreDB.salvaRispostaUtente(risposta)
        }
    }
}