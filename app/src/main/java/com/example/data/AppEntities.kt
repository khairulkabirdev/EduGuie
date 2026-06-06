package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lesson_progress")
data class LessonProgress(
    @PrimaryKey val lessonId: String,
    val classNum: Int,
    val subjectId: String,
    val completed: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey val lessonId: String,
    val lessonName: String,
    val classNum: Int,
    val subjectId: String,
    val subjectName: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_points")
data class StudyPoints(
    @PrimaryKey val id: Int = 1,
    val totalPoints: Int = 0,
    val starsEarned: Int = 0
)
