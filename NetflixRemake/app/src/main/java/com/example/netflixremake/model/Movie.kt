package com.example.netflixremake.model

import androidx.annotation.DrawableRes

data class Movie(
    val id: String,
    val title: String,
    val poster: String,
    val plot: String = "",
    val actors: String = ""
)
