package com.example.fighthub

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class MainFragmentProfiloModifica : Fragment() {

    private val artiSelezionate = mutableSetOf<String>()


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