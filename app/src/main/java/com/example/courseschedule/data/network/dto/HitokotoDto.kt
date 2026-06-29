package com.example.courseschedule.data.network

data class HitokotoDto(
    val id: Int = 0,
    val hitokoto: String = "",
    val from: String = "",
    val from_who: String? = null,
    val creator: String = "",
    val type: String = ""
)
