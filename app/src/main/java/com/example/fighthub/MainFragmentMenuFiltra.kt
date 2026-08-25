package com.example.fighthub

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.fighthub.controllori.ControlloreDB
import com.google.android.material.slider.Slider

class MainFragmentMenuFiltra : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Qui devi SOLO gonfiare il layout e ritornarlo
        return inflater.inflate(R.layout.fragment_main_menu_filtra, container, false)
    }

    private val artiSelezionate = mutableListOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sliderDistanza = view.findViewById<Slider>(R.id.sliderDistanza)
        val txtDistanzaSelezionata = view.findViewById<TextView>(R.id.txtDistanzaSelezionata)
        val btnApplicaFiltri = view.findViewById<Button>(R.id.btnApplicaFiltri)
        val btnAnnullaFiltri = view.findViewById<Button>(R.id.btnAnnullaFiltri)
        var distanzaScelta = 0

        val bottoniIds = listOf(
            R.id.btnJudo, R.id.btnKarate, R.id.btnBoxe,
            R.id.btnMuayThai, R.id.btnMMA, R.id.btnAltro
        )

        bottoniIds.forEach { id ->
            view.findViewById<Button>(id)?.let { btn ->
                btn.setOnClickListener {
                    toggleSelezione(btn)
                }
            }
        }


        // 2. LOGICA DELLO SLIDER (Aggiorna il testo mentre muovi il dito)
        sliderDistanza.addOnChangeListener { _, value, _ ->
            txtDistanzaSelezionata.text = "${value.toInt()} KM"
            distanzaScelta = value.toInt()
        }

        btnApplicaFiltri.setOnClickListener {
            if(artiSelezionate.isNotEmpty()){
                ControlloreDB.modificaParametri(distanzaScelta, artiSelezionate)
                parentFragmentManager.setFragmentResult("FILTRI_AGGIORNATI_KEY", Bundle())
                dismiss() // Chiude il popup
            }
        }

        btnAnnullaFiltri.setOnClickListener {
            dismiss() // Chiude il dialog
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.let { window ->
            val displayMetrics = resources.displayMetrics
            val width = (displayMetrics.widthPixels * 0.90).toInt()
            val height = (displayMetrics.heightPixels * 0.85).toInt()
            window.setLayout(width, height)
        }
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
