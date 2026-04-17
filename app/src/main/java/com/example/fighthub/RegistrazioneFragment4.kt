package com.example.fighthub

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.activityViewModels
import com.example.fighthub.controlloreDB.ControlloreDB
import com.example.fighthub.viewModel.RegistrazioneViewModel
import kotlin.getValue

class RegistrazioneFragment4 : Fragment() {
    private val registrazioneViewModel : RegistrazioneViewModel by activityViewModels()
    private val controlloreDB = ControlloreDB()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_registrazione4, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val accetta = view.findViewById<Button>(R.id.btnAccetta)

        accetta.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            controlloreDB.autenticaUtenteRegistrazione(registrazioneViewModel.getUser()!!)
            startActivity(intent)
        }
    }
}