import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.viewfinder.core.ScaleType
import androidx.fragment.app.Fragment
import android.widget.ImageView
import com.example.fighthub.R
import com.example.fighthub.RegistrationActivity

class RegistrazioneFragment3 : Fragment() {

    private var imageUri: Uri? = null // Per salvare l'URI dell'immagine scelta
    private lateinit var btnUpload: ImageButton

    // Launcher per aprire la galleria e ricevere il risultato
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // Questa funzione viene chiamata quando l'utente sceglie un'immagine
        if (uri != null) {
            imageUri = uri
            // Mostra l'immagine scelta direttamente nel bottone centrale
            btnUpload.setImageURI(uri)
            btnUpload.scaleType = ImageView.ScaleType.CENTER_CROP // Adatta l'immagine
            btnUpload.adjustViewBounds = true
            // Rimuovi il tint dell'icona se vuoi vedere l'immagine a colori
            btnUpload.imageTintList = null
        } else {
            Toast.makeText(requireContext(), "Nessuna immagine selezionata", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_registrazione3, container, false)

        btnUpload = view.findViewById(R.id.btnUpload)
        val btnContinua = view.findViewById<Button>(R.id.btnContinuaPhoto)

        // Cliccando sull'area grigia, apriamo la galleria per "immagini/*"
        btnUpload.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnContinua.setOnClickListener {
            if (imageUri == null) {
                Toast.makeText(requireContext(), "Inserisci almeno una foto!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Foto salvata! Procediamo...", Toast.LENGTH_SHORT).show()
                // Qui chiamerai la funzione dell'Activity per andare al prossimo step
            }
        }

        btnContinua.setOnClickListener {
            (activity as? RegistrationActivity)?.navigaAlQuartoStep()
        }


        return view
    }
}