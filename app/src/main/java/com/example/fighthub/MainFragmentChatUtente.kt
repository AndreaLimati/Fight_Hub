package com.example.fighthub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fighthub.model.Messaggio

class MessageAdapter(private val listaMessaggi: List<Messaggio>) :

    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SENT = 1
    private val TYPE_RECEIVED = 2

    // Determina quale tipo di vista usare
    override fun getItemViewType(position: Int): Int {
        return if (listaMessaggi[position].inviatoDaUtente == true) TYPE_SENT else TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SENT) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_messaggio_mandato, parent, false)
            SentViewHolder(v)
        } else {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_messaggio_ricevuto, parent, false)
            ReceivedViewHolder(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = listaMessaggi[position]
        if (holder is SentViewHolder) {
            holder.testo.text = msg.testo
            holder.ora.text = msg.orario
        } else if (holder is ReceivedViewHolder) {
            holder.testo.text = msg.testo
            holder.ora.text = msg.orario
        }
    }

    override fun getItemCount() = listaMessaggi.size

    // ViewHolder per i miei messaggi (Rossi)
    class SentViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val testo = v.findViewById<TextView>(R.id.tvMessageContent)
        val ora = v.findViewById<TextView>(R.id.tvMessageTime)
    }

    // ViewHolder per i messaggi ricevuti (Grigi)
    class ReceivedViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val testo = v.findViewById<TextView>(R.id.tvMessageContent)
        val ora = v.findViewById<TextView>(R.id.tvMessageTime)
    }
}
class MainFragmentChatUtente : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_chat_utente, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Recupera il nome dell'utente passato dal Fragment precedente
        val nomeUtente = arguments?.getString("nome_utente") ?: "Chat"
        //recycler view
        val rvMessages = view.findViewById<RecyclerView>(R.id.rvMessages)

        //messaggi esempio
        val messaggiEsempio = listOf(
            Messaggio("1", "Ehi, come è andato lo sparring?", "10:30", false),
            Messaggio("2", "Bene! Rocky ha un bel gancio destro.", "10:31", true),
            Messaggio("1", "Dovrebbe allenare di più la difesa però.", "10:31", true),
            Messaggio("2", "Concordo, Chuck è molto più tecnico.", "10:32", false),
            Messaggio("1", "Ci alleniamo domani?", "10:35", true)
        )
        rvMessages.adapter = MessageAdapter(messaggiEsempio)

        // Fa scorrere la chat all'ultimo messaggio automaticamente
        rvMessages.scrollToPosition(messaggiEsempio.size - 1)

        rvMessages.layoutManager = LinearLayoutManager(requireContext())
        // Imposta il nome nella Toolbar
        val tvName = view.findViewById<TextView>(R.id.tvChatPartnerName)
        tvName.text = nomeUtente

        // 2. Gestione Tasto Back (Freccia in alto)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            // Torna indietro nella pila dei Fragment
            parentFragmentManager.popBackStack()
        }
    }
}