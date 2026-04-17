package com.example.fighthub.controlloreDB

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.fighthub.model.User
import com.google.firebase.firestore.SetOptions

class ControlloreDB {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val collezioneUtenti = db.collection("utente")

    fun autenticaUtenteRegistrazione(user: User){
        auth.createUserWithEmailAndPassword(user.email!!, user.passw!!)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    user.uid = auth.currentUser?.uid ?: ""
                    salvaDatiUtente(user)
                    Log.d("Prova", "$user.uid, $user.email, $user.password")
                }else{
                    Log.e("Fallimento Registrazione", "Porcodio")
                }
            }
    }

    fun salvaDatiUtente(user: User){
        collezioneUtenti.document(user.uid!!).set(user, SetOptions.merge()).addOnSuccessListener {
            Log.d("DB", "Utente salvato con successo!")
        }
    }
}