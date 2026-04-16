package com.example.fighthub

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.Date
import java.util.Locale

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"



class RegistrazioneFragment1 : Fragment() {

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Collega il layout XML della card
        val view = inflater.inflate(R.layout.fragment_registrazione1, container, false)
        val btnDate = view.findViewById<Button>(R.id.SceltaData)
        val btnInvia=view.findViewById<Button>(R.id.btnInvia)
        val peso = view.findViewById<EditText>(R.id.etPeso).text.toString().toIntOrNull() ?: 0
        val altezza = view.findViewById<EditText>(R.id.etAltezza).text.toString().toIntOrNull() ?: 0

        btnDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Seleziona data di nascita")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            // Qui usiamo parentFragmentManager perché siamo in un Fragment
            datePicker.show(parentFragmentManager, "DATE_PICKER")

            datePicker.addOnPositiveButtonClickListener { selection ->
                val dateString = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
                    Date(
                        selection
                    )
                )
                btnDate.text = dateString
            }
        }

        //bottone per cambiare fragment
        btnInvia.setOnClickListener {
            (activity as? RegistrationActivity)?.navigaAlSecondoStep()
        }



        return view
    }
}