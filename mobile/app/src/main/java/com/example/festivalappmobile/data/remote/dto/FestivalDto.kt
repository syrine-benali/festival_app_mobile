package com.example.festivalappmobile.data.remote.dto

// Dto used to communicate with the API (matches the backend models)
data class FestivalDto(
    val id: Int,
    val nom: String,
    val lieu: String,
    val dateDebut: String,
    val dateFin: String,
    val nbTotalTable: Int,
    val nbTotalChaise: Int,
    val bigTables: Int? = 0,
    val bigChairs: Int? = 0,
    val smallTables: Int? = 0,
    val smallChairs: Int? = 0,
    val mairieTables: Int? = 0,
    val mairieChairs: Int? = 0
)

data class FestivalCreateRequestDto(
    val nom: String,
    val lieu: String,
    val dateDebut: String,
    val dateFin: String,
    val nbTotalTable: Int,
    val nbTotalChaise: Int,
    val bigTables: Int? = null,
    val bigChairs: Int? = null,
    val smallTables: Int? = null,
    val smallChairs: Int? = null,
    val mairieTables: Int? = null,
    val mairieChairs: Int? = null
)

data class FestivalUpdateRequestDto(
    val nom: String? = null,
    val lieu: String? = null,
    val dateDebut: String? = null,
    val dateFin: String? = null,
    val nbTotalTable: Int? = null,
    val nbTotalChaise: Int? = null,
    val bigTables: Int? = null,
    val bigChairs: Int? = null,
    val smallTables: Int? = null,
    val smallChairs: Int? = null,
    val mairieTables: Int? = null,
    val mairieChairs: Int? = null
)
