package com.studentprofile.app.models

/**
 * Minimal relation object capturing an association between a parent and a student.
 * Useful for index-style lookups or for representing selections without loading full ParentAccount.
 */
data class ParentStudentRelation(
    val parentId: String,
    val studentId: String
)
