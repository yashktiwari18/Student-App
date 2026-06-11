package com.studentprofile.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.studentprofile.app.AuthViewModel
import com.studentprofile.app.R

class StudentSwitchBottomSheetFragment : BottomSheetDialogFragment() {

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_student_switch, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val container = view.findViewById<LinearLayout>(R.id.container_switch_students)
        container.removeAllViews()

        val students = authViewModel.getLinkedStudentsForCurrentSession()
        for (student in students) {
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_student_select, container, false)

            val tvName = itemView.findViewById<TextView>(R.id.tv_student_name)
            val tvClass = itemView.findViewById<TextView>(R.id.tv_student_class)
            val tvSection = itemView.findViewById<TextView>(R.id.tv_student_section)
            val tvAdmission = itemView.findViewById<TextView>(R.id.tv_student_admission)
            val avatar = itemView.findViewById<ImageView>(R.id.img_student_avatar)

            tvName.text = student.displayName
            tvClass.text = student.classInfo
            tvSection.text = student.section?.let { "Section: $it" } ?: "Section: -"
            tvAdmission.text = student.admissionId?.let { "Admission ID: $it" } ?: "Admission ID: -"
            avatar.setImageResource(student.avatarResId ?: R.drawable.ic_attendance)

            itemView.setOnClickListener {
                authViewModel.selectStudent(student.studentId)
                dismiss()
            }

            container.addView(itemView)
        }
    }
}
