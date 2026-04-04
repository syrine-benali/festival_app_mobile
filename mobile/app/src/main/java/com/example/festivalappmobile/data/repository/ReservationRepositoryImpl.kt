package com.example.festivalappmobile.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.festivalappmobile.data.local.AppDatabase
import com.example.festivalappmobile.data.local.entity.toEntity
import com.example.festivalappmobile.data.local.entity.toDomain
import com.example.festivalappmobile.data.remote.ApiService
import com.example.festivalappmobile.data.remote.dto.*
import com.example.festivalappmobile.data.remote.mapper.toDomain
import com.example.festivalappmobile.domain.models.*
import com.example.festivalappmobile.domain.repository.ReservationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReservationRepositoryImpl(
    private val api: ApiService,
    private val db: AppDatabase,
    private val context: Context
) : ReservationRepository {

    private val dao = db.reservationDao()

    override fun isOnline(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: SecurityException) {
            false
        }
    }

    // Retourne le Flow Room (offline), et tente un refresh en arrière-plan si online
    override fun getReservations(festivalId: Int?): Flow<List<ReservationSummary>> {
        // Lance le refresh sans bloquer
        if (isOnline()) {
            // refresh asynchrone — le flow Room émettra automatiquement après insertion
        }
        val flow = if (festivalId != null)
            dao.getByFestival(festivalId)
        else
            dao.getAll()
        return flow.map { list -> list.map { it.toDomain() } }
    }

    // Refresh explicite appelé depuis le ViewModel
    suspend fun refreshReservations(festivalId: Int? = null) {
        if (!isOnline()) return
        try {
            val response = api.getReservations(festivalId)
            if (response.isSuccessful) {
                val list = response.body()?.data?.map { it.toDomain().toEntity() } ?: emptyList()
                dao.insertAll(list)
            }
        } catch (_: Exception) { /* silencieux : on garde le cache */ }
    }

    override suspend fun getReservationById(id: Int): Result<Reservation> {
        return try {
            if (isOnline()) {
                val response = api.getReservationById(id)
                if (response.isSuccessful && response.body()?.reservation != null) {
                    val res = response.body()!!.reservation!!.toDomain()
                    // Mettre à jour le cache
                    dao.insert(res.toSummary().toEntity())
                    Result.success(res)
                } else {
                    Result.failure(Exception("Réservation introuvable"))
                }
            } else {
                // Mode offline : retourne la version allégée du cache
                val cached = dao.getById(id)
                if (cached != null) {
                    // Reconstruit un Reservation partiel depuis le cache
                    Result.success(cached.toDomain().toPartialReservation())
                } else {
                    Result.failure(Exception("Données non disponibles hors-ligne"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createReservation(
        editeurId: Int, festivalId: Int, typeReservant: TypeReservant
    ): Result<Reservation> = safeApiCall {
        api.createReservation(CreateReservationRequestDto(editeurId, festivalId, typeReservant.name))
    }

    override suspend fun updateWorkflowStatus(id: Int, status: WorkflowStatus): Result<Reservation> =
        safeApiCall {
            api.updateReservation(id, UpdateReservationRequestDto(workflowStatus = status.name))
        }

    override suspend fun updateReservationFlags(
        id: Int,
        viendraPresenteSesJeux: Boolean?,
        nousPresentons: Boolean?,
        listeJeuxDemandee: Boolean?,
        listeJeuxObtenue: Boolean?,
        jeuxRecusPhysiquement: Boolean?,
        notesClient: String?,
        notesWorkflow: String?
    ): Result<Reservation> = safeApiCall {
        api.updateReservation(id, UpdateReservationRequestDto(
            viendraPresenteSesJeux = viendraPresenteSesJeux,
            nousPresentons = nousPresentons,
            listeJeuxDemandee = listeJeuxDemandee,
            listeJeuxObtenue = listeJeuxObtenue,
            jeuxRecusPhysiquement = jeuxRecusPhysiquement,
            notesClient = notesClient,
            notesWorkflow = notesWorkflow
        ))
    }

    override suspend fun addContact(
        reservationId: Int, dateContact: String, commentaire: String?
    ): Result<ReservationContact> {
        return try {
            val response = api.addContact(reservationId, AddContactRequestDto(dateContact, commentaire))
            if (response.isSuccessful) {
                val contacts = response.body()?.reservation?.reservationContacts
                val added = contacts?.lastOrNull()?.toDomain()
                    ?: return Result.failure(Exception("Contact non retourné"))
                Result.success(added)
            } else {
                Result.failure(Exception("Erreur ajout contact"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun deleteContact(contactId: Int): Result<Unit> {
        return try {
            api.deleteContact(contactId)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun addLine(
        reservationId: Int, zoneTarifaireId: Int,
        nbTables: Int, grandesTablesSouhaitees: Boolean
    ): Result<ReservationLine> {
        return try {
            val response = api.addLine(reservationId,
                AddLineRequestDto(zoneTarifaireId, nbTables, grandesTablesSouhaitees))
            if (response.isSuccessful) {
                val lines = response.body()?.reservation?.reservationLines
                val added = lines?.lastOrNull()?.toDomain()
                    ?: return Result.failure(Exception("Ligne non retournée"))
                Result.success(added)
            } else {
                Result.failure(Exception("Erreur ajout ligne"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun deleteLine(lineId: Int): Result<Unit> {
        return try {
            api.deleteLine(lineId)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun addJeu(
        reservationId: Int, jeuId: Int, nbExemplaires: Int,
        nbTablesAllouees: Int, zonePlanId: Int?
    ): Result<ReservationJeu> {
        return try {
            val response = api.addJeu(reservationId,
                AddJeuRequestDto(jeuId, nbExemplaires, nbTablesAllouees, zonePlanId))
            if (response.isSuccessful) {
                val jeux = response.body()?.reservation?.reservationJeux
                val added = jeux?.lastOrNull()?.toDomain()
                    ?: return Result.failure(Exception("Jeu non retourné"))
                Result.success(added)
            } else {
                Result.failure(Exception("Erreur ajout jeu"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun updateJeu(
        jeuId: Int, nbExemplaires: Int?, nbTablesAllouees: Int?, zonePlanId: Int?
    ): Result<ReservationJeu> = Result.failure(Exception("À implémenter"))

    override suspend fun deleteJeu(jeuId: Int): Result<Unit> {
        return try {
            api.deleteJeu(jeuId)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // Helper : appel API sécurisé qui retourne un Reservation
    private suspend fun safeApiCall(
        call: suspend () -> retrofit2.Response<ReservationResponseDto>
    ): Result<Reservation> {
        return try {
            val response = call()
            if (response.isSuccessful && response.body()?.reservation != null) {
                val res = response.body()!!.reservation!!.toDomain()
                dao.insert(res.toSummary().toEntity())
                Result.success(res)
            } else {
                Result.failure(Exception(response.message() ?: "Erreur serveur"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }
}

// Extensions helpers
fun Reservation.toSummary() = ReservationSummary(
    id = id, editeurId = editeurId, editeurLibelle = editeurLibelle,
    festivalId = festivalId, festivalNom = festivalNom,
    workflowStatus = workflowStatus, typeReservant = typeReservant,
    totalTables = totalTables, totalPrice = totalPrice
)

fun ReservationSummary.toPartialReservation() = Reservation(
    id = id, editeurId = editeurId, editeurLibelle = editeurLibelle,
    festivalId = festivalId, festivalNom = festivalNom,
    workflowStatus = workflowStatus, typeReservant = typeReservant,
    dateFacturation = null, viendraPresenteSesJeux = true, nousPresentons = false,
    listeJeuxDemandee = false, listeJeuxObtenue = false, jeuxRecusPhysiquement = false,
    notesClient = null, notesWorkflow = null, nbPrisesElectriques = 0,
    typeRemise = null, valeurRemise = 0.0,
    reservationLines = emptyList(), reservationContacts = emptyList(),
    reservationJeux = emptyList(),
    totalTables = totalTables, totalPrice = totalPrice
)