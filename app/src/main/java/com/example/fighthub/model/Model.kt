package com.example.fighthub.model

data class User(
    var uid: String? = null,
    var email: String? = null,
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
    var recensoreUid: String? = null,
    var recensitoUid: String? = null,
    var testo: String? = null,
    var valutazione: Int? = null
)

data class Chat(
    var uidPartecipanti: String? = null,
    var ultimoAggiornamento: String? = null,
    var altroPartecipante: String? = null
)

data class Messaggio(
    var mittenteUid: String? = null,
    var testo: String? = null,
    var orario: String? = null,
    var inviatoDaUtente: Boolean? = null
)

data class Risposta(
    var fromUid: String? = null,
    var toUid: String? = null,
    val tipo: String? = null
)
