package com.example.fighthub

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.fighthub.controllori.ControlloreStorage
import com.example.fighthub.viewModel.UtenteViewModel
import kotlinx.coroutines.launch
import kotlin.getValue

class RegistrazioneFragment3 : Fragment() {

    private val utenteViewModel : UtenteViewModel by activityViewModels()
    private var selectedImagesUris = mutableListOf<Uri>()
    private lateinit var btnUpload: ImageButton
    private lateinit var tvPhotoCount: TextView
    private val MAX_IMAGES = 10
    // Launcher per selezione multipla
    private val pickMultipleImagesLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val spazioRimanente = MAX_IMAGES - selectedImagesUris.size

            if (uris.size > spazioRimanente) {
                Toast.makeText(requireContext(), "Puoi aggiungere solo altre $spazioRimanente foto!", Toast.LENGTH_SHORT).show()
                selectedImagesUris.addAll(uris.take(spazioRimanente))
            } else {
                selectedImagesUris.addAll(uris)
            }

            aggiornaUI()
        }
    }

    private fun aggiornaUI() {
        // 1. Aggiorna il testo del contatore
        tvPhotoCount.text = "Foto: ${selectedImagesUris.size}/$MAX_IMAGES"

        // 2. Mostra l'ultima foto selezionata come anteprima nel bottone
        if (selectedImagesUris.isNotEmpty()) {
            btnUpload.setImageURI(selectedImagesUris.last())
            btnUpload.scaleType = ImageView.ScaleType.CENTER_CROP
            btnUpload.imageTintList = null // Toglie il colore grigio per vedere la foto
        }

        // 3. Se abbiamo raggiunto il limite, disabilitiamo l'upload
        if (selectedImagesUris.size >= MAX_IMAGES) {
            btnUpload.isEnabled = false
            btnUpload.alpha = 0.5f
            Toast.makeText(requireContext(), "Limite massimo raggiunto!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_registrazione3, container, false)

        // Inizializzazione viste
        btnUpload = view.findViewById(R.id.btnUpload)
        tvPhotoCount = view.findViewById(R.id.tvPhotoCount)
        val btnContinua = view.findViewById<Button>(R.id.btnContinuaPhoto)

        // Listener Click Upload
        btnUpload.setOnClickListener {
            pickMultipleImagesLauncher.launch("image/*")
        }

        // Listener Click Continua
        btnContinua.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                if (selectedImagesUris.isEmpty()) {
                    Toast.makeText(requireContext(), "Inserisci almeno una foto!", Toast.LENGTH_SHORT).show()
                } else {
                    // Procedi e passa la lista di foto all'Activity
                    val urlImmagini = ControlloreStorage.salvaFoto(requireContext(), selectedImagesUris)
                    utenteViewModel.updateUrlFoto(urlImmagini)
                    (activity as? RegistrationActivity)?.navigaAlQuartoStep()
                    // Nota: Assicurati che RegistrationActivity abbia questa funzione
                }
            }
        }

        return view
    }
}