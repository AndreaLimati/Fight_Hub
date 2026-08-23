package com.example.fighthub.controllori

import android.content.Context
import android.net.Uri
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

object ControlloreStorage {
    private const val url = "https://guebusnndyspxxmlmltl.supabase.co"
    private const val anon_key = "sb_publishable_3GuKGRjkyRuqjzOOjQE1kw_USNeK-5z"

    val supabase: SupabaseClient = createSupabaseClient(url, anon_key){
        install(Storage)
    }


    suspend fun salvaFoto(context: Context, dati: MutableList<Uri>): List<String>{
        return withContext(Dispatchers.IO){
            val urlCaricati = mutableListOf<String>()
            val bucket = supabase.storage.from("foto_fighthub")

            dati.forEach{ uri ->
                val inputStream = context.contentResolver.openInputStream(uri)
                val imageData = inputStream?.use { it.readBytes() }
                if(imageData!=null){
                    val fileName = "img_${UUID.randomUUID()}.jpg"
                    bucket.upload(fileName, imageData){
                        upsert=false
                    }
                    val link = bucket.publicUrl(fileName)
                    urlCaricati.add(link)
                }
            }
            urlCaricati
        }
    }
    suspend fun caricaFoto(context: Context, userId: String, uri: Uri): String? = withContext(Dispatchers.IO) {
        val bucket = supabase.storage.from("foto_fighthub")
        try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@withContext null
            val fileName = "$userId/${UUID.randomUUID()}.jpg"

            bucket.upload(fileName, bytes) { upsert = true }
            return@withContext bucket.publicUrl(fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun eliminaFoto(urlPubblico: String) = withContext(Dispatchers.IO) {
        val bucket = supabase.storage.from("foto_fighthub")
        try {
            // Estrae il path interno al bucket (es. "userId/nomefile.jpg") dall'URL pubblico
            val pathInBucket = urlPubblico.substringAfter("/foto_profili/")
            bucket.delete(pathInBucket)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}