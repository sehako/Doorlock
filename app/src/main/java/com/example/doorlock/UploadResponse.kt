package com.example.doorlock

data class UploadResponse(
    val error: Boolean,
    val message: String,
    val image: String
)