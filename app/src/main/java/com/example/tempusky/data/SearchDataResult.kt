package com.example.tempusky.data

data class SearchDataResult(
    val user: String,
    val location: String,
    val temperature: Double?,
    val humidity: Double?,
    val pressure: Double?,
    val date: String
)
