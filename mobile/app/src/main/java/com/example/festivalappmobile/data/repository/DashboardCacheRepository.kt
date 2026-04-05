package com.example.festivalappmobile.data.repository

import com.example.festivalappmobile.data.local.dao.DashboardEditeurDao
import com.example.festivalappmobile.data.local.dao.DashboardFestivalDao
import com.example.festivalappmobile.data.local.dao.DashboardJeuDao
import com.example.festivalappmobile.data.local.entity.DashboardEditeurEntity
import com.example.festivalappmobile.data.local.entity.DashboardFestivalEntity
import com.example.festivalappmobile.data.local.entity.DashboardJeuEntity
import com.example.festivalappmobile.domain.models.Editeur
import com.example.festivalappmobile.domain.models.Festival
import com.example.festivalappmobile.domain.models.Jeu
import com.example.festivalappmobile.domain.models.WorkflowStatus

/**
 * Repository de cache pour le tableau de bord.
 * Sauvegarde les données filtrées lors du dernier accès en ligne,
 * et les restitue en mode hors ligne (sans images/logos).
 */
class DashboardCacheRepository(
    private val festivalDao: DashboardFestivalDao,
    private val jeuDao: DashboardJeuDao,
    private val editeurDao: DashboardEditeurDao
) {

    // ── Festivals ──────────────────────────────────────────────────────────────

    suspend fun saveFestivals(festivals: List<Festival>) {
        festivalDao.clear()
        festivalDao.insertAll(festivals.map { it.toEntity() })
    }

    suspend fun getCachedFestivals(): List<Festival> =
        festivalDao.getAll().map { it.toDomain() }

    // ── Jeux ───────────────────────────────────────────────────────────────────

    suspend fun saveJeux(jeux: List<Jeu>) {
        jeuDao.clear()
        jeuDao.insertAll(jeux.map { it.toEntity() })
    }

    suspend fun getCachedJeux(): List<Jeu> =
        jeuDao.getAll().map { it.toDomain() }

    // ── Éditeurs ───────────────────────────────────────────────────────────────

    suspend fun saveEditeurs(editeurs: List<Editeur>) {
        editeurDao.clear()
        editeurDao.insertAll(editeurs.map { it.toEntity() })
    }

    suspend fun getCachedEditeurs(): List<Editeur> =
        editeurDao.getAll().map { it.toDomain() }
}

// ── Mappers Festival ──────────────────────────────────────────────────────────

private fun Festival.toEntity() = DashboardFestivalEntity(
    id = id, nom = nom, lieu = lieu,
    dateDebut = dateDebut, dateFin = dateFin,
    nbTotalTable = nbTotalTable, nbTotalChaise = nbTotalChaise,
    bigTables = bigTables, bigChairs = bigChairs,
    smallTables = smallTables, smallChairs = smallChairs,
    mairieTables = mairieTables, mairieChairs = mairieChairs
)

private fun DashboardFestivalEntity.toDomain() = Festival(
    id = id, nom = nom, lieu = lieu,
    dateDebut = dateDebut, dateFin = dateFin,
    nbTotalTable = nbTotalTable, nbTotalChaise = nbTotalChaise,
    bigTables = bigTables, bigChairs = bigChairs,
    smallTables = smallTables, smallChairs = smallChairs,
    mairieTables = mairieTables, mairieChairs = mairieChairs
)

// ── Mappers Jeu ───────────────────────────────────────────────────────────────

private fun Jeu.toEntity() = DashboardJeuEntity(
    id = id, libelle = libelle, auteur = auteur,
    nbMinJoueur = nbMinJoueur, nbMaxJoueur = nbMaxJoueur,
    ageMin = ageMin, duree = duree, prototype = prototype,
    editeurId = editeurId, editeurNom = editeurNom,
    typeJeuId = typeJeuId, typeJeuNom = typeJeuNom
    // image non stockée — inaccessible sans connexion
)

private fun DashboardJeuEntity.toDomain() = Jeu(
    id = id, libelle = libelle, auteur = auteur,
    nbMinJoueur = nbMinJoueur, nbMaxJoueur = nbMaxJoueur,
    ageMin = ageMin, duree = duree, prototype = prototype,
    image = null, // pas d'image en mode hors ligne
    editeurId = editeurId, editeurNom = editeurNom,
    typeJeuId = typeJeuId, typeJeuNom = typeJeuNom
)

// ── Mappers Editeur ───────────────────────────────────────────────────────────

private fun Editeur.toEntity() = DashboardEditeurEntity(
    id = id, libelle = libelle,
    exposant = exposant, distributeur = distributeur,
    phone = phone, email = email, notes = notes,
    workflowStatus = workflowStatus?.name,
    hasReservation = hasReservation
    // logo non stocké — inaccessible sans connexion
)

private fun DashboardEditeurEntity.toDomain() = Editeur(
    id = id, libelle = libelle,
    exposant = exposant, distributeur = distributeur,
    logo = null, // pas de logo en mode hors ligne
    phone = phone, email = email, notes = notes,
    workflowStatus = workflowStatus?.let {
        try { WorkflowStatus.valueOf(it) } catch (e: Exception) { null }
    },
    hasReservation = hasReservation
)
