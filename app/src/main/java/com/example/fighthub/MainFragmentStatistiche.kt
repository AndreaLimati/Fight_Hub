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
        var user = utenteViewModel.getUser()
        var textMedia = view.findViewById<TextView>(R.id.textAverageRating)
        var ratingMedia = view.findViewById<RatingBar>(R.id.ratingBar)
        var recensioniRicevute = view.findViewById<TextView>(R.id.textReviewsReceivedCount)
        var recensioniLasciate = view.findViewById<TextView>(R.id.textReviewsLeftCount)
        var matchEffettuati = view.findViewById<TextView>(R.id.textOpponentsCount)
        var contoLike = view.findViewById<TextView>(R.id.textLikeCount)
        var contoPass = view.findViewById<TextView>(R.id.textDislikeCount)
        var progressBar = view.findViewById<ProgressBar>(R.id.progressLikeRatio)
        var btnAnnulla = view.findViewById<Button>(R.id.buttonClose)

        if(user!=null){
            ControlloreDB.getStatistiche(user.uid!!){ media, lista ->
                textMedia.text = media.toString()
                ratingMedia.rating = media.toFloat()
                recensioniRicevute.text = lista["recRicevute"].toString()
                recensioniLasciate.text = lista["recLasciate"].toString()
                matchEffettuati.text = lista["numeroMatch"].toString()
                contoLike.text = lista["likeRicevuti"].toString()
                contoPass.text = lista["passRicevuti"].toString()
                if(lista["likeRicevuti"]!=0 || lista["passRicevuti"]!=0){
                    var totLike = lista["likeRicevuti"]?.plus(lista["passRicevuti"]!!)
                    if(totLike!=0){
                        progressBar.progress = lista["likeRicevuti"]?.div(totLike!!)!!*100
                    }
                }
            }
        }

        btnAnnulla.setOnClickListener {
            dismiss()
        }
    }

}