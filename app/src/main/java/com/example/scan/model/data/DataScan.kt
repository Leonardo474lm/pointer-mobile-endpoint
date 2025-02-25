package com.example.scan.model.data


data class ApiResponse(
    val result: Any?
)

data class ResultData(
    val verified: Boolean,
    val distance: Double,
    val threshold: Double,
    val model: String,
    val detector_backend: String,
    val similarity_metric: String,
    val facial_areas: FacialAreas,
    val time: Double
)

data class FacialAreas(
    val img1: FaceArea,
    val img2: FaceArea
)

data class FaceArea(
    val x: Int,
    val y: Int,
    val w: Int,
    val h: Int,
    val left_eye: List<Int>,
    val right_eye: List<Int>
)
