package com.example.fighthub

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.fighthub.controllori.ControlloreInterno
import com.example.fighthub.viewModel.UtenteViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.Date
import java.util.Locale
import kotlin.getValue

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"



class RegistrazioneFragment1 : Fragment() {

    private val utenteViewModel : UtenteViewModel by activityViewModels()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Collega il layout XML della card
        val view = inflater.inflate(R.layout.fragment_registrazione1, container, false)

        val btnDate = view.findViewById<Button>(R.id.SceltaData)
        val btnInvia=view.findViewById<Button>(R.id.btnInvia)

        var dateString: String? = null

        btnDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Seleziona data di nascita")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            // Qui usiamo parentFragmentManager perché siamo in un Fragment
            datePicker.show(parentFragmentManager, "DATE_PICKER")

            datePicker.addOnPositiveButtonClickListener { selection ->
                dateString = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
                    Date(
                        selection
                    )
                )
                btnDate.text = dateString
            }
        }

        //bottone per cambiare fragment
        btnInvia.setOnClickListener {
            //leggo variabili
            val nome = view.findViewById<EditText>(R.id.etNome).text.toString()
            val cognome = view.findViewById<EditText>(R.id.etCognome).text.toString()
            val peso = view.findViewById<EditText>(R.id.etPeso).text.toString().toIntOrNull() ?: 0
            val altezza = view.findViewById<EditText>(R.id.etAltezza).text.toString().toIntOrNull() ?: 0
            val descrizione = view.findViewById<EditText>(R.id.etDescrizione).text.toString()
            if(ControlloreInterno.controllaDati(nome, cognome, dateString!!, peso, altezza, descrizione)){
                //salvo info sul viewmodel
                utenteViewModel.updateNome(nome)
                utenteViewModel.updateCognome(cognome)
                utenteViewModel.updateDataNascita(dateString)
                utenteViewModel.updatePeso(peso)
                utenteViewModel.updateAltezza(altezza)
                utenteViewModel.updateDescrizione(descrizione)
                //mando allo step 2
                (activity as? RegistrationActivity)?.navigaAlSecondoStep()
            }else{
                Toast.makeText(requireContext(), "Campi non validi", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }
}