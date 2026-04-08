package com.example.festivalappmobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.festivalappmobile.domain.models.Festival

/**
 * Entité Room pour le cache local des festivals.
 * [lastUpdated] permet de connaître la fraîcheur du cache.
 */
@Entity(tableName = "festivals")
data class FestivalEntity(
    @PrimaryKey val id: Int,
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
    val mairieChairs: Int,
    val lastUpdated: Long = System.currentTimeMillis()
)

fun FestivalEntity.toDomain() = Festival(
    id = id,
    nom = nom,
    lieu = lieu,
    dateDebut = dateDebut,
    dateFin = dateFin,
    nbTotalTable = nbTotalTable,
    nbTotalChaise = nbTotalChaise,
    bigTables = bigTables,
    bigChairs = bigChairs,
    smallTables = smallTables,
    smallChairs = smallChairs,
    mairieTables = mairieTables,
    mairieChairs = mairieChairs
)

fun Festival.toEntity() = FestivalEntity(
    id = id,
    nom = nom,
    lieu = lieu,
    dateDebut = dateDebut,
    dateFin = dateFin,
    nbTotalTable = nbTotalTable,
    nbTotalChaise = nbTotalChaise,
    bigTables = bigTables,
    bigChairs = bigChairs,
    smallTables = smallTables,
    smallChairs = smallChairs,
    mairieTables = mairieTables,
    mairieChairs = mairieChairs
)
