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
        setupClickListeners()
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
                Toast.makeText(requireContext(), "Play Video: ${classSession.subject} - Next step implementation", Toast.LENGTH_SHORT).show()
            }

            container.addView(itemView)
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
            Toast.makeText(requireContext(), "Today's Classes selected", Toast.LENGTH_SHORT).show()
        }

        binding.tabPast.setOnClickListener {
            binding.tvTabTodays.setTextColor(Color.parseColor("#718096"))
            binding.tvTabTodays.paint.isFakeBoldText = false
            binding.indicatorTodays.setBackgroundColor(Color.TRANSPARENT)

            binding.tvTabPast.setTextColor(Color.parseColor("#002874"))
            binding.tvTabPast.paint.isFakeBoldText = true
            binding.indicatorPast.setBackgroundColor(Color.parseColor("#002874"))

            Toast.makeText(requireContext(), "Past Classes - Next step implementation", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        binding.dateSelector.setOnClickListener {
            Toast.makeText(requireContext(), "Date picker - Next step implementation", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
