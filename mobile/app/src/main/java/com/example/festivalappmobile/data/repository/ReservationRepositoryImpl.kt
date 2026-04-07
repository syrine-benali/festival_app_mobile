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
import org.json.JSONObject

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
        editeurId: Int,
        festivalId: Int,
        typeReservant: TypeReservant,
        notesClient: String?
    ): Result<Reservation> = safeApiCall {
        api.createReservation(
            CreateReservationRequestDto(
                editeurId = editeurId,
                festivalId = festivalId,
                typeReservant = typeReservant.name,
                notesClient = notesClient
            )
        )
    }

    override suspend fun deleteReservation(id: Int): Result<Unit> {
        return try {
            val response = api.deleteReservation(id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Erreur suppression réservation"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateWorkflowStatus(id: Int, status: WorkflowStatus): Result<Reservation> =
        safeApiCall {
            api.updateReservation(id, UpdateReservationRequestDto(workflowStatus = status.name))
        }

    override suspend fun updateReservationFlags(
        id: Int,
        typeReservant: TypeReservant?,
        dateFacturation: String?,
        viendraPresenteSesJeux: Boolean?,
        nousPresentons: Boolean?,
        listeJeuxDemandee: Boolean?,
        listeJeuxObtenue: Boolean?,
        jeuxRecusPhysiquement: Boolean?,
        notesClient: String?,
        notesWorkflow: String?,
        typeRemise: TypeRemise?,
        valeurRemise: Double?,
        nbPrisesElectriques: Int?
    ): Result<Reservation> = safeApiCall {
        api.updateReservation(id, UpdateReservationRequestDto(
            typeReservant = typeReservant?.name,
            dateFacturation = dateFacturation,
            viendraPresenteSesJeux = viendraPresenteSesJeux,
            nousPresentons = nousPresentons,
            listeJeuxDemandee = listeJeuxDemandee,
            listeJeuxObtenue = listeJeuxObtenue,
            jeuxRecusPhysiquement = jeuxRecusPhysiquement,
            notesClient = notesClient,
            notesWorkflow = notesWorkflow,
            typeRemise = typeRemise?.name,
            valeurRemise = valeurRemise,
            nbPrisesElectriques = nbPrisesElectriques
        ))
    }

    override suspend fun addContact(
        reservationId: Int, dateContact: String, commentaire: String?
    ): Result<ReservationContact> {
        return try {
            val response = api.addContact(reservationId, AddContactRequestDto(dateContact, commentaire))
            if (response.isSuccessful && response.body()?.success == true) {
                val added = response.body()?.contact?.toDomain()
                    ?: return Result.failure(Exception("Contact non retourné par le serveur"))
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
        reservationId: Int,
        pricingId: Int,
        nbTables: Int, grandesTablesSouhaitees: Boolean
    ): Result<ReservationLine> {
        return try {
            val response = api.addLine(
                reservationId,
                AddLineRequestDto(
                    pricingId = pricingId,
                    nbTables = nbTables,
                    grandesTablesSouhaitees = grandesTablesSouhaitees
                )
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val added = response.body()?.line?.toDomain()
                    ?: return Result.failure(Exception("Ligne non retournée par le serveur"))
                Result.success(added)
            } else {
                val backendMessage = extractApiErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(backendMessage ?: "Erreur ajout ligne"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun updateLine(
        lineId: Int,
        nbTables: Int?,
        nbM2: Double?,
        grandesTablesSouhaitees: Boolean?
    ): Result<ReservationLine> {
        return try {
            val response = api.updateLine(
                lineId,
                UpdateLineRequestDto(
                    nbTables = nbTables,
                    nbM2 = nbM2,
                    grandesTablesSouhaitees = grandesTablesSouhaitees
                )
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val updated = response.body()?.line?.toDomain()
                    ?: return Result.failure(Exception("Ligne non retournée par le serveur"))
                Result.success(updated)
            } else {
                Result.failure(Exception("Erreur mise à jour ligne"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteLine(lineId: Int): Result<Unit> {
        return try {
            api.deleteLine(lineId)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun addJeu(
        reservationId: Int, jeuId: Int, nbExemplaires: Int,
        nbTablesAllouees: Int, placementId: Int?
    ): Result<ReservationJeu> {
        return try {
            val response = api.addJeu(reservationId,
                AddJeuRequestDto(jeuId, nbExemplaires, nbTablesAllouees, placementId))
            if (response.isSuccessful && response.body()?.success == true) {
                val added = response.body()?.jeu?.toDomain()
                    ?: return Result.failure(Exception("Jeu non retourné par le serveur"))
                Result.success(added)
            } else {
                Result.failure(Exception("Erreur ajout jeu"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun updateJeu(
        jeuId: Int, nbExemplaires: Int?, nbTablesAllouees: Int?, placementId: Int?
    ): Result<ReservationJeu> {
        return try {
            val response = api.updateJeu(
                jeuId,
                UpdateJeuRequestDto(
                    nbExemplaires = nbExemplaires,
                    nbTablesAllouees = nbTablesAllouees,
                    placementId = placementId
                )
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val updated = response.body()?.jeu?.toDomain()
                    ?: return Result.failure(Exception("Jeu non retourné par le serveur"))
                Result.success(updated)
            } else {
                Result.failure(Exception("Erreur mise à jour jeu"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteJeu(jeuId: Int): Result<Unit> {
        return try {
            val response = api.deleteJeu(jeuId)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Erreur suppression jeu"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun calculatePrice(reservationId: Int): Result<Double> {
        return try {
            val response = api.calculatePrice(reservationId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.totalGeneral)
            } else {
                Result.failure(Exception("Erreur calcul du prix"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractApiErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val json = JSONObject(errorBody)
            when {
                json.optString("message").isNotBlank() -> json.optString("message")
                json.optString("error").isNotBlank() -> json.optString("error")
                json.optString("msg").isNotBlank() -> json.optString("msg")
                else -> null
            }
        } catch (_: Exception) {
            null
        }
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