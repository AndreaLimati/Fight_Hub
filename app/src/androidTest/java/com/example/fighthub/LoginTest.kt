package com.example.fighthub

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)


    @Test
    fun testLoginSbagliato_false() {
        // 1. Clicchiamo sul bottone "Login" iniziale per mostrare il form
        onView(withId(R.id.button_login)).perform(click())

        // 2. Verifichiamo che il form di login sia visibile
        onView(withId(R.id.layout_login)).check(matches(isDisplayed()))

        // 3. Inseriamo email e password usando replaceText per stabilità su Android 14+
        onView(withId(R.id.login_email_input)).perform(replaceText("test.utente@fighthub.it"))
        onView(withId(R.id.login_password_input)).perform(replaceText("password123"))

        // 4. Clicchiamo il tasto "Entra"
        onView(withId(R.id.btn_conferma_login)).perform(click())
        
        // Nota: Qui il test solitamente si aspetta la navigazione verso la MainActivity.
        // Se il login è asincrono (Firebase), Espresso potrebbe aver bisogno di un piccolo attesa 
        // o di IdlingResource se non viene gestita la navigazione immediatamente.
        waitUntilViewDisplayed(withId(R.id.bottom_navigation), doesNotExist(), timeoutMs = 4000)
    }

    @Test
    fun testLoginGiusto_true() {
        // 1. Clicchiamo sul bottone "Login" iniziale per mostrare il form
        onView(withId(R.id.button_login)).perform(click())

        // 2. Verifichiamo che il form di login sia visibile
        onView(withId(R.id.layout_login)).check(matches(isDisplayed()))

        // 3. Inseriamo email e password usando replaceText per stabilità su Android 14+
        onView(withId(R.id.login_email_input)).perform(replaceText("chuck@gmail.com"))
        onView(withId(R.id.login_password_input)).perform(replaceText("norris"))

        // 4. Clicchiamo il tasto "Entra"
        onView(withId(R.id.btn_conferma_login)).perform(click())

        // Nota: Qui il test solitamente si aspetta la navigazione verso la MainActivity.
        // Se il login è asincrono (Firebase), Espresso potrebbe aver bisogno di un piccolo attesa
        // o di IdlingResource se non viene gestita la navigazione immediatamente.
        waitUntilViewDisplayed(withId(R.id.bottom_navigation), timeoutMs = 8000)
    }

    private fun waitUntilViewDisplayed(matcher: Matcher<View>, assertion: ViewAssertion = matches(isDisplayed()), timeoutMs: Long = 5000) {
        val startTime = System.currentTimeMillis()
        val endTime = startTime + timeoutMs

        while (System.currentTimeMillis() < endTime) {
            try {
                onView(matcher).check(assertion)
                return // Trovata
            } catch (e: Exception) {
                Thread.sleep(200)
            }
        }
        // Tentativo finale per sollevare l'eccezione in caso di fallimento
        onView(matcher).check(assertion)
    }
}
