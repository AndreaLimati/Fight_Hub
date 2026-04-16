package com.example.fighthub.model

import java.text.SimpleDateFormat

data class UserRegistration(
    var email: String? = null,
    var passw: String? = null,
    var nome: String? = null,
    var cognome: String? = null,
    var dataNascita: SimpleDateFormat? = null,
    var peso: Int? = null,
    var altezza: Int? = null,
    var descrizione: String? = null,
    var artiPraticate: Set<String> = emptySet(),
    var urlFoto: Set<String> = emptySet()
)
