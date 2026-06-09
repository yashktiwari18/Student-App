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

        val container = view.findViewById<LinearLayout>(R.id.container_student_list)
        container.removeAllViews()

        for (child in children) {
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_student_select, container, false)

            val img = itemView.findViewById<ImageView>(R.id.img_student_avatar)
            val tvName = itemView.findViewById<TextView>(R.id.tv_student_name)
            val tvClass = itemView.findViewById<TextView>(R.id.tv_student_class)
            val tvSection = itemView.findViewById<TextView>(R.id.tv_student_section)

            // Set values
            tvName.text = child.displayName
            tvClass.text = child.classInfo
            tvSection.text = child.section?.let { "Section: $it" } ?: "Section: -"

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
