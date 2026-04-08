package com.example.festivalappmobile.api

import com.example.festivalappmobile.data.remote.ApiService
import com.example.festivalappmobile.data.remote.dto.EditeurCreateRequestDto
import com.example.festivalappmobile.data.remote.dto.EditeurUpdateRequestDto
import com.example.festivalappmobile.data.repository.EditeurRepositoryImpl
import com.example.festivalappmobile.domain.models.Editeur
import com.example.festivalappmobile.domain.repository.EditeurRepository
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class EditeurApiTest {

    private lateinit var repository: EditeurRepository
    private lateinit var testApiService: ApiService

    @Before
    fun setup() {
        // Created a custom ApiService for testing, without the HttpLoggingInterceptor noise of the main one
        val cleanOkHttpClient = OkHttpClient.Builder().build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api-festival-app.ferhatsn.fr/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(cleanOkHttpClient)
            .build()
        
        testApiService = retrofit.create(ApiService::class.java)
        repository = EditeurRepositoryImpl(testApiService)
    }

    @Test
    fun testEditeurWorkflow() = runBlocking {
        println("\n===== STARTING EDITEUR WORKFLOW TEST =====")

        // 1. Create an Editeur
        println("\n[1/5] STEP: Creating a new Editeur...")
        val createRequest = EditeurCreateRequestDto(
            libelle = "Test Workflow Editeur",
            exposant = true,
            distributeur = true,
            phone = "0600000000",
            email = "workflow@test.com",
            notes = "Initial notes"
        )
        val createdEditeur = repository.createEditeur(createRequest)
        
        assertNotNull("FAILED: Repository should have returned the created editeur", createdEditeur)
        println("OUTCOME: SUCCESS - Editeur created successfully.")
        println("DATA:\n${formatEditeur(createdEditeur!!)}")

        val id = createdEditeur.id

        // 2. Fetch by ID to verify
        println("\n[2/5] STEP: Fetching Editeur by ID: $id...")
        val fetchedEditeur = repository.getEditeurById(id)
        
        assertNotNull("FAILED: Could not fetch editeur with ID $id", fetchedEditeur)
        assertEquals("EXPECTED: Libelle match", createRequest.libelle, fetchedEditeur?.libelle)
        println("OUTCOME: SUCCESS - Fetched data matches expectation.")
        println("DATA:\n${formatEditeur(fetchedEditeur!!)}")

        // 3. Get all editeurs and print count
        println("\n[3/5] STEP: Fetching all Editeurs...")
        val allEditeurs = repository.getAllEditeurs()
        println("OUTCOME: SUCCESS - Total Editeurs count: ${allEditeurs.size}")

        // 4. Update the editeur
        println("\n[4/5] STEP: Updating Editeur ID: $id...")
        val updateRequest = EditeurUpdateRequestDto(
            phone = "0700000000",
            notes = "Updated notes during workflow"
        )
        val updatedEditeur = repository.updateEditeur(id, updateRequest)
        
        assertNotNull("FAILED: Update operation returned null", updatedEditeur)
        assertEquals("EXPECTED: Updated phone match", updateRequest.phone, updatedEditeur?.phone)
        assertEquals("EXPECTED: Updated notes match", updateRequest.notes, updatedEditeur?.notes)
        println("OUTCOME: SUCCESS - Editeur updated correctly.")
        
        // Verify again with GetEditeurById
        val verifiedEditeur = repository.getEditeurById(id)
        assertNotNull("FAILED: Verification fetch failed", verifiedEditeur)
        println("VERIFICATION DATA:\n${formatEditeur(verifiedEditeur!!)}")

        // 5. Delete the editeur and check deletion
        println("\n[5/5] STEP: Deleting Editeur ID: $id...")
        val deleteSuccess = repository.deleteEditeur(id)
        assertTrue("FAILED: Delete operation returned false", deleteSuccess)
        println("OUTCOME: SUCCESS - Delete request acknowledged.")

        // Verify it was deleted via API status code if possible, or repo null result
        println("\nVERIFYING DELETION: Attempting to fetch deleted Editeur ID $id...")
        
        // Use API service directly for this verification to show the error code as requested
        val deleteVerifyResponse = testApiService.getEditeurById(id)
        val repoResult = repository.getEditeurById(id)
        
        assertEquals("EXPECTED: Repo should return null for deleted item", null, repoResult)
        println("REPOSITORY STATUS: Result is NULL as expected.")
        println("SERVER RESPONSE: Status Code: ${deleteVerifyResponse.code()}, Successful: ${deleteVerifyResponse.isSuccessful}")
        
        if (deleteVerifyResponse.code() == 404) {
            println("OUTCOME: SUCCESS - Server confirmed deletion with 404 Not Found.")
        } else {
            println("OUTCOME: WARNING - Server responded with ${deleteVerifyResponse.code()} instead of expected 404.")
        }

        println("\n===== EDITEUR WORKFLOW TEST COMPLETED SUCCESSFULLY =====\n")
    }

    private fun formatEditeur(e: Editeur): String {
        return """
            ID: ${e.id}
            Libelle: ${e.libelle}
            Exposant: ${e.exposant}
            Distributeur: ${e.distributeur}
            Phone: ${e.phone ?: "N/A"}
            Email: ${e.email ?: "N/A"}
            Notes: ${e.notes ?: "N/A"}
            Workflow Status: ${e.workflowStatus ?: "N/A"}
            Has Reservation: ${e.hasReservation}
        """.trimIndent()
    }
}
