package com.example.fighthub.controllori

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.fighthub.model.Chat
import com.example.fighthub.model.Messaggio
import com.example.fighthub.model.Recensione
import com.example.fighthub.model.Risposta
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.fighthub.model.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.PriorityQueue
import kotlin.math.abs
import kotlin.time.ExperimentalTime

object ControlloreDB {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    init {
        val settings = firestoreSettings {
            // Disattiva la persistenza su disco e usa solo la RAM
            setLocalCacheSettings(memoryCacheSettings {}) //Per impostazione predefinita, Firestore su Android salva una copia dei dati letti in memoriaper permettere all'app di funzionare anche offline
        }
        db.firestoreSettings = settings  //applica la configurazione citata prima all'istanza del database
    }
    private var distanzaMax = 100
    private var artiVolute = mutableListOf("Judo", "Karate", "Boxe", "Muay Thai", "MMA", "Altro...")

    fun autenticaUtenteRegistrazione(user: User, passw: String){
        auth.createUserWithEmailAndPassword(user.email!!, passw) //metodo predefinito fornito dall'sdk di FirebaseAuth
            .addOnCompleteListener { task -> //Listener per operazioni di rete asincrone, quando firebase finisce di elaborare la richiesta esegue il blocco di codice
                if(task.isSuccessful){  //isSuccessful predefinita di firebase booleano
                    user.uid = auth.currentUser?.uid
                    salvaDatiUtente(user) //funzione definita sotto
                }else{
                    Log.e("Fallimento Registrazione", "Registrazione fallita")  //per vedere sul logcat
                }
            }
    }                                                               //String tipo in ingresso, Unit tipo in uscita (no ritorno)
    fun verificaLoginUtente(email: String, passw: String, onResult: (String?) -> Unit){ //onResult perchè è asincrona, dobbiamo aspettare la risposta del server
        auth.signInWithEmailAndPassword(email, passw)
            .addOnCompleteListener { task -> //task è l'esito di auth
                if(task.isSuccessful){
                    val userUID = auth.currentUser?.uid
                    onResult(userUID) //passa il valore userUid all'Uid nel LoginActivity
                }else{
                    onResult(null)
                }
            }
    }

    suspend fun aggiornaProfiloEFoto(
        context: Context,
        userId: String,
        peso: Int?,
        desc: String,
        artiMarziali: Set<String>,
        vecchieFotoUrls: List<String>,   // Gli URL pubblicati precedentemente su Firestore
        nuoveFotoUris: List<Uri>        // La lista correntemente nell'Adapter (può contenere http e content://)
    ): Boolean = withContext(Dispatchers.IO) {

        if (!ControlloreInterno.validaSelezioneArtiMarziali(artiMarziali)) return@withContext false
        try {
            // STEP 1: Conversione in stringhe per confronto
            val nuoveFotoStringhe = nuoveFotoUris.map { it.toString() }

            // STEP 2: Individua ed elimina da Supabase le foto che l'utente ha rimosso
            val fotoDaEliminare = vecchieFotoUrls.filter { oldUrl -> !nuoveFotoStringhe.contains(oldUrl) }
            for (url in fotoDaEliminare) {
                ControlloreStorage.eliminaFoto(url)
            }

            // STEP 3: Carica solo le foto NUOVE (quelle con scheme content:// o file://)
            val listaUrlFinale = mutableListOf<String>()

            for (uri in nuoveFotoUris) {
                if (uri.toString().startsWith("http")) {
                    // Foto già presente su Supabase: la manteniamo
                    listaUrlFinale.add(uri.toString())
                } else {
                    // Foto locale appena aggiunta: la carichiamo
                    val urlCaricato = ControlloreStorage.caricaFoto(context, userId, uri)
                    if (urlCaricato != null) {
                        listaUrlFinale.add(urlCaricato)
                    }
                }
            }

            // STEP 4: Aggiornamento Firestore con l'elenco finale e aggiornato
            val datiAggiornati = mapOf(
                "peso" to peso,
                "descrizione" to desc,
                "artiPraticate" to artiMarziali.toList(),
                "urlFoto" to listaUrlFinale
            )

            FirebaseFirestore.getInstance()
                .collection("utente")
                .document(userId)
                .update(datiAggiornati)
                .await()

            return@withContext true

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    fun salvaDatiUtente(user: User){ //collection:collezione di utenti. Document: singola istanza di utente. Set: salva i dati user aggiungendoli (senza merge sovrascriverebbe la collection utenti)
        db.collection("utente").document(user.uid!!).set(user, SetOptions.merge()).addOnSuccessListener {
            Log.d("DB", "Utente salvato con successo!")             //con !! l'uid NON deve essere nullo altrimenti l'app crasha
        }
    }

    fun getDatiUtente(uid: String?, onResult: (User?) -> Unit){
        if(uid!=null){
            val utente = db.collection("utente").document(uid) //salva l'indirizzo dell'uid non lo scarica da internet
            utente.get().addOnSuccessListener { document -> //scarica da internet l'uid e dato che è asincrono addOnSuccessListener
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
        if(uid1!=null && uid2!=null){                               //confronta fromUid con uid2                                   //tutto quello prima è documents
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
            db.collection("chat").whereArrayContains("partecipanti", uid).get().addOnSuccessListener { risposta -> //cerca tutte le chat in cui l'utente partecipa
                val listaChat: List<Chat> = risposta.toObjects(Chat::class.java)
                if(listaChat.isEmpty()){
                    onResult(null)
                }else{
                    val listaOrdinata = listaChat.sortedByDescending { it.ultimoOrario }
                    onResult(listaOrdinata)
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun inviaMessaggio(mittenteUid: String, destinatarioUid: String, testo: String, onResult: (Boolean) -> Unit){
        val orario = FieldValue.serverTimestamp()
        val messaggio = Messaggio(mittenteUid, destinatarioUid, testo, orario)
        db.collection("messaggio").add(messaggio).addOnSuccessListener {
            db.collection("chat")
                .whereArrayContains("partecipanti", mittenteUid) //query per trovare le chat in cui c'è l'uid del mittente
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val chatTrovata = querySnapshot.documents.firstOrNull { doc -> //metodo dell'interfaccia iterable, scorre una lista e restituisce il primo elemento che rispetta una condizione
                        val chat = doc.toObject(Chat::class.java)
                        chat?.partecipanti?.contains(destinatarioUid) == true //per trovare le chat con uid destinatario, viene tutto iterato da firstOfNull
                    }

                    if (chatTrovata != null) {
                        chatTrovata.reference.update(  //reference permette di accere all'id della chat a cui non possiamo accedere, update per modificare testo e orario
                            "ultimoAggiornamento", testo,
                            "ultimoOrario", orario
                        )
                            .addOnSuccessListener { onResult(true) }
                            .addOnFailureListener { onResult(false) }
                    } else {
                        onResult(false)
                    }
                }
                .addOnFailureListener { onResult(false) }
        }.addOnFailureListener {
            Log.d("ControlloreDB", "Chat non trovata")
            onResult(false)
        }
    }

    fun getListaMessaggi(mittenteUid: String, destinatarioUid: String, onResult: (List<Messaggio>?)->Unit){
        val filtro1 = Filter.and(
            Filter.equalTo("mittenteUid", mittenteUid), //filtro in cui prendiamo i messaggi inviati dal mittente e presi dal destinatario
            Filter.equalTo("destinatarioUid", destinatarioUid)
        )
        val filtro2 = Filter.and(
            Filter.equalTo("mittenteUid", destinatarioUid), //filtro in cui prendiamo i messaggi inviati dal destinatario e presi da dal mittente
            Filter.equalTo("destinatarioUid", mittenteUid)
        )
        db.collection("messaggio").where(Filter.or(filtro1, filtro2)).orderBy("orario").addSnapshotListener { ris, err -> //SnapshotListener metodo di FireStore utilizzato per ascoltare aggionramenti in tempo reale su dei dati
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