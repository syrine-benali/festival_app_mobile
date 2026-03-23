package com.example.festivalappmobile.domain.models

// dto used throughout the app for festival
data class Festival(
    val id: Int,
    val nom: String,
    val lieu: String,
    val dateDebut: String,
    val dateFin: String,
    val nbTotalTable: Int,
    val nbTotalChaise: Int,
    val bigTables: Int,
    val bigChairs: Int,
    val smallTables: Int,
    val smallChairs: Int,
    val mairieTables: Int,
    val mairieChairs: Int
)
