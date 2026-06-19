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
import androidx.fragment.app.activityViewModels
import com.example.fighthub.controllori.ControlloreDB
import com.example.fighthub.viewModel.UtenteViewModel
import kotlin.getValue

class MainFragmentRecensione : DialogFragment() {
    private val utenteViewModel : UtenteViewModel by activityViewModels()
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

        val userUid = utenteViewModel.getUser()?.uid
        val uidAvversario = arguments?.getString("uid_avversario")

        btnInvia.setOnClickListener {
            val voto = ratingBar.rating.toInt()
            val testo = casellaTesto.text.toString()
            if(testo.isNotEmpty() && voto!=0 && uidAvversario!=null && userUid!=null){
                ControlloreDB.inviaRecensione(uidAvversario, userUid, voto, testo){ ris ->
                    if(ris){
                        Toast.makeText(requireContext(), "Recensione inviata", Toast.LENGTH_SHORT).show()
                        dismiss() // Chiude il popup
                    }else{
                        Toast.makeText(requireContext(), "Errore", Toast.LENGTH_SHORT).show()
                        dismiss() // Chiude il popup
                    }
                }
            }else{
                Toast.makeText(requireContext(), "Riempi i campi", Toast.LENGTH_SHORT).show()
            }
        }

        btnAnnulla.setOnClickListener {
            dismiss() // Chiude il dialog
        }

    }

}