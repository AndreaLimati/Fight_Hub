package com.example.fighthub.viewModel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fighthub.model.User
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class UtenteViewModel : ViewModel() {
    val userData = MutableLiveData(User())

    fun updateEmailPassw(m: String?){
        userData.value = userData.value?.copy(
            email = m
        )
    }

    fun updateNome(n: String?){
        userData.value = userData.value?.copy(nome = n)
    }

    fun updateCognome(c: String?){
        userData.value = userData.value?.copy(cognome = c)
    }

    fun updateDataNascita(d: String?){
        userData.value = userData.value?.copy(dataNascita = d)
    }

    fun updatePeso(p: Int?){
        userData.value = userData.value?.copy(peso = p)
    }

    fun updateAltezza(a: Int?){
        userData.value = userData.value?.copy(altezza = a)
    }

    fun updateDescrizione(desc: String?){
        userData.value = userData.value?.copy(descrizione = desc)
    }

    fun updateArtiPratiate(ap: List<String>){
        userData.value = userData.value?.copy(artiPraticate = ap)
    }

    fun updateUrlFoto(url: List<String>){
        userData.value = userData.value?.copy(urlFoto = url)
    }

    fun updatePos(la: Double?, lo: Double?){
        userData.value = userData.value?.copy(lat = la)
        userData.value = userData.value?.copy(lon = lo)
    }

    fun getUser(): User? {
        return userData.value
    }
    fun getNome(): String? {
        return userData.value?.nome
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getEta(): Int{
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        try {
            val dataNascita = LocalDate.parse(userData.value?.dataNascita, formatter)
            val oggi = LocalDate.now()
            return Period.between(dataNascita, oggi).years
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }
    }

    fun getEmail(): String?{
        return userData.value?.email
    }

    fun getPeso(): Int?{
        return userData.value?.peso
    }

    fun getAltezza(): Int?{
        return userData.value?.altezza
    }

    fun getFoto(): List<String>?{
        return userData.value?.urlFoto
    }

    fun updateTutto(u: User){
        userData.value = u
    }

    fun getLat(): Double?{
        return userData.value?.lat
    }

    fun getLon(): Double?{
        return userData.value?.lon
    }

}