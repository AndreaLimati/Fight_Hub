package com.example.fighthub.controllori

import android.location.Location
import java.text.SimpleDateFormat
import java.util.Locale

object ControlloreInterno {
    fun validaInput(mail: String, pass: String): Int {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$" //definisce regole dell'email
        if (mail.isEmpty() || pass.isEmpty()) {
            return 1
        } else if (!mail.matches(emailRegex.toRegex())) {
            return 2
        } else if (pass.length < 6) {
            return 3
        } else {
            return 0
        }
    }
    fun controllaDati(nome: String, cognome: String, dataNascita: String, peso: Int, altezza: Int, descrizione: String): Boolean {
        if(nome.isBlank() || cognome.isBlank() || dataNascita.isBlank() || peso <= 20 || altezza <= 120 || descrizione.isBlank()) {
            return false
        }
        return try {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY).apply {
                isLenient = false //impedisce date impossibili
            }
            val dataInserita = formatter.parse(dataNascita) ?: return false
            val dataLimite = formatter.parse("01/01/2008") ?: return false

            dataInserita.before(dataLimite) || dataInserita == dataLimite
        } catch (e: Exception) {
            false
        }
    }
    fun calcolaDistanza(lat1: Double?, lon1: Double?, lat2: Double?, lon2: Double?): Float? {
        val results = FloatArray(1)
        if(lat1!=null && lat2!=null && lon1!=null && lon2!=null){
            Location.distanceBetween(lat1, lon1, lat2, lon2, results)
            return (results[0]/1000)
        }else{
            return null
        }
    }
    fun validaSelezioneArtiMarziali(artiSelezionate: Set<String>): Boolean {
        return artiSelezionate.isNotEmpty()
    }
}