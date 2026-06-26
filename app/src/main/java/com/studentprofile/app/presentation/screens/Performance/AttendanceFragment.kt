package com.studentprofile.app.presentation.screens.Performance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.studentprofile.app.R
import android.graphics.Color
import android.widget.LinearLayout
import android.widget.TextView
class AttendanceFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_attendance,
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

        setupRecentAbsences(view)

    }


    private fun setupRecentAbsences(view: View) {

        val container =
            view.findViewById<LinearLayout>(R.id.containerAbsences)

        container.removeAllViews()

        val absences = listOf(
            Triple("12 Jun 2026", "Medical Leave", "Approved"),
            Triple("03 Jun 2026", "Absent", "No Leave"),
            Triple("25 May 2026", "Family Function", "Approved")
        )

        absences.forEach {

            val item = layoutInflater.inflate(
                R.layout.item_absence,
                container,
                false
            )

            item.findViewById<TextView>(R.id.tvDate).text = it.first
            item.findViewById<TextView>(R.id.tvReason).text = it.second

            val status = item.findViewById<TextView>(R.id.tvStatus)
            status.text = it.third

            if (it.third == "No Leave") {
                status.setBackgroundResource(R.drawable.bg_notification_badge)
                status.setTextColor(Color.parseColor("#EF6C00"))
            }

            container.addView(item)
        }
    }
}