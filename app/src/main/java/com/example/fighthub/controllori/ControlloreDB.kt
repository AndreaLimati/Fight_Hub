package com.example.fighthub.controllori

import android.location.Location
import android.util.Log
import com.example.fighthub.model.Chat
import com.example.fighthub.model.Messaggio
import com.example.fighthub.model.Risposta
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.fighthub.model.User
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.SetOptions
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

    fun autenticaUtenteRegistrazione(user: User, passw: String){
        auth.createUserWithEmailAndPassword(user.email!!, passw)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    user.uid = auth.currentUser?.uid
                    salvaDatiUtente(user)
                    Log.d("Prova", "$user.email, $passw")
                }else{
                    Log.e("Fallimento Registrazione", "Porcodio")
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
        Log.d("UID PRIMA CAPITO", "$uid")
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
        Log.d("UtenteUID", "${currentUser}")
        db.collection("risposta").whereEqualTo("fromUid", currentUser.uid).get().addOnSuccessListener { risposte ->
            val utentiValutati = mutableSetOf<String>()
            if(!risposte.isEmpty){
                for (doc in risposte.documents){
                    val target = doc.getString("toUid")?.trim()
                    Log.d("utentiValutati", "${target}")
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
        val stiliComuni = user1.artiPraticate.intersect(user2.artiPraticate)
        punteggio += (stiliComuni.size * 25.0)

        //filtro distanza
        val results = FloatArray(1)
        if(user1.lat!=null && user1.lon!=null && user2.lat!=null && user2.lon!=null){
            Location.distanceBetween(user1.lat!!, user1.lon!!, user2.lat!!, user2.lon!!, results)
        }
        val dist = results[0]/1000
        punteggio += when {
            dist <= 10 -> 30.0
            dist <= 30 -> 15.0
            dist <= 100 -> 5.0
            else -> 0.0
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

    fun getListaChat(utente: User, onResult: (List<Chat>?) -> Unit){
        if(utente.uid!=null){
            db.collection("chat").whereArrayContains("partecipanti", utente.uid!!).get().addOnSuccessListener { risposta ->
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
        val orario = Clock.System.now().toString()
        val messaggio = Messaggio(mittenteUid, destinatarioUid, testo, orario)
        db.collection("messaggio").add(messaggio).addOnSuccessListener {
            Log.d("Messaggio inviato", "$messaggio")
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
        db.collection("messaggio").where(Filter.or(filtro1, filtro2)).addSnapshotListener { ris, err ->
            if(err!=null){
                onResult(null)
                return@addSnapshotListener
            }
            if(ris!=null && !ris.isEmpty){
                val listaMessaggi = ris.toObjects(Messaggio::class.java).sortedBy { it.orario }
                onResult(listaMessaggi)
            }else{
                onResult(null)
            }
        }
    }
}