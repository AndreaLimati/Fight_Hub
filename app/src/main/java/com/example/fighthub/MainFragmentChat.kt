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
import com.example.fighthub.viewModel.UtenteViewModel
import kotlin.getValue

class MainFragmentChat : Fragment() {
    //private val utenteViewModel : UtenteViewModel by activityViewModels()

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

        // Dati Mock
        val listaChat = listOf(
            ChatMock("Utente1", "buon combattente, abbiamo avuto una bella sessione di sparring"),
            ChatMock("Chuck", "debole..."),
            ChatMock("Rocky", "ha del potenziale ma deve allenarsi")
        )

        rvChat.adapter = ChatAdapter(listaChat){ chatSelezionata ->
            apriDettaglioChat(chatSelezionata)
        }
    }

        private fun apriDettaglioChat(chat: ChatMock) {
            val fragmentDettaglio = MainFragmentChatUtente() // Assicurati di averlo creato

            // Passiamo i dati al nuovo fragment (il nome dell'utente)
            val bundle = Bundle()
            bundle.putString("nome_utente", chat.autore)
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
    data class ChatMock(val autore: String, val testo: String)

    class ChatAdapter(private val lista: List<ChatMock>,private val onItemClick: (ChatMock) -> Unit) :
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

        override fun onBindViewHolder(holder: ChatAdapter.ViewHolder, position: Int) {
            val item = lista[position]
            holder.nome.text = item.autore
            holder.testo.text = item.testo

            //per il click
            holder.itemView.setOnClickListener {
                onItemClick(item)
            }
        }

        override fun getItemCount() = lista.size

    }