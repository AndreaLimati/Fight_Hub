import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fighthub.LoginActivity
import com.example.fighthub.MainFragmentProfiloModifica
import com.example.fighthub.MainFragmentProfiloUtenteFoto
import com.example.fighthub.MainFragmentStatistiche
import com.example.fighthub.R
import com.example.fighthub.controllori.ControlloreDB
import com.example.fighthub.model.Recensione
import com.example.fighthub.viewModel.UtenteViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlin.getValue
import kotlin.text.replace

class MainFragmentProfiloUtente : Fragment() {
    private val utenteViewModel : UtenteViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_profilo_utente, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : androidx.activity.OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Corpo vuoto, disabilito il tasto indietro
                }
            }
        )

        val nome = utenteViewModel.getNome()
        val eta = utenteViewModel.getEta()
        val peso = utenteViewModel.getPeso()
        val altezza = utenteViewModel.getAltezza()
        val urls = utenteViewModel.getFoto()

        Log.d("nome", "$nome")
        Log.d("eta", "$eta")

        // Imposta Nome, Età, peso, altezza
        val tvNomeEta = view.findViewById<TextView>(R.id.tvNomeEta)
        tvNomeEta.text = "$nome"+", "+"$eta"
        val tvPesoAltezza = view.findViewById<TextView>(R.id.tvPesoAltezza)
        tvPesoAltezza.text = "$peso"+"kg, "+"$altezza"+"cm"

        // foto profilo
        val immagine = view.findViewById<ImageView>(R.id.ivProfilo)
        // foto log out
        val logOut = view.findViewById<ImageView>(R.id.LogOut)

        //bottoni oni oni ma material card non bottone lol
        val btnModifica = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnModifica)

        val btnStatistiche = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnStatistiche)

        if(!urls.isNullOrEmpty()){
            Glide.with(requireContext()).load(urls[0]).into(immagine)
        }



        immagine.setOnClickListener {
            val gallery = MainFragmentProfiloUtenteFoto()  //Creazione istanza
            gallery.show(parentFragmentManager, "foto_gallery")
        }

        btnStatistiche.setOnClickListener {
            val statistiche = MainFragmentStatistiche()  //Creazione istanza
            statistiche.show(parentFragmentManager, "statistiche")
        }

        //bottone modifica
        btnModifica.setOnClickListener{
            requireActivity().supportFragmentManager.beginTransaction()
                // Animazione: entra da destra, esce a sinistra
                .setCustomAnimations(
                    android.R.anim.fade_in, // Entrata galleria
                    android.R.anim.fade_out, // Uscita galleria
                    android.R.anim.fade_in, // Ritorno al profilo (quando premi back)
                    android.R.anim.fade_out  // Scomparsa galleria (quando premi back)
                )
                .replace(R.id.fragment_chat_container, MainFragmentProfiloModifica()) // Carica Fight
                .addToBackStack(null) // Permette di tornare indietro col tasto back
                .commit()
        }

        //bottone log out:
        logOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        // Configura RecyclerView
        val rvRecensioni = view.findViewById<RecyclerView>(R.id.rvRecensioni)
        rvRecensioni.layoutManager = LinearLayoutManager(requireContext())

        val uid = utenteViewModel.getUser()?.uid
        if(uid!=null){
            ControlloreDB.getListaRecensioni(uid){ listaRecensioni->
                rvRecensioni.adapter = RecensioniAdapter(requireContext(), listaRecensioni)
            }
        }
    }
}

class RecensioniAdapter(private val context: Context, private val lista: List<Recensione>) :
    RecyclerView.Adapter<RecensioniAdapter.ViewHolder>() {
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val nome = v.findViewById<TextView>(R.id.tvAutore)
        val testo = v.findViewById<TextView>(R.id.tvTesto)
        val stelle = v.findViewById<TextView>(R.id.tvStelle)
        val immagine = v.findViewById<ImageView>(R.id.ivAutore)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_recensione, parent, false)
        return ViewHolder(v)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        if(item.valutazione!=null){
            ControlloreDB.getDatiUtente(item.recensoreUid){ u->
                holder.nome.text = u?.nome + " " + u?.cognome
                holder.testo.text = item.testo
                holder.stelle.text = "★".repeat(item.valutazione!!)
                if(!u?.urlFoto.isNullOrEmpty()){
                    Glide.with(context).load(u?.urlFoto?.get(0)).into(holder.immagine)
                }
            }
        }
    }
    override fun getItemCount() = lista.size
}

