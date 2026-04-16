package com.example.fighthub
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper

class AppDatabase(context: Context){
    fun onCreate(db: SQLiteDatabase?){
        val tabellaUtenti = ("CREATE TABLE utente(id INTEGER PRIMARY KEY," +
                "nome TEXT")
        db?.execSQL(tabellaUtenti)
    }
}