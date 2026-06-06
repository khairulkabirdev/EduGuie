package com.example.data

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.io.IOException

class EduRepository(
    private val context: Context,
    private val eduDao: EduDao
) {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    fun loadClassLevels(): List<ClassLevel> {
        return try {
            // 1. Read classes.json
            val classesJson = context.assets.open("classes.json").bufferedReader().use { it.readText() }
            val rawClassListType = Types.newParameterizedType(List::class.java, RawClass::class.java)
            val rawClassListAdapter = moshi.adapter<List<RawClass>>(rawClassListType)
            val rawClasses = rawClassListAdapter.fromJson(classesJson) ?: emptyList()

            // 2. Read units.json
            val unitsJson = context.assets.open("units.json").bufferedReader().use { it.readText() }
            val rawUnitListType = Types.newParameterizedType(List::class.java, RawUnit::class.java)
            val rawUnitListAdapter = moshi.adapter<List<RawUnit>>(rawUnitListType)
            val rawUnits = rawUnitListAdapter.fromJson(unitsJson) ?: emptyList()

            // 3. Read lessons.json
            val lessonsJson = context.assets.open("lessons.json").bufferedReader().use { it.readText() }
            val rawLessonListType = Types.newParameterizedType(List::class.java, RawLesson::class.java)
            val rawLessonListAdapter = moshi.adapter<List<RawLesson>>(rawLessonListType)
            val rawLessons = rawLessonListAdapter.fromJson(lessonsJson) ?: emptyList()

            // 4. Read lesson_contents.json
            val contentsJson = context.assets.open("lesson_contents.json").bufferedReader().use { it.readText() }
            val contentMapType = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
            val contentMapAdapter = moshi.adapter<Map<String, String>>(contentMapType)
            val lessonContents = contentMapAdapter.fromJson(contentsJson) ?: emptyMap()

            // Stitch everything together nicely
            rawClasses.map { rc ->
                val subjects = rc.subjects.map { rs ->
                    val units = rawUnits.filter { it.subjectId == rs.id }.map { ru ->
                        val lessons = rawLessons.filter { it.unitId == ru.id }.map { rl ->
                            val htmlContent = lessonContents[rl.id] ?: "<div class='card'><h2>${rl.name}</h2><p>Course content is loading...</p></div>"
                            Lesson(id = rl.id, name = rl.name, content = htmlContent)
                        }
                        UnitClass(id = ru.id, name = ru.name, lessons = lessons)
                    }
                    Subject(id = rs.id, name = rs.name, icon = rs.icon, units = units)
                }
                ClassLevel(
                    classNum = rc.classNum,
                    className = rc.className,
                    description = rc.description,
                    subjects = subjects
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            getFallbackClassLevels()
        }
    }

    // Room DB Interactions
    fun getAllProgress(): Flow<List<LessonProgress>> = eduDao.getAllProgress()
    suspend fun insertProgress(progress: LessonProgress) = eduDao.insertProgress(progress)
    suspend fun deleteProgress(lessonId: String) = eduDao.deleteProgress(lessonId)

    fun getAllBookmarks(): Flow<List<Bookmark>> = eduDao.getAllBookmarks()
    suspend fun insertBookmark(bookmark: Bookmark) = eduDao.insertBookmark(bookmark)
    suspend fun deleteBookmark(lessonId: String) = eduDao.deleteBookmark(lessonId)

    fun getStudyPoints(): Flow<StudyPoints?> = eduDao.getStudyPoints()
    
    suspend fun addPoints(points: Int, stars: Int) {
        val current = eduDao.getStudyPoints().firstOrNull() ?: StudyPoints(id = 1, totalPoints = 0, starsEarned = 0)
        val updated = current.copy(
            totalPoints = current.totalPoints + points,
            starsEarned = current.starsEarned + stars
        )
        eduDao.insertStudyPoints(updated)
    }

    // High quality fallback in case reading from assets fails
    private fun getFallbackClassLevels(): List<ClassLevel> {
        return listOf(
            ClassLevel(
                classNum = 6,
                className = "Class 6",
                description = "Foundation arithmetic and natural studies",
                subjects = listOf(
                    Subject(
                        id = "fallback_c6_math",
                        name = "Mathematics",
                        icon = "Calculate",
                        units = listOf(
                            UnitClass(
                                id = "fallback_c6_math_u1",
                                name = "Unit 1: Playing with numbers",
                                lessons = listOf(
                                    Lesson(
                                        id = "fallback_c6_math_u1_l1",
                                        name = "Lesson 1: Introduction to Prime Numbers",
                                        content = "<h3>Prime Numbers</h3><p>Whole numbers with only two factors: 1 and itself.</p>"
                                    )
                                )
                             )
                        )
                    )
                )
            )
        )
    }
}
