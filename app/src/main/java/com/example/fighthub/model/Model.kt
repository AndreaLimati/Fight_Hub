package com.example.fighthub.model

data class User(
    var uid: String? = null,
    var email: String? = null,
    var passw: String? = null,
    var nome: String? = null,
    var cognome: String? = null,
    var dataNascita: String? = null,
    var peso: Int? = null,
    var altezza: Int? = null,
    var descrizione: String? = null,
    var artiPraticate: List<String> = emptyList(),
    var urlFoto: List<String> = emptyList(),
    var lat: Double? = null,
    var lon: Double? = null
)

data class Recensione(
    var uid: String? = null,
    var recensoreUid: String? = null,
    var recensitoUid: String? = null,
    var valutazione: Int? = 6,
    var testo: String? = null
)

data class Chat(
    var uid: String? = null,
    var partecipanti: List<String> = emptyList(),
    var ultimoAggiornamento: Long? = null
)

data class Messaggio(
    var mittenteUid: String? = null,
    var testo: String? = null,
    var orario: Long? = null
)
