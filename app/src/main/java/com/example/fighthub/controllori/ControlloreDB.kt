package com.example.fighthub.controllori

import android.content.Context
import android.location.Location
import android.util.Log
import android.widget.Toast
import com.example.fighthub.model.Chat
import com.example.fighthub.model.Messaggio
import com.example.fighthub.model.Recensione
import com.example.fighthub.model.Risposta
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.fighthub.model.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import java.sql.Time
import java.util.PriorityQueue
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

object ControlloreDB {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    init {
        val settings = firestoreSettings {
            // Disattiva la persistenza su disco e usa solo la RAM
            setLocalCacheSettings(memoryCacheSettings {})
        }
        db.firestoreSettings = settings
    }
    private var distanzaMax = 100
    private var artiVolute = mutableListOf("Judo", "Karate", "Boxe", "Muay Thai", "MMA", "Altro...")

    fun autenticaUtenteRegistrazione(user: User, passw: String){
        auth.createUserWithEmailAndPassword(user.email!!, passw)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    user.uid = auth.currentUser?.uid
                    salvaDatiUtente(user)
                }else{
                    Log.e("Fallimento Registrazione", "Registrazione fallita")
                }
            }
    }

    fun verificaLoginUtente(email: String, passw: String, onResult: (String?) -> Unit){
        auth.signInWithEmailAndPassword(email, passw)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val userUID = auth.currentUser?.uid
                    onResult(userUID)
                }else{
                    onResult(null)
                }
            }
    }

    fun salvaDatiUtente(user: User){
        db.collection("utente").document(user.uid!!).set(user, SetOptions.merge()).addOnSuccessListener {
            Log.d("DB", "Utente salvato con successo!")
        }
    }

    fun getDatiUtente(uid: String?, onResult: (User?) -> Unit){
        if(uid!=null){
            val utente = db.collection("utente").document(uid)
            utente.get().addOnSuccessListener { document ->
                val user = document.toObject<User>()
                onResult(user)
            }.addOnFailureListener{
                onResult(null)
            }
        }
    }

    fun getUidUtenteMatch(currentUser: User, onResult: (PriorityQueue<Pair<User, Double>>?) -> Unit){
        db.collection("risposta").whereEqualTo("fromUid", currentUser.uid).get().addOnSuccessListener { risposte ->
            val utentiValutati = mutableSetOf<String>()
            if(!risposte.isEmpty){
                for (doc in risposte.documents){
                    val target = doc.getString("toUid")?.trim()
                    if(target!=null){
                        utentiValutati.add(target)
                    }
                }
            }
            db.collection("utente").get().addOnSuccessListener { result ->
                if(!result.isEmpty){
                    val pq = PriorityQueue<Pair<User, Double>> { a, b ->
                        b.second.compareTo(a.second)
                    }
                    for (doc in result.documents){
                        val user = doc.toObject(User::class.java)
                        if (user?.uid!=null && user.uid!=currentUser.uid && !utentiValutati.contains(user.uid)){
                            val affinita = calcolaAffinita(currentUser, user)
                            pq.add(user to affinita)
                        }
                    }
                    onResult(pq)
                } else {
                    onResult(null)
                }
            }.addOnFailureListener {
                onResult(null)
            }
        }
    }

    fun calcolaAffinita(user1: User, user2: User): Double{
        var punteggio = 0.0

        //filtro peso
        if(user1.peso!=null && user2.peso!=null){
            val diffPeso = abs(user1.peso!! - user2.peso!!)
            punteggio += when {
                diffPeso <= 5 -> 40.0
                diffPeso <= 10 -> 20.0
                diffPeso <= 20 -> 5.0
                else -> -50.0
            }
        }

        //filtro arti
        val stiliComuni = artiVolute.intersect(user2.artiPraticate)
        punteggio += (stiliComuni.size * 25.0)

        //filtro distanza
        val dist = ControlloreInterno.calcolaDistanza(user1.lat, user1.lon, user2.lat, user2.lon)
        if (dist != null) {
            punteggio += when {
                dist <= distanzaMax/4 -> 30.0
                dist <= distanzaMax/2 -> 15.0
                dist <= distanzaMax -> 5.0
                else -> 0.0
            }
        }else{
            punteggio = 0.0
        }
        return punteggio
    }
    fun salvaRispostaUtente(risp: Risposta){
        db.collection("risposta").add(risp).addOnSuccessListener {
            if(risp.tipo=="LIKE"){
                controlloCreazioneChat(risp.fromUid, risp.toUid)
            }
        }
    }

    fun controlloCreazioneChat(uid1: String?, uid2: String?){
        if(uid1!=null && uid2!=null){
            db.collection("risposta").whereEqualTo("fromUid", uid2).get().addOnSuccessListener { documents ->
                for(document in documents){
                    val risposta = document.toObject<Risposta>()
                    if(risposta.toUid == uid1 && uid1!=uid2){
                        iniziaChat(uid1, uid2)
                    }
                }
            }
        }
    }

    fun iniziaChat(uid1: String, uid2: String){
        val chat = Chat(listOf(uid1, uid2), null)
        db.collection("chat").add(chat)
    }

    fun getListaChat(uid: String, onResult: (List<Chat>?) -> Unit){
        if(uid.isNotEmpty()){
            db.collection("chat").whereArrayContains("partecipanti", uid).get().addOnSuccessListener { risposta ->
                val listaChat: List<Chat> = risposta.toObjects(Chat::class.java)
                if(listaChat.isEmpty()){
                    onResult(null)
                }else{
                    onResult(listaChat)
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun inviaMessaggio(mittenteUid: String, destinatarioUid: String, testo: String, onResult: (Boolean) -> Unit){
        val orario = FieldValue.serverTimestamp()
        val messaggio = Messaggio(mittenteUid, destinatarioUid, testo, orario)
        db.collection("messaggio").add(messaggio).addOnSuccessListener {
            onResult(true)
        }.addOnFailureListener {
            onResult(false)
        }
    }

    fun getListaMessaggi(mittenteUid: String, destinatarioUid: String, onResult: (List<Messaggio>?)->Unit){
        val filtro1 = Filter.and(
            Filter.equalTo("mittenteUid", mittenteUid),
            Filter.equalTo("destinatarioUid", destinatarioUid)
        )
        val filtro2 = Filter.and(
            Filter.equalTo("mittenteUid", destinatarioUid),
            Filter.equalTo("destinatarioUid", mittenteUid)
        )
        db.collection("messaggio").where(Filter.or(filtro1, filtro2)).orderBy("orario").addSnapshotListener { ris, err ->
            if(err!=null){
                onResult(null)
                return@addSnapshotListener
            }
            if(ris!=null && !ris.isEmpty){
                val listaMessaggi = ris.toObjects(Messaggio::class.java)
                onResult(listaMessaggi)
            }else{
                onResult(null)
            }
        }
    }

    fun modificaParametri(distanza: Int, arti: List<String>){
        distanzaMax = distanza
        artiVolute.clear()
        artiVolute.addAll(arti)
    }

    fun inviaRecensione(toUid: String, fromUid: String, valutazione: Int, testo: String, onResult: (Boolean) -> Unit){
        val rec = Recensione(fromUid, toUid, testo, valutazione)
        db.collection("recensione").add(rec).addOnSuccessListener{
            onResult(true)
        }.addOnFailureListener {
            onResult(false)
        }
    }

    fun getListaRecensioni(toUid: String, onResult: (List<Recensione>) -> Unit){
        db.collection("recensione").whereEqualTo("recensitoUid", toUid).get().addOnSuccessListener { risp ->
            val listaRecensioni: List<Recensione> = risp.toObjects(Recensione::class.java)
            if(listaRecensioni.isNotEmpty()){
                onResult(listaRecensioni)
            }else{
                onResult(emptyList())
            }
        }
    }

    fun getStatistiche(uid: String, onResult: (Double, Map<String, Int>) -> Unit){
        var media = 0.0
        var numeroRecLasciate = 0
        var numeroRecRicevute = 0
        var nMatch = 0
        var likeRicevuti = 0
        var passRicevuti = 0

        getListaRecensioni(uid){ listaRec ->
            if(listaRec.isNotEmpty()){
                for(rec in listaRec){
                    if(rec.valutazione!=null){
                        media+=rec.valutazione!!
                    }
                }
                numeroRecRicevute = listaRec.size
                media/=numeroRecRicevute
            }
            db.collection("recensione").whereEqualTo("recensoreUid", uid).get().addOnSuccessListener { rispo ->
                if(rispo.isEmpty){
                    numeroRecLasciate = 0
                } else {
                    numeroRecLasciate = rispo.size()
                }
                getListaChat(uid){ listaMatch ->
                    if(listaMatch.isNullOrEmpty()){
                        nMatch = 0
                    } else {
                        nMatch = listaMatch.size
                    }
                    db.collection("risposta").whereEqualTo("toUid", uid).get().addOnSuccessListener { risp ->
                        val listaRisp: List<Risposta> = risp.toObjects(Risposta::class.java)
                        for(r in listaRisp){
                            if(r.tipo == "LIKE"){
                                likeRicevuti++
                            } else {
                                passRicevuti++
                            }
                        }
                        var risposta = mutableMapOf<String, Int>()
                        risposta.put("recLasciate", numeroRecLasciate)
                        risposta.put("recRicevute", numeroRecRicevute)
                        risposta.put("numeroMatch", nMatch)
                        risposta.put("likeRicevuti", likeRicevuti)
                        risposta.put("passRicevuti", passRicevuti)
                        onResult(media, risposta)
                    }
                }
            }
        }
    }
}