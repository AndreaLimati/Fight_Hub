package com.example.fighthub

import com.example.fighthub.controllori.ControlloreInterno.validaSelezioneArtiMarziali
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class ArtiMarzialiTest {
    @Test
    fun datiGiusti_true(){
        val esito = validaSelezioneArtiMarziali(setOf("Articola1", "Articola2"))
        assertTrue(esito)
    }
    @Test
    fun datiVuoti_false(){
        val esito = validaSelezioneArtiMarziali(setOf())
        assertFalse(esito)
    }
}