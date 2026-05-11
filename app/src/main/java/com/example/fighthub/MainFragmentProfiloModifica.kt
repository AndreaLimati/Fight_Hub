package com.example.fighthub

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainFragmentProfiloModifica : Fragment() {

    private val mockPhotoUris = mutableListOf<Uri>()
    private val maxPhotos = 5
    private lateinit var photoAdapter: PhotoAdapter
    //adapter casino per foto lascia perdere dobbiamo rivederlo
    inner class PhotoAdapter : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_foto_modifica, parent, false)
            return PhotoViewHolder(view)
        }

        override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
            // Controlliamo se nella nostra lista MOCK c'è una foto per questa posizione
            if (position < mockPhotoUris.size) {
                // --- CASO: FOTO PRESENTE (Mock) ---
                val currentUri = mockPhotoUris[position]

                // Dato che sono Uri finti, usiamo setImageDrawable per mostrare i colori
                // ma manteniamo l'adapter pronto per l'URI vero
                //holder.img.setImageURI(currentUri)

                // MOCK VISIVO: Mostriamo quadratini colorati invece di foto
                val colors = listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.MAGENTA)
                holder.img.setBackgroundColor(colors[position % colors.size])

                holder.img.scaleType = ImageView.ScaleType.CENTER_CROP
                holder.btnRemove.visibility = View.VISIBLE // Mostra la X

                holder.btnRemove.setOnClickListener {
                    mockPhotoUris.removeAt(position)
                    notifyDataSetChanged() // Rinfresca la griglia
                }

                holder.img.setOnClickListener(null)

            } else {
                // --- CASO: SLOT VUOTO (Tasto +) ---
                // Ripuliamo lo sfondo colorato del mock
                holder.img.setBackgroundColor(Color.parseColor("#1E1E1E"))

                holder.img.setImageResource(R.drawable.ic_add_photo) // L'icona che hai creato
                holder.img.scaleType = ImageView.ScaleType.CENTER
                holder.btnRemove.visibility = View.GONE // Nascondi la X

                // LOGICA MOCK: Invece della galleria, aggiunge una foto finta
                holder.img.setOnClickListener {
                    if (mockPhotoUris.size < maxPhotos) {
                        // Creiamo un Uri finto e lo aggiungiamo
                        val fakeUri = Uri.parse("mock://photo_${System.currentTimeMillis()}")
                        mockPhotoUris.add(fakeUri)
                        notifyDataSetChanged()
                    } else {
                        Toast.makeText(requireContext(), "Limite di 5 foto raggiunto", Toast.LENGTH_SHORT).show()
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
            // Torna indietro nella pila dei Fragment temporaneo
            parentFragmentManager.popBackStack()
        }

        photoAdapter = PhotoAdapter()

        val rvPhotos = rootView.findViewById<RecyclerView>(R.id.rvFoto)
        rvPhotos.layoutManager = GridLayoutManager(requireContext(), 3)
        rvPhotos.adapter = photoAdapter

        return rootView
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