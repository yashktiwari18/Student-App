package com.studentprofile.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.studentprofile.app.AuthViewModel
import com.studentprofile.app.AuthState
import com.studentprofile.app.R
import com.studentprofile.app.models.StudentProfile

class SelectStudentFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_select_student, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Determine parentId from AuthState
        val state = authViewModel.authState.value
        val parentId = when (state) {
            is AuthState.ParentAuthenticated -> state.parentId
            else -> null
        }

        // Fallback: do nothing if parentId missing
        if (parentId == null) return

        val children = authViewModel.getChildrenForParent(parentId)

        view.findViewById<TextView>(R.id.tv_student_count).text = "${children.size} Student${if (children.size == 1) "" else "s"}"

        val container = view.findViewById<LinearLayout>(R.id.container_student_list)
        container.removeAllViews()

        if (children.isEmpty()) {
            val emptyView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_student_select, container, false)
            emptyView.findViewById<TextView>(R.id.tv_student_name).text = "No linked students found"
            emptyView.findViewById<TextView>(R.id.tv_student_class).text = "Please contact support or register a student profile."
            emptyView.findViewById<TextView>(R.id.tv_student_section).text = ""
            emptyView.findViewById<TextView>(R.id.tv_student_admission).text = ""
            emptyView.findViewById<ImageView>(R.id.img_arrow).visibility = View.GONE
            emptyView.setOnClickListener(null)
            container.addView(emptyView)
            return
        }

        for (child in children) {
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_student_select, container, false)

            val img = itemView.findViewById<ImageView>(R.id.img_student_avatar)
            val tvName = itemView.findViewById<TextView>(R.id.tv_student_name)
            val tvClass = itemView.findViewById<TextView>(R.id.tv_student_class)
            val tvSection = itemView.findViewById<TextView>(R.id.tv_student_section)
            val tvAdmission = itemView.findViewById<TextView>(R.id.tv_student_admission)

            // Set values
            tvName.text = child.displayName
            tvClass.text = child.classInfo
            tvSection.text = child.section?.let { "Section: $it" } ?: "Section: -"
            tvAdmission.text = child.admissionId?.let { "Admission ID: $it" } ?: "Admission ID: -"
            img.setImageResource(child.avatarResId ?: R.drawable.ic_attendance)

            itemView.setOnClickListener {
                // Persist selection and navigate to dashboard
                authViewModel.selectStudent(child.studentId)
                findNavController().navigate(R.id.nav_dashboard) {
                    popUpTo(R.id.nav_select_student) { inclusive = true }
                }
            }

            container.addView(itemView)
        }
    }
}
