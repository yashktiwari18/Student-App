package com.studentprofile.app.models

import androidx.annotation.DrawableRes

data class HomeworkItem(
    val subject: String,
    val chapter: String,
    val description: String,
    val assignedDate: String,
    val dueDate: String,
    val status: String, // "Completed" or "Pending"
    @DrawableRes val iconRes: Int,
    @DrawableRes val iconBgRes: Int
)
