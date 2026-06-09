package com.studentprofile.app.fragments

import android.graphics.Color
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.studentprofile.app.R
import com.studentprofile.app.AuthViewModel
import com.studentprofile.app.databinding.FragmentDashboardBinding
import com.studentprofile.app.models.StudentDetails
import com.studentprofile.app.models.RecentAssessment
import com.studentprofile.app.models.SubjectPerformance
import kotlinx.coroutines.launch
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
        }
        binding.tvViewProfile.text = "Switch Student >"
        binding.tvViewProfile.setOnClickListener {
            StudentSwitchBottomSheetFragment().show(childFragmentManager, "StudentSwitchBottomSheet")
        }

        observeSelectedStudent()
        setupRecentAssessments()
        setupQuickActions()
        setupClickListeners()
        authViewModel.refreshSelectedStudentFromSession()
    }

    private fun observeSelectedStudent() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.selectedStudentDetails.collect { details ->
                    if (details != null) {
                        bindStudentDetails(details)
                    }
                }
            }
        }
    }

    private fun bindStudentDetails(details: StudentDetails) {
        binding.tvStudentName.text = details.student.displayName
        binding.tvClassInfo.text = details.student.classInfo
        binding.tvFatherName.text = details.fatherName ?: "-"
        binding.tvMotherName.text = details.motherName ?: "-"

        binding.progressAttendance.setProgress(details.attendancePercent)
        binding.tvAttendancePresent.text = "Present :  ${details.presentCount}"
        binding.tvAttendanceAbsent.text = "Absent :  ${details.absentCount}"

        binding.progressAvgScore.setProgress(details.avgScore)
        binding.tvAvgGrade.text = "Grade:   ${details.grade}"
        binding.tvAvgRank.text = "Rank:   ${details.rank}"

        val homeworkTotal = (details.homeworkSubmitted + details.homeworkPending).coerceAtLeast(1)
        val homeworkPercent = (details.homeworkSubmitted.toFloat() / homeworkTotal.toFloat()) * 100f
        binding.progressHomework.setProgress(homeworkPercent)
        binding.tvHomeworkSubmitted.text = "Submitted:  ${details.homeworkSubmitted}"
        binding.tvHomeworkPending.text = "Pending :   ${details.homeworkPending}"

        binding.tvBehaviourValue.text = details.behaviour
        binding.tvNumRemarks.text = "No. of Remarks: ${details.numRemarks}"

        renderSubjectPerformance(details.subjectPerformances)
    }

    private fun renderSubjectPerformance(subjects: List<SubjectPerformance>) {
        val container = binding.containerSubjectList
        container.removeAllViews()

        for (subject in subjects) {
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_subject_performance, container, false)

            val iconContainer = itemView.findViewById<FrameLayout>(R.id.subject_icon_container)
            iconContainer.setBackgroundResource(subject.iconBgRes)
            itemView.findViewById<ImageView>(R.id.img_subject_icon).setImageResource(subject.iconRes)
            itemView.findViewById<TextView>(R.id.tv_subject_name).text = subject.name
            val progressBar = itemView.findViewById<ProgressBar>(R.id.progress_subject)
            progressBar.progress = subject.percentage.toInt()
            progressBar.progressDrawable = createProgressDrawable(subject.progressColor)
            itemView.findViewById<TextView>(R.id.tv_percentage).text = String.format("%.1f%%", subject.percentage)
            itemView.findViewById<TextView>(R.id.tv_grade).text = subject.grade

            container.addView(itemView)

            if (subject != subjects.last()) {
                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1
                    )
                    setBackgroundColor(Color.parseColor("#EDF2F7"))
                }
                container.addView(divider)
            }
        }
    }

    private fun setupSubjectPerformance() {
        val subjects = listOf(
            SubjectPerformance("English", 82.0f, "A", R.drawable.ic_subject_english, R.drawable.bg_subject_icon_english, Color.parseColor("#28C76F")),
            SubjectPerformance("Hindi", 78.0f, "B+", R.drawable.ic_subject_hindi, R.drawable.bg_subject_icon_hindi, Color.parseColor("#EA5455")),
            SubjectPerformance("Mathematics", 74.0f, "B", R.drawable.ic_subject_math, R.drawable.bg_subject_icon_math, Color.parseColor("#FF9F43")),
            SubjectPerformance("Science", 81.0f, "A", R.drawable.ic_subject_science, R.drawable.bg_subject_icon_science, Color.parseColor("#28C76F")),
            SubjectPerformance("Social Science", 75.0f, "B+", R.drawable.ic_subject_social, R.drawable.bg_subject_icon_social, Color.parseColor("#28C76F")),
            SubjectPerformance("Computer", 90.0f, "A+", R.drawable.ic_subject_computer, R.drawable.bg_subject_icon_computer, Color.parseColor("#28C76F")),
            SubjectPerformance("Sanskrit", 85.0f, "A", R.drawable.ic_subject_sanskrit, R.drawable.bg_subject_icon_sanskrit, Color.parseColor("#28C76F"))
        )

        val container = binding.containerSubjectList
        container.removeAllViews()

        for (subject in subjects) {
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_subject_performance, container, false)

            // Set subject icon with background
            val iconContainer = itemView.findViewById<FrameLayout>(R.id.subject_icon_container)
            iconContainer.setBackgroundResource(subject.iconBgRes)
            val iconImage = itemView.findViewById<ImageView>(R.id.img_subject_icon)
            iconImage.setImageResource(subject.iconRes)

            // Set subject name
            itemView.findViewById<TextView>(R.id.tv_subject_name).text = subject.name

            // Set progress bar with custom drawable
            val progressBar = itemView.findViewById<ProgressBar>(R.id.progress_subject)
            progressBar.progress = subject.percentage.toInt()
            val progressDrawable = createProgressDrawable(subject.progressColor)
            progressBar.progressDrawable = progressDrawable

            // Set percentage text
            itemView.findViewById<TextView>(R.id.tv_percentage).text = String.format("%.1f%%", subject.percentage)

            // Set grade
            itemView.findViewById<TextView>(R.id.tv_grade).text = subject.grade

            // Click listener
            itemView.setOnClickListener {
                Toast.makeText(requireContext(), "${subject.name} details - Next step", Toast.LENGTH_SHORT).show()
            }

            container.addView(itemView)

            // Add divider except for last item
            if (subject != subjects.last()) {
                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1
                    )
                    setBackgroundColor(Color.parseColor("#EDF2F7"))
                }
                container.addView(divider)
            }
        }
    }

    private fun createProgressDrawable(color: Int): LayerDrawable {
        // Background track
        val trackShape = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 12f
            setColor(Color.parseColor("#E8ECF0"))
        }

        // Progress fill
        val progressShape = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 12f
            setColor(color)
        }

        val clipDrawable = ClipDrawable(
            progressShape,
            Gravity.START,
            ClipDrawable.HORIZONTAL
        )

        return LayerDrawable(arrayOf(trackShape, clipDrawable)).apply {
            setId(0, android.R.id.background)
            setId(1, android.R.id.progress)
        }
    }

    private fun setupRecentAssessments() {
        val assessments = listOf(
            RecentAssessment("Unit Test 4 - Science", "18 May 2025", "81.0%", "A", R.drawable.ic_assessment, R.drawable.bg_subject_icon_science),
            RecentAssessment("Unit Test 4 - Mathematics", "18 May 2025", "74.0%", "B", R.drawable.ic_assessment, R.drawable.bg_subject_icon_math),
            RecentAssessment("Half Yearly Exam - English", "10 May 2025", "82.0%", "A", R.drawable.ic_assessment, R.drawable.bg_subject_icon_english)
        )

        val container = binding.containerAssessmentList
        container.removeAllViews()

        for (assessment in assessments) {
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_recent_assessment, container, false)

            // Set icon with background
            val iconContainer = itemView.findViewById<FrameLayout>(R.id.assessment_icon_container)
            iconContainer.setBackgroundResource(assessment.iconBgRes)
            val iconImage = itemView.findViewById<ImageView>(R.id.img_assessment_icon)
            iconImage.setImageResource(assessment.iconRes)

            // Set text fields
            itemView.findViewById<TextView>(R.id.tv_assessment_title).text = assessment.title
            itemView.findViewById<TextView>(R.id.tv_assessment_date).text = assessment.date
            itemView.findViewById<TextView>(R.id.tv_marks).text = assessment.marks
            itemView.findViewById<TextView>(R.id.tv_assessment_grade).text = assessment.grade

            // Click listener
            itemView.setOnClickListener {
                Toast.makeText(requireContext(), "${assessment.title} - Next step", Toast.LENGTH_SHORT).show()
            }

            container.addView(itemView)

            // Add divider except for last
            if (assessment != assessments.last()) {
                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1
                    )
                    setBackgroundColor(Color.parseColor("#EDF2F7"))
                }
                container.addView(divider)
            }
        }
    }

    private fun setupQuickActions() {
        data class QuickAction(val label: String, val iconRes: Int)

        val actions = listOf(
            QuickAction("View Report\nCard", R.drawable.ic_report_card),
            QuickAction("View\nMarksheet", R.drawable.ic_marksheet),
            QuickAction("Homework", R.drawable.ic_homework_action),
            QuickAction("Assignments", R.drawable.ic_assignments),
            QuickAction("Attendance", R.drawable.ic_attendance)
        )

        val container = binding.containerQuickActions
        container.removeAllViews()

        for (action in actions) {
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_quick_action, container, false)

            itemView.findViewById<ImageView>(R.id.img_action_icon).setImageResource(action.iconRes)
            itemView.findViewById<TextView>(R.id.tv_action_label).text = action.label

            itemView.setOnClickListener {
                Toast.makeText(requireContext(), "${action.label.replace("\n", " ")} - Next step", Toast.LENGTH_SHORT).show()
            }

            container.addView(itemView)
        }
    }

    private fun setupClickListeners() {
        binding.tvViewProfile.setOnClickListener {
            Toast.makeText(requireContext(), "View Profile - Next step implementation", Toast.LENGTH_SHORT).show()
        }

        binding.tvSubjectViewAll.setOnClickListener {
            Toast.makeText(requireContext(), "View All Subjects - Next step implementation", Toast.LENGTH_SHORT).show()
        }

        binding.tvAssessmentViewAll.setOnClickListener {
            Toast.makeText(requireContext(), "View All Assessments - Next step implementation", Toast.LENGTH_SHORT).show()
        }

        binding.tvViewDetails.setOnClickListener {
            Toast.makeText(requireContext(), "View Behaviour Details - Next step implementation", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
