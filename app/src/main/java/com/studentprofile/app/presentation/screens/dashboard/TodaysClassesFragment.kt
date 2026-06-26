package com.studentprofile.app.presentation.screens.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.studentprofile.app.R
import com.studentprofile.app.databinding.FragmentTodaysClassesBinding
import com.studentprofile.app.domain.models.ClassSession

class TodaysClassesFragment : Fragment() {

    private var _binding: FragmentTodaysClassesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodaysClassesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClassCards()
        setupTabListeners()
        binding.containerPastClasses.visibility = View.GONE




    }



    private fun setupClassCards() {
        val classes = listOf(
            ClassSession(
                "08:00 AM - 08:45 AM",
                "English",
                "Chapter 2 - First Flight (Prose)",
                "Teacher: Ananya Mishra",
                "Completed",
                R.drawable.ic_subject_english,
                R.drawable.bg_subject_icon_english
            ),
            ClassSession(
                "08:45 AM - 09:30 AM",
                "Hindi",
                "पाठ 2 - राम-लक्ष्मण-परशुराम संवाद",
                "Teacher: Priya Tiwari",
                "Completed",
                R.drawable.ic_subject_hindi,
                R.drawable.bg_subject_icon_hindi
            ),
            ClassSession(
                "09:30 AM - 10:00 AM",
                "Computer",
                "Chapter 1 - Computer System Overview",
                "Teacher: Vivek Singh",
                "Completed",
                R.drawable.ic_subject_computer,
                R.drawable.bg_subject_icon_computer
            )
        )

        val container = binding.containerClassCards
        container.removeAllViews()

        for (classSession in classes) {
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_class_card, container, false)

            itemView.findViewById<TextView>(R.id.tv_time_range).text = classSession.timeRange

            val iconContainer = itemView.findViewById<FrameLayout>(R.id.class_icon_container)
            iconContainer.setBackgroundResource(classSession.iconBgRes)
            val iconImage = itemView.findViewById<ImageView>(R.id.img_class_icon)
            iconImage.setImageResource(classSession.iconRes)

            itemView.findViewById<TextView>(R.id.tv_class_subject).text = classSession.subject
            itemView.findViewById<TextView>(R.id.tv_class_chapter).text = classSession.chapter
            itemView.findViewById<TextView>(R.id.tv_class_teacher).text = classSession.teacher
            itemView.findViewById<TextView>(R.id.tv_class_status).text = classSession.status

            itemView.findViewById<LinearLayout>(R.id.btn_play_video).setOnClickListener {
                Toast.makeText(requireContext(), "Watch Recording: ${classSession.subject} - Next step implementation", Toast.LENGTH_SHORT).show()
            }

            container.addView(itemView)
        }
    }

    private fun setupPastClasses() {

        val container = binding.containerPastClasses

        container.removeAllViews()

        addDateSection(
            container,
            "23 Jun 2026",
            listOf(
                "English",
                "Mathematics"
            )
        )

        addDateSection(
            container,
            "22 Jun 2026",
            listOf(
                "Science"
            )
        )

        addDateSection(
            container,
            "21 Jun 2026",
            listOf(
                "Computer",
                "Hindi"
            )
        )
    }

    private fun addDateSection(
        container: LinearLayout,
        date: String,
        subjects: List<String>
    ) {

        val headerView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_date_header, container, false)

        headerView.findViewById<TextView>(R.id.tvDateHeader).text = date

        container.addView(headerView)

        subjects.forEach { subject ->

            val cardView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_class_card, container, false)

            cardView.findViewById<TextView>(R.id.tv_time_range)
                .text = "Recorded Class"

            cardView.findViewById<TextView>(R.id.tv_class_subject)
                .text = subject

            val iconContainer =
                cardView.findViewById<FrameLayout>(R.id.class_icon_container)

            val iconImage =
                cardView.findViewById<ImageView>(R.id.img_class_icon)

            when(subject) {

                "English" -> {
                    iconContainer.setBackgroundResource(
                        R.drawable.bg_subject_icon_english
                    )
                    iconImage.setImageResource(
                        R.drawable.ic_subject_english
                    )
                }

                "Mathematics" -> {
                    iconContainer.setBackgroundResource(
                        R.drawable.bg_subject_icon_math
                    )
                    iconImage.setImageResource(
                        R.drawable.ic_subject_math
                    )
                }

                "Science" -> {
                    iconContainer.setBackgroundResource(
                        R.drawable.bg_subject_icon_science
                    )
                    iconImage.setImageResource(
                        R.drawable.ic_subject_science
                    )
                }

                "Computer" -> {
                    iconContainer.setBackgroundResource(
                        R.drawable.bg_subject_icon_computer
                    )
                    iconImage.setImageResource(
                        R.drawable.ic_subject_computer
                    )
                }

                "Hindi" -> {
                    iconContainer.setBackgroundResource(
                        R.drawable.bg_subject_icon_hindi
                    )
                    iconImage.setImageResource(
                        R.drawable.ic_subject_hindi
                    )
                }
            }

            cardView.findViewById<TextView>(R.id.tv_class_chapter)
                .text = "Recorded Session"

            cardView.findViewById<TextView>(R.id.tv_class_teacher)
                .text = "Teacher Available"

            cardView.findViewById<TextView>(R.id.tv_class_status)
                .text = "Completed"

            container.addView(cardView)
        }
    }

    private fun setupTabListeners() {
        binding.tabTodays.setOnClickListener {
            binding.tvTabTodays.setTextColor(Color.parseColor("#002874"))
            binding.tvTabTodays.paint.isFakeBoldText = true
            binding.indicatorTodays.setBackgroundColor(Color.parseColor("#002874"))

            binding.tvTabPast.setTextColor(Color.parseColor("#718096"))
            binding.tvTabPast.paint.isFakeBoldText = false
            binding.indicatorPast.setBackgroundColor(Color.TRANSPARENT)


            binding.containerClassCards.visibility = View.VISIBLE
            binding.containerPastClasses.visibility = View.GONE
            binding.layoutCompletedHeader.visibility = View.VISIBLE
        }

        binding.tabPast.setOnClickListener {
            binding.tvTabTodays.setTextColor(Color.parseColor("#718096"))
            binding.tvTabTodays.paint.isFakeBoldText = false
            binding.indicatorTodays.setBackgroundColor(Color.TRANSPARENT)
            binding.tvTabPast.setTextColor(Color.parseColor("#002874"))
            binding.tvTabPast.paint.isFakeBoldText = true
            binding.indicatorPast.setBackgroundColor(Color.parseColor("#002874"))
            binding.containerClassCards.visibility = View.GONE
            binding.containerPastClasses.visibility = View.VISIBLE
            binding.layoutCompletedHeader.visibility = View.GONE
            setupPastClasses()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
