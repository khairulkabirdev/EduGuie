package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Lesson(
    val id: String,
    val name: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class UnitClass(
    val id: String,
    val name: String,
    val lessons: List<Lesson>
)

@JsonClass(generateAdapter = true)
data class Subject(
    val id: String,
    val name: String,
    val icon: String,
    val units: List<UnitClass>
)

@JsonClass(generateAdapter = true)
data class ClassLevel(
    val classNum: Int,
    val className: String,
    val description: String,
    val subjects: List<Subject>
)

// Raw intermediate structures for the 4 separate JSON files
@JsonClass(generateAdapter = true)
data class RawSubject(
    val id: String,
    val name: String,
    val icon: String
)

@JsonClass(generateAdapter = true)
data class RawClass(
    val classNum: Int,
    val className: String,
    val description: String,
    val subjects: List<RawSubject>
)

@JsonClass(generateAdapter = true)
data class RawUnit(
    val subjectId: String,
    val id: String,
    val name: String
)

@JsonClass(generateAdapter = true)
data class RawLesson(
    val unitId: String,
    val id: String,
    val name: String
)

