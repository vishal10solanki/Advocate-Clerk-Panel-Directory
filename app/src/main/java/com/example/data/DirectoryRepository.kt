package com.example.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.model.AdvocateEntity
import com.example.model.AdvocateWithClerk
import com.example.model.ClerkEntity
import com.example.model.ClerkWithAdvocates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class DirectoryRepository(
    private val context: Context,
    private val dao: DirectoryDao
) {
    val advocatesWithClerks: Flow<List<AdvocateWithClerk>> = dao.getAllAdvocatesWithClerks()
    val clerksWithAdvocates: Flow<List<ClerkWithAdvocates>> = dao.getAllClerksWithAdvocates()
    val allClerks: Flow<List<ClerkEntity>> = dao.getAllClerks()

    private val prefs = context.getSharedPreferences("directory_cache_prefs", Context.MODE_PRIVATE)

    companion object {
        const val PREF_DATASET_CHECKSUM = "cached_seed_checksum"
        const val PREF_LAST_REFRESH_TIME = "last_refresh_timestamp"
        const val DATASET_VERSION = "2026.10.17.1365"
    }

    /**
     * Calculates MD5 checksum of the embedded directory_seed.csv asset for cache-busting.
     */
    fun getSeedChecksum(): String {
        return try {
            val inputStream = context.assets.open("directory_seed.csv")
            val md = java.security.MessageDigest.getInstance("MD5")
            val buffer = ByteArray(8192)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                md.update(buffer, 0, read)
            }
            inputStream.close()
            md.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "default_seed_checksum"
        }
    }

    /**
     * Checks if a new published version of directory_seed.csv is available
     * or if this is the initial launch.
     */
    suspend fun checkIfUpdateAvailableOrFirstLaunch(): Boolean = withContext(Dispatchers.IO) {
        try {
            val count = dao.getAdvocateCount()
            if (count == 0) return@withContext true
            val storedChecksum = prefs.getString(PREF_DATASET_CHECKSUM, null)
            val currentChecksum = getSeedChecksum()
            return@withContext storedChecksum == null || storedChecksum != currentChecksum
        } catch (e: Exception) {
            false
        }
    }

    suspend fun preloadIfNeeded() = withContext(Dispatchers.IO) {
        try {
            val count = dao.getAdvocateCount()
            val storedChecksum = prefs.getString(PREF_DATASET_CHECKSUM, null)
            val currentChecksum = getSeedChecksum()
            if (count == 0 || storedChecksum == null || storedChecksum != currentChecksum) {
                resetToSeedData()
            }
        } catch (e: Exception) {
            android.util.Log.e("DirectoryRepository", "Error during preload", e)
        }
    }

    suspend fun resetToSeedData(): CsvImportResult = withContext(Dispatchers.IO) {
        dao.clearAdvocates()
        dao.clearClerks()
        val result = seedFromAssets()
        val checksum = getSeedChecksum()
        prefs.edit()
            .putString(PREF_DATASET_CHECKSUM, checksum)
            .putLong(PREF_LAST_REFRESH_TIME, System.currentTimeMillis())
            .apply()
        result
    }

    /**
     * Force refresh data and bust cache:
     * Clears all local records and re-reads fresh CSV data directly from embedded asset.
     */
    suspend fun forceRefreshData(): CsvImportResult = withContext(Dispatchers.IO) {
        resetToSeedData()
    }

    fun getLastRefreshTime(): Long {
        return prefs.getLong(PREF_LAST_REFRESH_TIME, 0L)
    }

    private suspend fun seedFromAssets(): CsvImportResult {
        val assetManager = context.assets
        val inputStream = assetManager.open("directory_seed.csv")
        return importCsvFromStream(inputStream, overwrite = true)
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        for (ch in line) {
            when {
                ch == '\"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    result.add(current.toString().trim())
                    current.clear()
                }
                else -> current.append(ch)
            }
        }
        result.add(current.toString().trim())
        return result
    }

    suspend fun addAdvocate(
        name: String,
        code: String,
        primaryMobile: String,
        secondaryMobile: String,
        panelAdvocate: String = "",
        clerkId: Long?,
        additionalClerkIds: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val advocate = AdvocateEntity(
            name = name.trim(),
            code = code.trim(),
            primaryMobile = primaryMobile.trim(),
            secondaryMobile = secondaryMobile.trim(),
            panelAdvocate = panelAdvocate.trim(),
            clerkId = clerkId,
            additionalClerkIds = additionalClerkIds
        )
        dao.insertAdvocate(advocate)
    }

    suspend fun updateAdvocate(advocate: AdvocateEntity) = withContext(Dispatchers.IO) {
        dao.updateAdvocate(advocate)
    }

    suspend fun deleteAdvocate(advocateId: Long) = withContext(Dispatchers.IO) {
        dao.deleteAdvocateById(advocateId)
    }

    suspend fun addClerk(
        name: String,
        primaryMobile: String,
        secondaryMobile: String
    ): Long = withContext(Dispatchers.IO) {
        val clerk = ClerkEntity(
            name = name.trim(),
            primaryMobile = primaryMobile.trim(),
            secondaryMobile = secondaryMobile.trim()
        )
        dao.insertClerk(clerk)
    }

    suspend fun updateClerk(clerk: ClerkEntity) = withContext(Dispatchers.IO) {
        dao.updateClerk(clerk)
    }

    suspend fun deleteClerk(clerk: ClerkEntity) = withContext(Dispatchers.IO) {
        dao.deleteClerk(clerk)
    }

    suspend fun replaceAdvocateClerk(advocateId: Long, newClerkId: Long?) = withContext(Dispatchers.IO) {
        dao.updateAdvocateClerk(advocateId, newClerkId)
    }

    suspend fun assignAdvocatesToClerk(advocateIds: List<Long>, clerkId: Long) = withContext(Dispatchers.IO) {
        dao.assignAdvocatesToClerk(advocateIds, clerkId)
    }

    suspend fun exportDirectoryCsv(): String = withContext(Dispatchers.IO) {
        val list = dao.getAllAdvocatesWithClerks().first()
        val sb = StringBuilder()
        sb.append("Sr No,Advocate Name,Advocate Code,Panel Advocate,Advocate Primary Mobile,Advocate Secondary Mobile,Clerk Name,Clerk Primary Mobile,Clerk Secondary Mobile\n")
        list.forEachIndexed { index, item ->
            val adv = item.advocate
            val clerk = item.clerk
            val sr = adv.initialSrNo ?: (index + 1)
            sb.append(escapeCsv(sr.toString())).append(",")
            sb.append(escapeCsv(adv.name)).append(",")
            sb.append(escapeCsv(adv.code)).append(",")
            sb.append(escapeCsv(adv.panelAdvocate)).append(",")
            sb.append(escapeCsv(adv.primaryMobile)).append(",")
            sb.append(escapeCsv(adv.secondaryMobile)).append(",")
            sb.append(escapeCsv(clerk?.name ?: "")).append(",")
            sb.append(escapeCsv(clerk?.primaryMobile ?: "")).append(",")
            sb.append(escapeCsv(clerk?.secondaryMobile ?: "")).append("\n")
        }
        sb.toString()
    }

    suspend fun createExportCsvUri(): Uri = withContext(Dispatchers.IO) {
        val csvContent = exportDirectoryCsv()
        val cacheDir = File(context.cacheDir, "exports")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val file = File(cacheDir, "advocate_directory_export.csv")
        file.writeText(csvContent, Charsets.UTF_8)
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    suspend fun importCsvFromUri(uri: Uri, overwrite: Boolean = false): CsvImportResult = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext CsvImportResult(success = false, errorMessage = "Could not open selected file")
            importCsvFromStream(inputStream, overwrite)
        } catch (e: Exception) {
            android.util.Log.e("DirectoryRepository", "CSV import failed", e)
            CsvImportResult(success = false, errorMessage = e.message ?: "Failed to import CSV file")
        }
    }

    suspend fun importCsvFromText(csvText: String, overwrite: Boolean = false): CsvImportResult = withContext(Dispatchers.IO) {
        try {
            val stream = java.io.ByteArrayInputStream(csvText.toByteArray(Charsets.UTF_8))
            importCsvFromStream(stream, overwrite)
        } catch (e: Exception) {
            android.util.Log.e("DirectoryRepository", "CSV text import failed", e)
            CsvImportResult(success = false, errorMessage = e.message ?: "Failed to import CSV text")
        }
    }

    suspend fun importCsvFromStream(inputStream: java.io.InputStream, overwrite: Boolean = false): CsvImportResult = withContext(Dispatchers.IO) {
        try {
            val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
            val lines = mutableListOf<String>()
            reader.use { r ->
                var l = r.readLine()
                val currentLine = StringBuilder()
                while (l != null) {
                    currentLine.append(l)
                    val quoteCount = currentLine.count { it == '\"' }
                    if (quoteCount % 2 == 0) {
                        lines.add(currentLine.toString())
                        currentLine.clear()
                    } else {
                        currentLine.append("\n")
                    }
                    l = r.readLine()
                }
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                }
            }

            if (lines.isEmpty()) {
                return@withContext CsvImportResult(success = false, errorMessage = "CSV file is empty")
            }

            // Detect headers
            val firstLine = lines.first()
            val headerParts = parseCsvLine(firstLine).map { it.trim().lowercase() }
            val isHeader = headerParts.any { 
                it.contains("name") || it.contains("code") || it.contains("clerk") || it.contains("mobile") || it.contains("sr")
            }

            val dataLines = if (isHeader) lines.drop(1) else lines

            var colSr = -1
            var colAdvName = -1
            var colAdvCode = -1
            var colPanel = -1
            var colAdvMob1 = -1
            var colAdvMob2 = -1
            var colClerkName = -1
            var colClerkMob1 = -1
            var colClerkMob2 = -1

            if (isHeader) {
                headerParts.forEachIndexed { idx, h ->
                    when {
                        h.contains("panel") -> colPanel = idx
                        h.contains("clerk") && (h.contains("mob") || h.contains("phone") || h.contains("contact")) -> {
                            if (h.contains("sec") || h.contains("2") || h.contains("other") || h.contains("alt")) {
                                colClerkMob2 = idx
                            } else {
                                colClerkMob1 = idx
                            }
                        }
                        h.contains("clerk") -> colClerkName = idx
                        // Check secondary mobile before primary mobile so advocate_mobile_secondary is not captured as primary
                        (h.contains("sec") || h.contains("2") || h.contains("other") || h.contains("alt")) && 
                                (h.contains("mob") || h.contains("phone") || h.contains("contact")) -> colAdvMob2 = idx
                        (h.contains("pri") || h.contains("1") || h.contains("adv")) && 
                                (h.contains("mob") || h.contains("phone") || h.contains("contact")) -> colAdvMob1 = idx
                        (h.contains("mob") || h.contains("phone") || h.contains("contact")) -> {
                            if (colAdvMob1 == -1) colAdvMob1 = idx else if (colAdvMob2 == -1) colAdvMob2 = idx
                        }
                        h.contains("code") -> colAdvCode = idx
                        h.contains("adv") && h.contains("name") -> colAdvName = idx
                        (h == "name" || h.contains("advocate")) && colAdvName == -1 -> colAdvName = idx
                        h.contains("sr") || h == "no" || h == "id" -> colSr = idx
                    }
                }
            }

            // Fallback column positions
            val sampleParts = parseCsvLine(dataLines.firstOrNull() ?: "")
            if (colAdvName == -1) colAdvName = 1
            if (colAdvCode == -1) colAdvCode = 2
            if (sampleParts.size >= 9 && sampleParts.getOrNull(3)?.contains("panel", ignoreCase = true) == true) {
                if (colPanel == -1) colPanel = 3
                if (colAdvMob1 == -1) colAdvMob1 = 4
                if (colAdvMob2 == -1) colAdvMob2 = 5
                if (colClerkName == -1) colClerkName = 6
                if (colClerkMob1 == -1) colClerkMob1 = 7
                if (colClerkMob2 == -1) colClerkMob2 = 8
            } else {
                if (colAdvMob1 == -1) colAdvMob1 = 3
                if (colAdvMob2 == -1) colAdvMob2 = 4
                if (colClerkName == -1) colClerkName = 5
                if (colClerkMob1 == -1) colClerkMob1 = 6
                if (colClerkMob2 == -1) colClerkMob2 = 7
                if (colPanel == -1 && sampleParts.size >= 9) colPanel = 8
            }
            if (colSr == -1) colSr = 0

            data class ParsedRow(
                val sr: Int?,
                val advName: String,
                val advCode: String,
                val panel: String,
                val advMob1: String,
                val advMob2: String,
                val clerkName: String,
                val clerkMob1: String,
                val clerkMob2: String
            )

            val parsedRows = mutableListOf<ParsedRow>()
            for (line in dataLines) {
                val trimmed = line.trim()
                if (trimmed.isEmpty()) continue
                val parts = parseCsvLine(trimmed)
                fun getCol(idx: Int): String = if (idx in parts.indices) parts[idx].trim() else ""

                val advName = getCol(colAdvName)
                if (advName.isBlank() && getCol(colAdvCode).isBlank()) continue

                val sr = if (colSr in parts.indices) parts[colSr].toIntOrNull() else null
                val advCode = getCol(colAdvCode)
                val panel = getCol(colPanel)
                val advMob1 = getCol(colAdvMob1)
                val advMob2 = getCol(colAdvMob2)
                val clerkName = getCol(colClerkName)
                val clerkMob1 = getCol(colClerkMob1)
                val clerkMob2 = getCol(colClerkMob2)

                parsedRows.add(
                    ParsedRow(
                        sr = sr,
                        advName = advName,
                        advCode = advCode,
                        panel = panel,
                        advMob1 = advMob1,
                        advMob2 = advMob2,
                        clerkName = clerkName,
                        clerkMob1 = clerkMob1,
                        clerkMob2 = clerkMob2
                    )
                )
            }

            if (parsedRows.isEmpty()) {
                return@withContext CsvImportResult(success = false, errorMessage = "No valid records found in CSV file")
            }

            if (overwrite) {
                dao.clearAdvocates()
                dao.clearClerks()

                val clerkMap = mutableMapOf<String, ClerkEntity>()
                for (row in parsedRows) {
                    if (row.clerkName.isNotBlank()) {
                        val key = row.clerkName.lowercase()
                        val existing = clerkMap[key]
                        if (existing == null) {
                            clerkMap[key] = ClerkEntity(
                                name = row.clerkName,
                                primaryMobile = row.clerkMob1,
                                secondaryMobile = row.clerkMob2
                            )
                        } else {
                            val u1 = existing.primaryMobile.ifBlank { row.clerkMob1 }
                            val u2 = existing.secondaryMobile.ifBlank { row.clerkMob2 }
                            clerkMap[key] = existing.copy(primaryMobile = u1, secondaryMobile = u2)
                        }
                    }
                }

                val clerkList = clerkMap.values.toList()
                val clerkKeys = clerkMap.keys.toList()
                val insertedClerkIds = dao.insertClerks(clerkList)
                val clerkIdMap = mutableMapOf<String, Long>()
                for (i in clerkKeys.indices) {
                    clerkIdMap[clerkKeys[i]] = insertedClerkIds[i]
                }

                val advocatesToInsert = parsedRows.map { row ->
                    val clerkId = if (row.clerkName.isNotBlank()) clerkIdMap[row.clerkName.lowercase()] else null
                    AdvocateEntity(
                        name = row.advName,
                        code = row.advCode,
                        primaryMobile = row.advMob1,
                        secondaryMobile = row.advMob2,
                        panelAdvocate = row.panel,
                        clerkId = clerkId,
                        initialSrNo = row.sr
                    )
                }

                dao.insertAdvocates(advocatesToInsert)
                return@withContext CsvImportResult(
                    success = true,
                    totalProcessed = parsedRows.size,
                    insertedAdvocates = advocatesToInsert.size,
                    updatedAdvocates = 0,
                    newClerks = clerkList.size
                )
            } else {
                val existingClerks = dao.getAllClerksSnapshot().toMutableList()
                val clerkMapByName = existingClerks.associateBy { it.name.trim().lowercase() }.toMutableMap()
                var newClerkCount = 0

                for (row in parsedRows) {
                    if (row.clerkName.isNotBlank()) {
                        val key = row.clerkName.trim().lowercase()
                        val existing = clerkMapByName[key]
                        if (existing == null) {
                            val newClerk = ClerkEntity(
                                name = row.clerkName.trim(),
                                primaryMobile = row.clerkMob1.trim(),
                                secondaryMobile = row.clerkMob2.trim()
                            )
                            val newId = dao.insertClerk(newClerk)
                            val insertedClerk = newClerk.copy(id = newId)
                            clerkMapByName[key] = insertedClerk
                            newClerkCount++
                        } else {
                            val needsMob1Update = existing.primaryMobile.isBlank() && row.clerkMob1.isNotBlank()
                            val needsMob2Update = existing.secondaryMobile.isBlank() && row.clerkMob2.isNotBlank()
                            if (needsMob1Update || needsMob2Update) {
                                val updated = existing.copy(
                                    primaryMobile = if (needsMob1Update) row.clerkMob1.trim() else existing.primaryMobile,
                                    secondaryMobile = if (needsMob2Update) row.clerkMob2.trim() else existing.secondaryMobile
                                )
                                dao.updateClerk(updated)
                                clerkMapByName[key] = updated
                            }
                        }
                    }
                }

                val existingAdvocates = dao.getAllAdvocatesSnapshot()
                val advocatesByCode = existingAdvocates.filter { it.code.isNotBlank() }
                    .associateBy { it.code.trim().lowercase() }.toMutableMap()
                val advocatesByName = existingAdvocates.associateBy { it.name.trim().lowercase() }.toMutableMap()

                var insertedAdvCount = 0
                var updatedAdvCount = 0

                for (row in parsedRows) {
                    val codeKey = row.advCode.trim().lowercase()
                    val nameKey = row.advName.trim().lowercase()
                    val matchedExisting = (if (codeKey.isNotEmpty()) advocatesByCode[codeKey] else null)
                        ?: advocatesByName[nameKey]

                    val clerkId = if (row.clerkName.isNotBlank()) {
                        clerkMapByName[row.clerkName.trim().lowercase()]?.id
                    } else null

                    if (matchedExisting != null) {
                        val updated = matchedExisting.copy(
                            name = if (row.advName.isNotBlank()) row.advName.trim() else matchedExisting.name,
                            code = if (row.advCode.isNotBlank()) row.advCode.trim() else matchedExisting.code,
                            primaryMobile = if (row.advMob1.isNotBlank()) row.advMob1.trim() else matchedExisting.primaryMobile,
                            secondaryMobile = if (row.advMob2.isNotBlank()) row.advMob2.trim() else matchedExisting.secondaryMobile,
                            panelAdvocate = if (row.panel.isNotBlank()) row.panel.trim() else matchedExisting.panelAdvocate,
                            clerkId = clerkId ?: matchedExisting.clerkId
                        )
                        dao.updateAdvocate(updated)
                        if (updated.code.isNotBlank()) {
                            advocatesByCode[updated.code.trim().lowercase()] = updated
                        }
                        advocatesByName[updated.name.trim().lowercase()] = updated
                        updatedAdvCount++
                    } else {
                        val newAdv = AdvocateEntity(
                            name = row.advName.trim(),
                            code = row.advCode.trim(),
                            primaryMobile = row.advMob1.trim(),
                            secondaryMobile = row.advMob2.trim(),
                            panelAdvocate = row.panel.trim(),
                            clerkId = clerkId,
                            initialSrNo = row.sr
                        )
                        val newId = dao.insertAdvocate(newAdv)
                        val insertedAdv = newAdv.copy(id = newId)
                        if (insertedAdv.code.isNotBlank()) {
                            advocatesByCode[insertedAdv.code.trim().lowercase()] = insertedAdv
                        }
                        advocatesByName[insertedAdv.name.trim().lowercase()] = insertedAdv
                        insertedAdvCount++
                    }
                }

                return@withContext CsvImportResult(
                    success = true,
                    totalProcessed = parsedRows.size,
                    insertedAdvocates = insertedAdvCount,
                    updatedAdvocates = updatedAdvCount,
                    newClerks = newClerkCount
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("DirectoryRepository", "Error parsing CSV stream", e)
            CsvImportResult(success = false, errorMessage = e.message ?: "Failed to parse CSV file")
        }
    }
}

data class CsvImportResult(
    val success: Boolean,
    val totalProcessed: Int = 0,
    val insertedAdvocates: Int = 0,
    val updatedAdvocates: Int = 0,
    val newClerks: Int = 0,
    val errorMessage: String? = null
)

