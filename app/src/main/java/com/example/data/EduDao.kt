package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EduDao {
    @Query("SELECT * FROM lesson_progress")
    fun getAllProgress(): Flow<List<LessonProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: LessonProgress)

    @Query("DELETE FROM lesson_progress WHERE lessonId = :lessonId")
    suspend fun deleteProgress(lessonId: String)

    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<Bookmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark)

    @Query("DELETE FROM bookmarks WHERE lessonId = :lessonId")
    suspend fun deleteBookmark(lessonId: String)

    @Query("SELECT * FROM study_points WHERE id = 1 LIMIT 1")
    fun getStudyPoints(): Flow<StudyPoints?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyPoints(points: StudyPoints)
}
