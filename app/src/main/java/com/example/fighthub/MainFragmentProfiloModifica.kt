package com.example.fighthub

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fighthub.controllori.ControlloreDB
import com.example.fighthub.controllori.ControlloreInterno
import com.example.fighthub.viewModel.UtenteViewModel
import kotlinx.coroutines.launch
import kotlin.getValue

class MainFragmentProfiloModifica : Fragment() {

    private val utenteViewModel : UtenteViewModel by activityViewModels()
    private val mockPhotoUris = mutableListOf<Uri>()
    private val maxPhotos = 5
    private lateinit var photoAdapter: PhotoAdapter

    //per aprire galleria
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            // 1. Permessi permanenti (per non perdere la foto al riavvio)
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            requireContext().contentResolver.takePersistableUriPermission(uri, flag)

            // 2. Aggiungi al ViewModel
           utenteViewModel.aggiungiFoto(uri.toString())

            // 3. Notifica all'adapter che i dati sono cambiati
            photoAdapter.notifyDataSetChanged()
        }
    }

    //adapter casino per foto lascia perdere dobbiamo rivederlo
    inner class PhotoAdapter : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_foto_modifica, parent, false)
            return PhotoViewHolder(view)
        }

        override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
            val fotoReali = utenteViewModel.getFoto() ?: emptyList()

            if (position < fotoReali.size) {
                // foto presente
                val currentUri = fotoReali[position]

                Glide.with(holder.itemView.context)
                    .load(currentUri)
                    .centerCrop()
                    .into(holder.img)

                holder.img.setBackgroundColor(Color.TRANSPARENT)

                holder.img.scaleType = ImageView.ScaleType.CENTER_CROP
                holder.btnRemove.visibility = View.VISIBLE // Mostra la X

                holder.btnRemove.setOnClickListener {
                    utenteViewModel.rimuoviFoto(position)
                    notifyDataSetChanged()
                }

                holder.img.setOnClickListener(null)

            } else {
                // foto non presente
                Glide.with(holder.itemView.context).clear(holder.img)

                holder.img.setBackgroundColor(Color.parseColor("#1E1E1E"))

                holder.img.setImageResource(R.drawable.ic_add_photo) // L'icona che hai creato
                holder.img.scaleType = ImageView.ScaleType.CENTER
                holder.btnRemove.visibility = View.GONE // Nascondi la X

                // LOGICA Vera
                holder.img.setOnClickListener {
                    if (fotoReali.size < maxPhotos) {
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    } else {
                        Toast.makeText(holder.itemView.context, "Limite di 5 foto raggiunto", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        override fun getItemCount(): Int = maxPhotos

        inner class PhotoViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val img: ImageView = v.findViewById(R.id.imgUserPhoto)
            val btnRemove: ImageButton = v.findViewById(R.id.btnRemovePhoto)
        }
    }

    private val artiSelezionate = mutableSetOf<String>()


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_main_profilo_modifica, container, false)

        val peso = rootView?.findViewById<EditText>(R.id.etPeso)
        val desc = rootView?.findViewById<EditText>(R.id.etBio)

        peso?.setText(utenteViewModel.getPeso()?.toString() ?: "")
        desc?.setText(utenteViewModel.getDescrizione() ?: "")

        // Lista degli ID dei bottoni degli sport definiti nell'XML
        val bottoniIds = listOf(
            R.id.btnJudo, R.id.btnKarate, R.id.btnBoxe,
            R.id.btnMuayThai, R.id.btnMMA, R.id.btnAltro
        )

        // Configura ogni bottone
        bottoniIds.forEach { id ->
            rootView.findViewById<Button>(id)?.let { btn ->
                btn.setOnClickListener {
                    toggleSelezione(btn)
                }
            }
        }

        val btnSave = rootView.findViewById<Button>(R.id.btnSave)
        btnSave.setOnClickListener {
            val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid

            if (currentUserId == null) {
                Toast.makeText(requireContext(), "Utente non autenticato!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!ControlloreInterno.validaSelezioneArtiMarziali(artiSelezionate)) {
                Toast.makeText(requireContext(), "Seleziona almeno un'arte marziale", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Foto correnti (possono contenere http:// e content://)
            val fotoCorrentiStringhe: List<String> = utenteViewModel.getFoto() ?: emptyList()
            val fotoCorrentiUris: List<Uri> = fotoCorrentiStringhe.map { Uri.parse(it) }

            // Foto originali caricate all'inizio dall'utente su Supabase
            val vecchieFotoUrls: List<String> = utenteViewModel.getFotoPrecedenti()

            btnSave.isEnabled = false
            Toast.makeText(requireContext(), "Salvataggio in corso...", Toast.LENGTH_SHORT).show()

            val p = peso?.text.toString().toIntOrNull()
            val d = desc?.text.toString()

            viewLifecycleOwner.lifecycleScope.launch {
                val successo = ControlloreDB.aggiornaProfiloEFoto(
                    context = requireContext(),
                    userId = currentUserId,
                    peso = p,
                    desc = d,
                    artiMarziali = artiSelezionate,
                    vecchieFotoUrls = vecchieFotoUrls,
                    nuoveFotoUris = fotoCorrentiUris
                )

                if (successo) {
                    utenteViewModel.updatePeso(p)
                    utenteViewModel.updateDescrizione(d)
                    utenteViewModel.updateArtiPratiate(artiSelezionate.toList())
                    Toast.makeText(requireContext(), "Profilo aggiornato con successo!", Toast.LENGTH_SHORT).show()
                    requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.VISIBLE
                    parentFragmentManager.popBackStack()
                } else {
                    btnSave.isEnabled = true
                    Toast.makeText(requireContext(), "Errore durante il salvataggio del profilo", Toast.LENGTH_LONG).show()
                }
            }
            parentFragmentManager.popBackStack()
        }

        photoAdapter = PhotoAdapter()

        val rvPhotos = rootView.findViewById<RecyclerView>(R.id.rvFoto)
        rvPhotos.layoutManager = GridLayoutManager(requireContext(), 3)
        rvPhotos.adapter = photoAdapter

        return rootView

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, // Assicura che la callback si distrugga con il ciclo di vita del Fragment
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Esegue il popBackStack
                    parentFragmentManager.popBackStack()
                }
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Rimuove il callback momentaneamente ed esegue il pop dal BackStack
                    isEnabled = false
                    parentFragmentManager.popBackStack()
                }
            }
        )
    }

    private fun toggleSelezione(btn: Button) {
        // Inverte lo stato grafico (isSelected attiva il selector XML)
        btn.isSelected = !btn.isSelected

        val nomeArte = btn.text.toString()

        if (btn.isSelected) {
            artiSelezionate.add(nomeArte)
            btn.setTextColor(Color.WHITE) // Feedback visivo: testo bianco se selezionato
        } else {
            artiSelezionate.remove(nomeArte)
            btn.setTextColor(Color.BLACK) // Torna nero se deselezionato
        }
    }
}