package com.example.festivalappmobile.api

import com.example.festivalappmobile.data.remote.ApiService
import com.example.festivalappmobile.data.remote.dto.FestivalCreateRequestDto
import com.example.festivalappmobile.data.remote.dto.FestivalUpdateRequestDto
import com.example.festivalappmobile.data.repository.FestivalRepositoryImpl
import com.example.festivalappmobile.domain.models.Festival
import com.example.festivalappmobile.domain.repository.FestivalRepository
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FestivalApiTest {

    private lateinit var repository: FestivalRepository
    private lateinit var testApiService: ApiService

    @Before
    fun setup() {
        val cleanOkHttpClient = OkHttpClient.Builder().build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api-festival-app.ferhatsn.fr/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(cleanOkHttpClient)
            .build()
        
        testApiService = retrofit.create(ApiService::class.java)
        repository = FestivalRepositoryImpl(testApiService)
    }

    @Test
    fun testFestivalWorkflow() = runBlocking {
        println("\n===== STARTING FESTIVAL WORKFLOW TEST =====")

        // 1. Create a Festival
        println("\n[1/5] STEP: Creating a new Festival...")
        val createRequest = FestivalCreateRequestDto(
            nom = "Workflow Test Festival",
            lieu = "Test City",
            dateDebut = "2026-07-01T10:00:00.000Z",
            dateFin = "2026-07-03T18:00:00.000Z",
            nbTotalTable = 50,
            nbTotalChaise = 200,
            bigTables = 10,
            bigChairs = 40,
            smallTables = 40,
            smallChairs = 160,
            mairieTables = 5,
            mairieChairs = 20
        )
        val createdFestival = repository.createFestival(createRequest)
        
        assertNotNull("FAILED: Repository should have returned the created festival", createdFestival)
        println("OUTCOME: SUCCESS - Festival created successfully.")
        println("DATA:\n${formatFestival(createdFestival!!)}")

        val id = createdFestival.id

        // 2. Fetch by ID to verify
        println("\n[2/5] STEP: Fetching Festival by ID: $id...")
        val fetchedFestival = repository.getFestivalById(id)
        
        assertNotNull("FAILED: Could not fetch festival with ID $id", fetchedFestival)
        assertEquals("EXPECTED: Nom match", createRequest.nom, fetchedFestival?.nom)
        println("OUTCOME: SUCCESS - Fetched data matches expectation.")
        println("DATA:\n${formatFestival(fetchedFestival!!)}")

        // 3. Get all festivals and print count
        println("\n[3/5] STEP: Fetching all Festivals...")
        val allFestivals = repository.getFestivals()
        println("OUTCOME: SUCCESS - Total Festivals count: ${allFestivals.size}")

        // 4. Update the festival
        println("\n[4/5] STEP: Updating Festival ID: $id...")
        val updateRequest = FestivalUpdateRequestDto(
            lieu = "Updated Test City",
            nbTotalTable = 60
        )
        val updatedFestival = repository.updateFestival(id, updateRequest)
        
        assertNotNull("FAILED: Update operation returned null", updatedFestival)
        assertEquals("EXPECTED: Updated lieu match", updateRequest.lieu, updatedFestival?.lieu)
        assertEquals("EXPECTED: Updated tables match", updateRequest.nbTotalTable, updatedFestival?.nbTotalTable)
        println("OUTCOME: SUCCESS - Festival updated correctly.")
        
        // Verify again with GetFestivalById
        val verifiedFestival = repository.getFestivalById(id)
        assertNotNull("FAILED: Verification fetch failed", verifiedFestival)
        println("VERIFICATION DATA:\n${formatFestival(verifiedFestival!!)}")

        // 5. Delete the festival and check deletion
        println("\n[5/5] STEP: Deleting Festival ID: $id...")
        val deleteSuccess = repository.deleteFestival(id)
        assertTrue("FAILED: Delete operation returned false", deleteSuccess)
        println("OUTCOME: SUCCESS - Delete request acknowledged.")

        // Verify it was deleted
        println("\nVERIFYING DELETION: Attempting to fetch deleted Festival ID $id...")
        val repoResult = repository.getFestivalById(id)
        
        assertEquals("EXPECTED: Repo should return null for deleted item", null, repoResult)
        println("REPOSITORY STATUS: Result is NULL as expected.")
        
        val apiResponse = testApiService.getFestivalById(id)
        println("SERVER RESPONSE: Status Code: ${apiResponse.code()}, Successful: ${apiResponse.isSuccessful}")
        
        if (apiResponse.code() == 404) {
            println("OUTCOME: SUCCESS - Server confirmed deletion with 404 Not Found.")
        } else {
            println("OUTCOME: WARNING - Server responded with ${apiResponse.code()} instead of expected 404.")
        }

        println("\n===== FESTIVAL WORKFLOW TEST COMPLETED SUCCESSFULLY =====\n")
    }

    private fun formatFestival(f: Festival): String {
        return """
            ID: ${f.id}
            Nom: ${f.nom}
            Lieu: ${f.lieu}
            Dates: ${f.dateDebut} to ${f.dateFin}
            Tables/Chairs: ${f.nbTotalTable}/${f.nbTotalChaise}
            Big T/C: ${f.bigTables}/${f.bigChairs}
            Small T/C: ${f.smallTables}/${f.smallChairs}
            Mairie T/C: ${f.mairieTables}/${f.mairieChairs}
        """.trimIndent()
    }
}
