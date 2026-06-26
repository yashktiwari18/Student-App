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

data class HomeworkSubject(
    val subject: String,
    val submitted: Int,
    val total: Int,
    val progress: Int,
    val icon: Int
)
class HomeworkStatsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_homework_stats,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.btnBack)
            .setOnClickListener {
                parentFragmentManager.popBackStack()
            }


        setupHomeworkSubjects()
    }



    private fun setupHomeworkSubjects() {

        val container =
            requireView().findViewById<LinearLayout>(R.id.containerHomeworkSubjects)

        container.removeAllViews()

        val subjects = listOf(

            HomeworkSubject(
                "English",
                8,
                8,
                100,
                R.drawable.ic_subject_english
            ),

            HomeworkSubject(
                "Mathematics",
                4,
                5,
                80,
                R.drawable.ic_subject_math
            ),

            HomeworkSubject(
                "Science",
                9,
                10,
                90,
                R.drawable.ic_subject_science
            ),

            HomeworkSubject(
                "Computer",
                6,
                7,
                86,
                R.drawable.ic_subject_computer
            )
        )

        subjects.forEach { item ->

            val subjectView = layoutInflater.inflate(
                R.layout.item_homework_subject,
                container,
                false
            )

            subjectView.findViewById<ImageView>(R.id.imgSubject)
                .setImageResource(item.icon)

            subjectView.findViewById<TextView>(R.id.tvSubject)
                .text = item.subject

            subjectView.findViewById<TextView>(R.id.tvSubmission)
                .text = "${item.submitted} / ${item.total} Submitted"

            subjectView.findViewById<ProgressBar>(R.id.progressHomework)
                .progress = item.progress

            container.addView(subjectView)
        }
    }
}