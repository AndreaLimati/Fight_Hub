package com.example.fighthub

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fighthub.controllori.ControlloreDB
import com.example.fighthub.model.Chat
import com.example.fighthub.viewModel.UtenteViewModel
import kotlin.getValue

class MainFragmentChat : Fragment() {
    private val utenteViewModel : UtenteViewModel by activityViewModels()

    private val listaChat = mutableListOf<Chat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_chat, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Configura RecyclerView
        val rvChat = view.findViewById<RecyclerView>(R.id.rvChat)
        rvChat.layoutManager = LinearLayoutManager(requireContext())

        // Dati
        raccogliDati()

        rvChat.adapter = ChatAdapter(listaChat){ chatSelezionata ->
            apriDettaglioChat(chatSelezionata)
        }
    }

    private fun raccogliDati(){
        if(utenteViewModel.getUser()!=null){
            ControlloreDB.getListaChat(utenteViewModel.getUser()!!){ res ->
                if(res!=null){
                    listaChat.addAll(res)
                }
            }
        }
    }
    private fun apriDettaglioChat(chat: Chat) {
        val fragmentDettaglio = MainFragmentChatUtente() // Assicurati di averlo creato

        val altroUtente = (chat.partecipanti.subtract(listOf(utenteViewModel.getUser()?.uid))).first()

        // Passiamo i dati al nuovo fragment (il nome dell'utente)
        val bundle = Bundle()
        bundle.putString("nome_utente", altroUtente)
        fragmentDettaglio.arguments = bundle

        // Transizione con il BackStack per poter tornare indietro
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                android.R.anim.slide_out_right
            )
            .replace(R.id.fragment_chat_container, fragmentDettaglio) // Usa l'ID del tuo FrameLayout/FragmentContainerView
            .addToBackStack(null)
            .commit()
    }
}
    class ChatAdapter(private val lista: List<Chat>,private val onItemClick: (Chat) -> Unit) :
        RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val nome = v.findViewById<TextView>(R.id.tvNome)
            val testo = v.findViewById<TextView>(R.id.tvTesto)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v =
                LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = lista[position]
            holder.nome.text = "prova"
            holder.testo.text = item.ultimoAggiornamento

            //per il click
            holder.itemView.setOnClickListener {
                onItemClick(item)
            }
        }

        override fun getItemCount() = lista.size

    }