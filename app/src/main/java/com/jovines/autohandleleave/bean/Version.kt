package com.jovines.autohandleleave.bean

data class Version(
    val applicationId: String,
    val artifactType: ArtifactType,
    val elements: List<Element>,
    val variantName: String,
    val version: Int
)

data class Element(
    val filters: List<Any>,
    val outputFile: String,
    val type: String,
    val versionCode: Int,
    val versionName: String
)

data class ArtifactType(
    val kind: String,
    val type: String
)
