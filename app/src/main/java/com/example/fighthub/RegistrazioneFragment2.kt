package com.example.fighthub

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.fighthub.controlloreDB.ControlloreDB
import com.example.fighthub.viewModel.RegistrazioneViewModel
import kotlin.getValue

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class RegistrazioneFragment2 : Fragment() {

    private val registrazioneViewModel : RegistrazioneViewModel by activityViewModels()

    // Set per memorizzare le arti marziali selezionate (evita duplicati)
    private val artiSelezionate = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Collega il layout XML
        val view = inflater.inflate(R.layout.fragment_registrazione2, container, false)
        val btnContinua = view.findViewById<Button>(R.id.btnContinua)

        // Lista degli ID dei bottoni definiti nell'XML
        val bottoniIds = listOf(
            R.id.btnJudo, R.id.btnKarate, R.id.btnBoxe,
            R.id.btnMuayThai, R.id.btnMMA, R.id.btnAltro
        )

        // Configura ogni bottone
        bottoniIds.forEach { id ->
            view.findViewById<Button>(id)?.let { btn ->
                btn.setOnClickListener {
                    toggleSelezione(btn)
                }
            }
        }

        // Bottone finale per inviare i dati
        view.findViewById<Button>(R.id.btnContinua)?.setOnClickListener {
            if (artiSelezionate.isEmpty()) {
                Toast.makeText(context, "Seleziona almeno un'arte marziale!", Toast.LENGTH_SHORT).show()
            } else {
                val riepilogo = artiSelezionate.joinToString(", ")
                Toast.makeText(context, "Hai scelto: $riepilogo", Toast.LENGTH_LONG).show()
                // Qui puoi chiamare una funzione dell'Activity per salvare tutto
                registrazioneViewModel.updateArtiPratiate(artiSelezionate.toList())
                (activity as? RegistrationActivity)?.navigaAlTerzoStep()
            }
        }
        return view
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