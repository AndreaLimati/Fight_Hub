package com.example.fighthub

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.fighthub.controllori.ControlloreDB

class MainFragmentRecensione : DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_recensione, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnInvia = view.findViewById<Button>(R.id.btnInvia)
        val btnAnnulla = view.findViewById<Button>(R.id.btnAnnulla)

        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        val casellaTesto = view.findViewById<EditText>(R.id.etCommento)

        btnInvia.setOnClickListener {
            val voto = ratingBar.rating.toInt()
            val testo = casellaTesto.text.toString()

            if(testo.isNotEmpty() && voto!=0){
                //ControlloreDB.registraRecensione
                dismiss() // Chiude il popup
            }else{
                Toast.makeText(requireContext(), "Riempi i campi", Toast.LENGTH_SHORT).show()
            }
        }

        btnAnnulla.setOnClickListener {
            dismiss() // Chiude il dialog
        }

    }

}