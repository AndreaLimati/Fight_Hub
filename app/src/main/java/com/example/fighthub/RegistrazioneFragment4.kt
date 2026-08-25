package com.example.fighthub

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.fighthub.controllori.ControlloreDB
import com.example.fighthub.viewModel.UtenteViewModel
import com.google.android.gms.location.LocationServices
import kotlin.getValue

class RegistrazioneFragment4 : Fragment() {
    private val utenteViewModel : UtenteViewModel by activityViewModels()

    private lateinit var fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient

    val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            ottieniPosizioneESalva()
        } else {
            Toast.makeText(requireContext(), "Permesso negato", Toast.LENGTH_SHORT).show()
        }
    }
    companion object {
        fun newInstance(mioDato: String): RegistrazioneFragment4 {
            val fragment = RegistrazioneFragment4()
            val args = Bundle()
            args.putString("pass", mioDato)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_registrazione4, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //pulsante per accettare
        val accetta = view.findViewById<Button>(R.id.btnAccetta)
        var passw = arguments?.getString("pass")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        verificaEChiediPermesso()

        accetta.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.putExtra("uid", utenteViewModel.getUser()!!.uid)
            ControlloreDB.autenticaUtenteRegistrazione(utenteViewModel.getUser()!!, passw!!)
            startActivity(intent)
        }
    }

    private fun ottieniPosizioneESalva() {
        // Controllo di sicurezza obbligatorio per il compilatore
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude
                utenteViewModel.updatePos(lat, lon)
            }
        }
    }

    private fun verificaEChiediPermesso() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            // Se l'utente lo aveva già dato in passato, prendiamo la posizione subito
            ottieniPosizioneESalva()
        } else {
            // Altrimenti, appena caricato il fragment, esce il pop-up di sistema
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}