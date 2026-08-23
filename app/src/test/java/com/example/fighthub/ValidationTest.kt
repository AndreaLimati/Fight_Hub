package com.example.fighthub

import com.example.fighthub.controllori.ControlloreInterno.validaInput
import org.junit.Assert
import org.junit.Test

class ValidationTest {
    @Test
    fun campiVuoti_false() {
        val result = validaInput("", "123456")
        Assert.assertEquals(1, result.toLong())
    }

    @Test
    fun emailSbagliata_false() {
        val result = validaInput("mailErrata", "123456")
        Assert.assertEquals(2, result.toLong())
    }

    @Test
    fun passCorta_false() {
        val result = validaInput("test@test.com", "123")
        Assert.assertEquals(3, result.toLong())
    }

    @Test
    fun datiCorretti_true() {
        val result = validaInput("test@test.com", "123456")
        Assert.assertEquals(0, result.toLong())
    }
}
