import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fighthub.MainFragmentProfiloUtenteFoto
import com.example.fighthub.R
import com.example.fighthub.controllori.ControlloreDB
import com.example.fighthub.model.Recensione
import com.example.fighthub.viewModel.UtenteViewModel
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

                if(!urls.isNullOrEmpty()){
                    Glide.with(requireContext()).load(urls[0]).into(immagine)
                }

                immagine.setOnClickListener {
                    //  apriProfilo(it) // Ora la funzione sotto diventerà colorata!
                    val gallery = MainFragmentProfiloUtenteFoto()  //Creazione istanza
                    gallery.show(parentFragmentManager, "foto_gallery")
                }

                // Configura RecyclerView
                val rvRecensioni = view.findViewById<RecyclerView>(R.id.rvRecensioni)
                rvRecensioni.layoutManager = LinearLayoutManager(requireContext())

                // Dati Mock
                val listaRecensioni = listOf(
                    Recensione("Utente1","2", "buon combattente, abbiamo avuto una bella sessione di sparring", 5),
                    Recensione("Chuck", "2","debole...", 1),
                    Recensione("Rocky", "2","ha del potenziale ma deve allenarsi", 4)
                )

                rvRecensioni.adapter = RecensioniAdapter(listaRecensioni)
            }
        }
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


