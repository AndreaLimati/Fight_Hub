package com.example.fighthub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fighthub.controllori.ControlloreDB
import com.example.fighthub.model.Messaggio
import com.example.fighthub.model.User
import com.example.fighthub.viewModel.UtenteViewModel
import kotlin.getValue

class MainFragmentChatUtente : Fragment() {
    private val utenteViewModel : UtenteViewModel by activityViewModels()
    private lateinit var messageAdapter: MessageAdapter
    private val listaMessaggi = mutableListOf<Messaggio>()

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
        val uidUtente = arguments?.getString("uidUtente")
        val uid1 = utenteViewModel.getUser()?.uid
        //recycler view
        val rvMessages = view.findViewById<RecyclerView>(R.id.rvMessages)
        messageAdapter = MessageAdapter(utenteViewModel.getUser(), listaMessaggi)
        rvMessages.adapter = messageAdapter

        raccogliDati(uid1, uidUtente)

        val fragmentRootView = view

        ViewCompat.setOnApplyWindowInsetsListener(fragmentRootView) { v, insets ->
            // Prende lo spazio occupato dalle barre di sistema E dalla tastiera (ime)
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            // Applica il padding inferiore in base a quanto è alta la tastiera
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                if (imeInsets.bottom > 0) imeInsets.bottom else systemBars.bottom
            )
            insets
        }

        //messaggi esempio


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

        val btnInvia = view.findViewById<ImageButton>(R.id.btnSendMessage)
        btnInvia.setOnClickListener {
            val testo = view.findViewById<EditText>(R.id.etMessageInput).text.toString()
            if(!testo.isEmpty()){
                if(uid1!=null && uidUtente!=null){
                    ControlloreDB.inviaMessaggio(uid1, uidUtente, testo){ esito ->
                        if(esito){
                            raccogliDati(uid1, uidUtente)
                        }
                    }
                    view.findViewById<EditText>(R.id.etMessageInput).text.clear()
                }
            }
        }
    }
    fun raccogliDati(uid1: String?, uidUtente: String?){
        if(uid1!=null && uidUtente!=null){
            ControlloreDB.getListaMessaggi(uid1, uidUtente){ lista ->
                if(lista!=null){
                    listaMessaggi.clear()
                    listaMessaggi.addAll(lista)
                    messageAdapter.notifyDataSetChanged()
                }
            }
        }
    }
}

class MessageAdapter(private val user: User?, private var listaMessaggi: List<Messaggio>) :

    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_SENT = 1
    private val TYPE_RECEIVED = 2


    // Determina quale tipo di vista usare
    override fun getItemViewType(position: Int): Int {
        return if (listaMessaggi[position].mittenteUid == user?.uid) TYPE_SENT else TYPE_RECEIVED
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