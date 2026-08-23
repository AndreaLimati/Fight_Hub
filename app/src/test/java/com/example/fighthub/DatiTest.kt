package com.example.fighthub

import com.example.fighthub.controllori.ControlloreInterno.controllaDati
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class DatiTest {
    @Test
    fun datiGiusti_true() {
        val esito = controllaDati("Mario", "Rossi", "15/05/1995", 70, 175, "Descrizione")
        assertTrue(esito)
    }

    @Test
    fun dataPiccola_false() {
        val esito = controllaDati("Mario", "Rossi", "10/01/2010", 70, 175, "Descrizione")
        assertFalse(esito)
    }

    @Test
    fun dataFormato_false() {
        val esito = controllaDati("Mario", "Rossi", "data-sbagliata", 70, 175, "Descrizione")
        assertFalse(esito)
    }

    @Test
    fun pesoSotto_false() {
        val esito = controllaDati("Mario", "Rossi", "15/05/1995", 15, 175, "Descrizione")
        assertFalse(esito)
    }

    @Test
    fun altezzaSotto_false() {
        val esito = controllaDati("Mario", "Rossi", "15/05/1995", 70, 110, "Descrizione")
        assertFalse(esito)
    }

    @Test
    fun descrizioneVuota_false() {
        val esito = controllaDati("Mario", "Rossi", "15/05/1995", 70, 175, "")
        assertFalse(esito)
    }
}
