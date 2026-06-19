import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fighthub.MainFragmentProfiloUtenteFoto
import com.example.fighthub.MainFragmentRecensione
import com.example.fighthub.R
import com.example.fighthub.controllori.ControlloreDB
import com.example.fighthub.viewModel.UtenteViewModel
import com.google.android.material.button.MaterialButton
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import kotlin.getValue

class MainFragmentProfiloAvversario : Fragment() {
    private val utenteViewModel : UtenteViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_profilo_avversario, container, false)
    }

    override fun onResume() {
        super.onResume()
        // Nasconde la navbar (usa l'ID che hai nella MainActivity)
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
    }

    override fun onStop() {
        super.onStop()
        // Fa riapparire la navigation bar quando esci dal fragment (così la lista la ritrova)
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.VISIBLE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ControlloreDB.getDatiUtente(arguments?.getString("uid_avversario")){ avversario ->
            if(avversario!=null){
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val dataNascita = LocalDate.parse(avversario.dataNascita, formatter)
                val oggi = LocalDate.now()

                val nome = avversario.nome
                val eta = Period.between(dataNascita, oggi).years
                val peso = avversario.peso
                val altezza = avversario.altezza
                val urls = avversario.urlFoto

                Log.d("nome", "$nome")
                Log.d("eta", "$eta")

                // Imposta Nome, Età, peso, altezza
                val tvNomeEta = view.findViewById<TextView>(R.id.tvNomeEta)
                tvNomeEta.text = "$nome"+", "+"$eta"
                val tvPesoAltezza = view.findViewById<TextView>(R.id.tvPesoAltezza)
                tvPesoAltezza.text = "$peso"+"kg, "+"$altezza"+"cm"

                // foto profilo
                val immagine = view.findViewById<ImageView>(R.id.ivProfilo)

                val inviaRecensione = view.findViewById<MaterialButton>(R.id.btnLasciaRecensione)

                val uidAvv = arguments?.getString("uid_avversario")

                if(!urls.isNullOrEmpty()){
                    Glide.with(requireContext()).load(urls[0]).into(immagine)
                }

                val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
                btnBack.setOnClickListener {
                    // Torna indietro nella pila dei Fragment
                    parentFragmentManager.popBackStack()
                }

                immagine.setOnClickListener {
                    //  apriProfilo(it) // Ora la funzione sotto diventerà colorata!
                    val gallery = MainFragmentProfiloUtenteFoto()  //Creazione istanza
                    gallery.arguments = Bundle().apply{
                        putString("uid_avversario", uidAvv)
                    }
                    gallery.show(parentFragmentManager, "foto_gallery")
                }

                inviaRecensione.setOnClickListener {
                    //  apriProfilo(it) // Ora la funzione sotto diventerà colorata!
                    val recensione = MainFragmentRecensione()  //Creazione istanza
                    recensione.arguments = Bundle().apply{
                        putString("uid_avversario", uidAvv)
                    }
                    recensione.show(parentFragmentManager, "scrivi_recensione")
                }

                // Configura RecyclerView
                val rvRecensioni = view.findViewById<RecyclerView>(R.id.rvRecensioni)
                rvRecensioni.layoutManager = LinearLayoutManager(requireContext())

                // Dati Mock
                if(uidAvv!=null){
                    ControlloreDB.getListaRecensioni(uidAvv){ listaRecensioni ->
                        rvRecensioni.adapter = RecensioniAdapter(listaRecensioni)
                    }
                }
            }
        }

        //configurazione tasto back
        val motionLayout = view.findViewById<MotionLayout>(R.id.motionLayout)
        val backCallback = object : OnBackPressedCallback(false) { // Inizialmente disattivato (false)
            override fun handleOnBackPressed() {
                // Se l'utente preme back, torniamo allo stato iniziale
                parentFragmentManager.popBackStack()
                motionLayout.transitionToStart()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)
    }

    /*fun apriProfilo(view: View){
        requireActivity().supportFragmentManager.beginTransaction()
            // Animazione: entra da destra, esce a sinistra
            .setCustomAnimations(
                android.R.anim.fade_in, // Entrata galleria
                android.R.anim.fade_out, // Uscita galleria
                android.R.anim.fade_in, // Ritorno al profilo (quando premi back)
                android.R.anim.fade_out  // Scomparsa galleria (quando premi back)
            )
            .replace(R.id.fragment_main_container, MainFragmentProfiloUtenteFoto()) // Carica Fight
            .addToBackStack(null) // Permette di tornare indietro col tasto back
            .commit()
    } */
}

// Adapter Interno per semplicità di copia


