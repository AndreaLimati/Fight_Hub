package com.example.fighthub.controllori

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.fighthub.model.User
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject

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

    fun getUidUtenteMatch(onResult: (String?) -> Unit){
        db.collection("utente").get().addOnSuccessListener { result ->
            if(!result.isEmpty){
                val rand = result.documents.random()
                val uid = rand.id
                if(!uid.isEmpty()){
                    onResult(uid)
                }else{
                    onResult("vuoto")
                }
            }
        }
    }
}