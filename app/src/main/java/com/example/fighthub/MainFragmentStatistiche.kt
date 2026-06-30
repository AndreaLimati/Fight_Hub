package com.example.fighthub

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.fighthub.controllori.ControlloreDB
import com.example.fighthub.viewModel.UtenteViewModel
import org.w3c.dom.Text
import kotlin.getValue

class MainFragmentStatistiche : DialogFragment() {
    private val utenteViewModel : UtenteViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_statistiche, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = utenteViewModel.getUser()
        val textMedia = view.findViewById<TextView>(R.id.textAverageRating)
        val ratingMedia = view.findViewById<RatingBar>(R.id.ratingBar)
        val recensioniRicevute = view.findViewById<TextView>(R.id.textReviewsReceivedCount)
        val recensioniLasciate = view.findViewById<TextView>(R.id.textReviewsLeftCount)
        val matchEffettuati = view.findViewById<TextView>(R.id.textOpponentsCount)
        val contoLike = view.findViewById<TextView>(R.id.textLikeCount)
        val contoPass = view.findViewById<TextView>(R.id.textDislikeCount)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressLikeRatio)
        val btnAnnulla = view.findViewById<Button>(R.id.buttonClose)

        if(user!=null){
            ControlloreDB.getStatistiche(user.uid!!){ media, lista ->
                textMedia.text = media.toString()
                ratingMedia.rating = media.toFloat()
                recensioniRicevute.text = lista["recRicevute"].toString()
                recensioniLasciate.text = lista["recLasciate"].toString()
                matchEffettuati.text = lista["numeroMatch"].toString()
                val like: Int = lista["likeRicevuti"]!!
                val pass: Int = lista["passRicevuti"]!!
                contoLike.text = like.toString()
                contoPass.text = pass.toString()
                if(like!=0 || pass!=0){
                    val totRisp = (like + pass).toFloat()
                    val ratio = ((like/totRisp)*100).toInt()
                    progressBar.progress = ratio
                }
            }
        }

        btnAnnulla.setOnClickListener {
            dismiss()
        }
    }

}