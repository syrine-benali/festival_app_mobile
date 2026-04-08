package com.example.festivalappmobile.data.remote.mapper

import com.example.festivalappmobile.data.remote.dto.FestivalDto
import com.example.festivalappmobile.domain.models.Festival

fun FestivalDto.toDomain(): Festival {
    return Festival(
        id = id,
        nom = nom,
        lieu = lieu,
        dateDebut = dateDebut,
        dateFin = dateFin,
        nbTotalTable = nbTotalTable,
        nbTotalChaise = nbTotalChaise,
        bigTables = bigTables ?: 0,
        bigChairs = bigChairs ?: 0,
        smallTables = smallTables ?: 0,
        smallChairs = smallChairs ?: 0,
        mairieTables = mairieTables ?: 0,
        mairieChairs = mairieChairs ?: 0
    )
}
