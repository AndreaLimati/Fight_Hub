import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fighthub.R
import com.example.fighthub.viewModel.UtenteViewModel
import kotlin.getValue

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

        val nome = utenteViewModel.getNome()
        val eta = utenteViewModel.getEta()

        Log.d("nome", "$nome")
        Log.d("eta", "$eta")

        // Imposta Nome ed Età
        val tvNomeEta = view.findViewById<TextView>(R.id.tvNomeEta)
        tvNomeEta.text = "$nome"+", "+"$eta"

        // Configura RecyclerView
        val rvRecensioni = view.findViewById<RecyclerView>(R.id.rvRecensioni)
        rvRecensioni.layoutManager = LinearLayoutManager(requireContext())

        // Dati Mock
        val listaRecensioni = listOf(
            RecensioneMock("Utente1", "buon combattente, abbiamo avuto una bella sessione di sparring", 5),
            RecensioneMock("Chuck", "debole...", 1),
            RecensioneMock("Rocky", "ha del potenziale ma deve allenarsi", 4)
        )

        rvRecensioni.adapter = RecensioniAdapter(listaRecensioni)
    }
}

// Data Class per il Mockup
data class RecensioneMock(val autore: String, val testo: String, val stelle: Int)

// Adapter Interno per semplicità di copia
class RecensioniAdapter(private val lista: List<RecensioneMock>) :
    RecyclerView.Adapter<RecensioniAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val nome = v.findViewById<TextView>(R.id.tvAutore)
        val testo = v.findViewById<TextView>(R.id.tvTesto)
        val stelle = v.findViewById<TextView>(R.id.tvStelle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_recensione, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.nome.text = item.autore
        holder.testo.text = item.testo
        holder.stelle.text = "⭐ " + "★".repeat(item.stelle)
    }

    override fun getItemCount() = lista.size
}