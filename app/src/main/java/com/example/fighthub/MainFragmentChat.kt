package com.example.fighthub

import android.content.Context
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
    private lateinit var chatAdapter: ChatAdapter
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

        chatAdapter = ChatAdapter(requireContext(), utenteViewModel.getUser()?.uid, listaChat){ chatSelezionata ->
            apriDettaglioChat(chatSelezionata)
        }
        rvChat.adapter = chatAdapter


        // Dati
        raccogliDati()
    }

    private fun raccogliDati(){
        if(utenteViewModel.getUser()!=null){
            ControlloreDB.getListaChat(utenteViewModel.getUser()!!){ res ->
                if(res!=null){
                    listaChat.clear()
                    listaChat.addAll(res)
                    chatAdapter.notifyDataSetChanged()
                    Log.d("lista chat", "$listaChat")
                }
            }
        }
    }
    private fun apriDettaglioChat(chat: Chat) {
        val fragmentDettaglio = MainFragmentChatUtente() // Assicurati di averlo creato

        val altroUtente = (chat.partecipanti.subtract(listOf(utenteViewModel.getUser()?.uid))).first()

        Log.d("Altroutente", "$altroUtente")
        ControlloreDB.getDatiUtente(altroUtente){ user ->
            // Passiamo i dati al nuovo fragment (il nome dell'utente)
            val bundle = Bundle()
            bundle.putString("uidUtente", user?.uid)
            bundle.putString("nome_utente", user?.nome)
            val primaFoto = user?.urlFoto?.firstOrNull()
            bundle.putString("foto", primaFoto)
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
}
    class ChatAdapter(private val context: Context, private val uid: String?, private val lista: List<Chat>, private val onItemClick: (Chat) -> Unit) :
        RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val nome = v.findViewById<TextView>(R.id.tvNome)
            val testo = v.findViewById<TextView>(R.id.tvTesto)
            val foto = v.findViewById<ImageView>(R.id.ivProfile)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v =
                LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = lista[position]
            val altroUtente = (item.partecipanti.subtract(listOf(uid))).first()
            ControlloreDB.getDatiUtente(altroUtente){ user ->
                holder.nome.text = user?.nome
                if(!user?.urlFoto.isNullOrEmpty()){
                    Glide.with(context).load(user?.urlFoto[0]).into(holder.foto)
                }
                if(item.ultimoAggiornamento!=null){
                    holder.testo.text = item.ultimoAggiornamento
                }else{
                    holder.testo.text = "Nessun aggiornamento recente"
                }

                //per il click
                holder.itemView.setOnClickListener {
                    onItemClick(item)
                }
            }
        }

        override fun getItemCount() = lista.size

    }