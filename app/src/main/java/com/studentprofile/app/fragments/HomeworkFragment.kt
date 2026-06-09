package com.studentprofile.app.fragments

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
import com.studentprofile.app.databinding.FragmentHomeworkBinding
import com.studentprofile.app.models.HomeworkItem

class HomeworkFragment : Fragment() {

    private var _binding: FragmentHomeworkBinding? = null
    private val binding get() = _binding!!
    private var currentTab = "all"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeworkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHomeworkCards("all")
        setupTabListeners()
        setupClickListeners()
    }

    private fun getHomeworkData(): List<HomeworkItem> {
        return listOf(
            HomeworkItem(
                "Science",
                "Chapter 6 - Life Processes",
                "Answer the following questions from page 45 to 48 in your textbook.",
                "24 May 2025",
                "28 May 2025",
                "Completed",
                R.drawable.ic_subject_science,
                R.drawable.bg_subject_icon_science
            ),
            HomeworkItem(
                "Mathematics",
                "Chapter 3 - Pair of Linear Equations",
                "Solve Exercise 3.2 (Q.1 to Q.5) in the notebook.",
                "24 May 2025",
                "29 May 2025",
                "Pending",
                R.drawable.ic_subject_math,
                R.drawable.bg_subject_icon_math
            ),
            HomeworkItem(
                "English",
                "Chapter 2 - First Flight (Prose)",
                "Read the chapter and write the summary in 150 words.",
                "23 May 2025",
                "26 May 2025",
                "Completed",
                R.drawable.ic_subject_english,
                R.drawable.bg_subject_icon_english
            ),
            HomeworkItem(
                "Hindi",
                "\u092A\u093E\u0920 2 - \u0930\u093E\u092E-\u0932\u0915\u094D\u0937\u094D\u092E\u0923-\u092A\u0930\u0936\u0941\u0930\u093E\u092E \u0938\u0902\u0935\u093E\u0926",
                "\u092A\u094D\u0930\u0936\u094D\u0928 \u0905\u092D\u094D\u092F\u093E\u0938 (\u092A\u094D\u0930\u0936\u094D\u0928 1 \u0938\u0947 5) \u0909\u0924\u094D\u0924\u0930 \u092A\u0941\u0938\u094D\u0924\u093F\u0915\u093E \u092E\u0947\u0902 \u0932\u093F\u0916\u0947\u0902\u0964",
                "23 May 2025",
                "26 May 2025",
                "Pending",
                R.drawable.ic_subject_hindi,
                R.drawable.bg_subject_icon_hindi
            ),
            HomeworkItem(
                "Computer",
                "Chapter 1 - Computer System Overview",
                "Write short notes on the following:\n1. Input Devices\n2. Output Devices",
                "22 May 2025",
                "25 May 2025",
                "Completed",
                R.drawable.ic_subject_computer,
                R.drawable.bg_subject_icon_computer
            )
        )
    }

    private fun setupHomeworkCards(filter: String) {
        val allHomework = getHomeworkData()
        val filteredHomework = when (filter) {
            "pending" -> allHomework.filter { it.status == "Pending" }
            "completed" -> allHomework.filter { it.status == "Completed" }
            else -> allHomework
        }

        val container = binding.containerHomeworkCards
        container.removeAllViews()

        for (homework in filteredHomework) {
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_homework_card, container, false)

            // Set subject icon with background
            val iconContainer = itemView.findViewById<FrameLayout>(R.id.hw_icon_container)
            iconContainer.setBackgroundResource(homework.iconBgRes)
            val iconImage = itemView.findViewById<ImageView>(R.id.img_hw_icon)
            iconImage.setImageResource(homework.iconRes)

            // Set text fields
            itemView.findViewById<TextView>(R.id.tv_hw_subject).text = homework.subject
            itemView.findViewById<TextView>(R.id.tv_hw_chapter).text = homework.chapter
            itemView.findViewById<TextView>(R.id.tv_hw_description).text = homework.description
            itemView.findViewById<TextView>(R.id.tv_hw_assigned_date).text = homework.assignedDate
            itemView.findViewById<TextView>(R.id.tv_hw_due_date).text = homework.dueDate

            // Set status badge
            val statusBadge = itemView.findViewById<LinearLayout>(R.id.badge_hw_status)
            val statusIcon = itemView.findViewById<ImageView>(R.id.img_hw_status_icon)
            val statusText = itemView.findViewById<TextView>(R.id.tv_hw_status)

            if (homework.status == "Completed") {
                statusBadge.setBackgroundResource(R.drawable.bg_completed_badge)
                statusIcon.setImageResource(R.drawable.ic_check_circle)
                statusText.text = "Completed"
                statusText.setTextColor(Color.parseColor("#28C76F"))
            } else {
                statusBadge.setBackgroundResource(R.drawable.bg_pending_badge)
                statusIcon.setImageResource(R.drawable.ic_pending_circle)
                statusText.text = "Pending"
                statusText.setTextColor(Color.parseColor("#FF9F43"))
            }

            // Download button click
            itemView.findViewById<LinearLayout>(R.id.btn_download).setOnClickListener {
                Toast.makeText(requireContext(), "Download ${homework.subject} homework - Next step implementation", Toast.LENGTH_SHORT).show()
            }

            container.addView(itemView)
        }
    }

    private fun setupTabListeners() {
        binding.tabAllHw.setOnClickListener {
            currentTab = "all"
            updateTabUI("all")
            setupHomeworkCards("all")
        }

        binding.tabPendingHw.setOnClickListener {
            currentTab = "pending"
            updateTabUI("pending")
            setupHomeworkCards("pending")
        }

        binding.tabCompletedHw.setOnClickListener {
            currentTab = "completed"
            updateTabUI("completed")
            setupHomeworkCards("completed")
        }
    }

    private fun updateTabUI(activeTab: String) {
        val navyColor = Color.parseColor("#002874")
        val grayColor = Color.parseColor("#718096")

        // Reset all tabs
        binding.tvTabAll.setTextColor(grayColor)
        binding.tvTabAll.paint.isFakeBoldText = false
        binding.indicatorAll.setBackgroundColor(Color.TRANSPARENT)

        binding.tvTabPending.setTextColor(grayColor)
        binding.tvTabPending.paint.isFakeBoldText = false
        binding.indicatorPending.setBackgroundColor(Color.TRANSPARENT)

        binding.tvTabCompleted.setTextColor(grayColor)
        binding.tvTabCompleted.paint.isFakeBoldText = false
        binding.indicatorCompleted.setBackgroundColor(Color.TRANSPARENT)

        // Activate selected tab
        when (activeTab) {
            "all" -> {
                binding.tvTabAll.setTextColor(navyColor)
                binding.tvTabAll.paint.isFakeBoldText = true
                binding.indicatorAll.setBackgroundColor(navyColor)
            }
            "pending" -> {
                binding.tvTabPending.setTextColor(navyColor)
                binding.tvTabPending.paint.isFakeBoldText = true
                binding.indicatorPending.setBackgroundColor(navyColor)
            }
            "completed" -> {
                binding.tvTabCompleted.setTextColor(navyColor)
                binding.tvTabCompleted.paint.isFakeBoldText = true
                binding.indicatorCompleted.setBackgroundColor(navyColor)
            }
        }
    }

    private fun setupClickListeners() {
        binding.dateSelectorHw.setOnClickListener {
            Toast.makeText(requireContext(), "Date picker - Next step implementation", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
