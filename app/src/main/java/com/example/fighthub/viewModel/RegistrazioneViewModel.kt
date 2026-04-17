package com.example.fighthub.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fighthub.model.User

class RegistrazioneViewModel : ViewModel() {
    val userData = MutableLiveData(User())

    fun updateUid(u: String?){
        userData.value = userData.value?.copy(uid = u)
    }
    fun updateEmailPassw(m: String?, p: String?){
        userData.value = userData.value?.copy(
            email = m,
            passw = p
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
}