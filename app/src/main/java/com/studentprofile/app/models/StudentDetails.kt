package com.studentprofile.app.models

/**
 * Aggregated student data used by the UI. This merges static profile info with
 * runtime metrics (attendance, scores, homework, behaviour and subject-wise performance).
 */
data class StudentDetails(
    val student: StudentProfile,
    val fatherName: String? = null,
    val motherName: String? = null,
    val attendancePercent: Float = 0f,
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val avgScore: Float = 0f,
    val grade: String = "",
    val rank: String = "",
    val homeworkSubmitted: Int = 0,
    val homeworkPending: Int = 0,
    val behaviour: String = "",
    val numRemarks: Int = 0,
    val subjectPerformances: List<SubjectPerformance> = emptyList()
)
