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

    fun autenticaUtenteRegistrazione(email: String?, password: String?){
        if (email != null && password != null) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        val uid = auth.currentUser?.uid ?: ""
                        val utenteNuovo = User(uid = uid, email = email, passw = password)
                        salvaDatiUtente(utenteNuovo)
                        Log.d("Prova", "$uid, $email, $password")
                    }
                }
        }
    }

    fun salvaDatiUtente(user: User){
        collezioneUtenti.document(user.uid!!).set(user, SetOptions.merge()).addOnSuccessListener {
            Log.d("DB", "Utente salvato con successo!")
        }
    }
}