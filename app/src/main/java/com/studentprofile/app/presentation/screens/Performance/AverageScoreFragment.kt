package com.studentprofile.app.presentation.screens.Performance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.studentprofile.app.R
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView


data class SubjectScore(
    val subject: String,
    val score: Int,
    val grade: String,
    val icon: Int
)
class AverageScoreFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_average_score,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupSubjectScores()

        view.findViewById<ImageView>(R.id.btnBack)
            .setOnClickListener {

                parentFragmentManager.popBackStack()

            }

    }



    private fun setupSubjectScores() {

        val container =
            requireView().findViewById<LinearLayout>(R.id.containerSubjectScores)

        container.removeAllViews()

        val subjects = listOf(

            SubjectScore(
                "English",
                82,
                "A",
                R.drawable.ic_subject_english
            ),

            SubjectScore(
                "Mathematics",
                71,
                "B",
                R.drawable.ic_subject_math
            ),

            SubjectScore(
                "Science",
                78,
                "B+",
                R.drawable.ic_subject_science
            ),

            SubjectScore(
                "Computer",
                94,
                "A+",
                R.drawable.ic_subject_computer
            )
        )

        subjects.forEach { item ->

            val subjectView = layoutInflater.inflate(
                R.layout.item_subject_score,
                container,
                false
            )

            subjectView.findViewById<ImageView>(R.id.imgSubject)
                .setImageResource(item.icon)

            subjectView.findViewById<TextView>(R.id.tvSubject).text =
                item.subject

            subjectView.findViewById<TextView>(R.id.tvScore).text =
                "${item.score}%"

            subjectView.findViewById<TextView>(R.id.tvGrade).text =
                item.grade

            subjectView.findViewById<ProgressBar>(R.id.progressSubject)
                .progress = item.score

            container.addView(subjectView)
        }
    }
}