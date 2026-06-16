package com.example.fighthub

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.slider.Slider

class MainFragmentMenuFiltra : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Qui devi SOLO gonfiare il layout e ritornarlo
        return inflater.inflate(R.layout.fragment_main_menu_filtra, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ORA la variabile 'view' (quella scritta nei parametri qui sopra) esiste ed è pronta!
        val sliderDistanza = view.findViewById<Slider>(R.id.sliderDistanza)
        val txtDistanzaSelezionata = view.findViewById<TextView>(R.id.txtDistanzaSelezionata)
        val btnApplicaFiltri = view.findViewById<Button>(R.id.btnApplicaFiltri)
        val btnAnnullaFiltri = view.findViewById<Button>(R.id.btnAnnullaFiltri)

        // 2. LOGICA DELLO SLIDER (Aggiorna il testo mentre muovi il dito)
        sliderDistanza.addOnChangeListener { _, value, _ ->
            txtDistanzaSelezionata.text = "${value.toInt()} KM"
        }

        btnApplicaFiltri.setOnClickListener {
            dismiss() // Chiude il popup
            }
        }
    }
