package com.example.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.model.AdvocateWithClerk
import com.example.model.ClerkEntity
import com.example.model.ClerkWithAdvocates
import com.example.model.getAllClerkIds
import com.example.model.getPanelList
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

enum class DirectoryDocumentType {
    APPEARANCE_NOTE,
    FULL_DIRECTORY,
    PANEL_ADVOCATES,
    CLERKS_REGISTER,
    ADVOCATE_CODE_INDEX,
    MULTI_CLERK_ASSIGNMENTS,
    FILTERED_RESULTS,
    CUSTOM_PDF
}

data class DirectoryPdfDocument(
    val type: DirectoryDocumentType,
    val title: String,
    val description: String,
    val category: String,
    val fileName: String,
    val recordCount: Int,
    val estimatedPages: Int,
    val formatLabel: String = "PDF • A4",
    val metaSummary: String? = null,
    val id: String = type.name,
    val isCustom: Boolean = false
)

data class CustomPdfRecord(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val fileName: String,
    val sizeBytes: Long
)

data class PdfSaveResult(
    val uri: Uri,
    val displayLocation: String,
    val savedToPublicDownloads: Boolean
)

object PdfDirectoryGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN_LEFT = 32f
    private const val MARGIN_RIGHT = 563f
    private const val CONTENT_WIDTH = MARGIN_RIGHT - MARGIN_LEFT
    private const val FOOTER_Y = 816f
    private const val MAX_CONTENT_Y = 786f

    private const val ROWS_PER_PAGE_ESTIMATE = 26
    private const val PREFS_CUSTOM_PDFS = "custom_pdfs_prefs"
    private const val KEY_CUSTOM_LIST = "custom_pdf_records_json"
    private const val KEY_HIDDEN_BUILTINS = "hidden_builtin_pdf_ids"

    fun loadCustomPdfs(context: Context): List<CustomPdfRecord> {
        val prefs = context.getSharedPreferences(PREFS_CUSTOM_PDFS, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_CUSTOM_LIST, "[]") ?: "[]"
        val list = mutableListOf<CustomPdfRecord>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val id = obj.optString("id")
                val file = File(File(context.filesDir, "custom_pdfs"), "$id.pdf")
                if (id.isNotBlank() && file.exists()) {
                    list.add(
                        CustomPdfRecord(
                            id = id,
                            title = obj.optString("title", "Document"),
                            description = obj.optString("description", ""),
                            category = obj.optString("category", "Custom"),
                            fileName = obj.optString("fileName", "$id.pdf"),
                            sizeBytes = obj.optLong("sizeBytes", file.length())
                        )
                    )
                }
            }
        } catch (_: Exception) {
        }
        return list
    }

    fun getHiddenBuiltInIds(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_CUSTOM_PDFS, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_HIDDEN_BUILTINS, emptySet())?.toSet() ?: emptySet()
    }

    fun saveCustomPdf(
        context: Context,
        title: String,
        description: String,
        category: String,
        fileName: String,
        pdfBytes: ByteArray
    ): CustomPdfRecord {
        val id = "custom_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val safeFileName = fileName.trim().let { raw ->
            val base = if (raw.isBlank()) "document.pdf" else raw.replace(Regex("[^A-Za-z0-9._-]"), "_")
            if (base.lowercase().endsWith(".pdf")) base else "$base.pdf"
        }
        val dir = File(context.filesDir, "custom_pdfs")
        if (!dir.exists()) dir.mkdirs()
        val destFile = File(dir, "$id.pdf")
        destFile.writeBytes(pdfBytes)

        val record = CustomPdfRecord(
            id = id,
            title = title.trim(),
            description = description.trim().ifBlank { "Uploaded PDF document ($safeFileName)" },
            category = category.trim().ifBlank { "Custom" },
            fileName = safeFileName,
            sizeBytes = pdfBytes.size.toLong()
        )

        val existing = loadCustomPdfs(context).toMutableList()
        existing.add(0, record)
        persistCustomPdfs(context, existing)
        return record
    }

    fun deleteDocument(context: Context, doc: DirectoryPdfDocument) {
        if (doc.isCustom) {
            val file = File(File(context.filesDir, "custom_pdfs"), "${doc.id}.pdf")
            if (file.exists()) file.delete()
            val updated = loadCustomPdfs(context).filterNot { it.id == doc.id }
            persistCustomPdfs(context, updated)
        } else {
            val prefs = context.getSharedPreferences(PREFS_CUSTOM_PDFS, Context.MODE_PRIVATE)
            val current = prefs.getStringSet(KEY_HIDDEN_BUILTINS, emptySet())?.toMutableSet() ?: mutableSetOf()
            current.add(doc.id)
            prefs.edit().putStringSet(KEY_HIDDEN_BUILTINS, current).apply()
        }
    }

    fun restoreDefaultPdfs(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_CUSTOM_PDFS, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_HIDDEN_BUILTINS).apply()
    }

    private fun persistCustomPdfs(context: Context, records: List<CustomPdfRecord>) {
        val arr = JSONArray()
        records.forEach { rec ->
            arr.put(
                JSONObject().apply {
                    put("id", rec.id)
                    put("title", rec.title)
                    put("description", rec.description)
                    put("category", rec.category)
                    put("fileName", rec.fileName)
                    put("sizeBytes", rec.sizeBytes)
                }
            )
        }
        context.getSharedPreferences(PREFS_CUSTOM_PDFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CUSTOM_LIST, arr.toString())
            .apply()
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${kotlin.math.max(1, bytes / 1024)} KB"
            else -> String.format(Locale.US, "%.1f MB", bytes / (1024f * 1024f))
        }
    }

    fun buildDocumentCatalog(
        allAdvocates: List<AdvocateWithClerk>,
        allClerks: List<ClerkEntity>,
        clerksWithAdvocates: List<ClerkWithAdvocates>,
        filteredAdvocates: List<AdvocateWithClerk>,
        searchQuery: String,
        customPdfs: List<CustomPdfRecord> = emptyList(),
        hiddenBuiltInIds: Set<String> = emptySet()
    ): List<DirectoryPdfDocument> {
        return customPdfs.map { rec ->
            DirectoryPdfDocument(
                type = DirectoryDocumentType.CUSTOM_PDF,
                title = rec.title,
                description = rec.description,
                category = rec.category,
                fileName = rec.fileName,
                recordCount = 1,
                estimatedPages = 1,
                formatLabel = "PDF • Uploaded",
                metaSummary = "${formatFileSize(rec.sizeBytes)} • Admin Added",
                id = rec.id,
                isCustom = true
            )
        }
    }

    private data class TableColumn(
        val header: String,
        val width: Float
    )

    private data class TableRow(
        val cells: List<String>
    )

    fun loadOrGeneratePdfBytes(
        context: Context,
        type: DirectoryDocumentType,
        allAdvocates: List<AdvocateWithClerk>,
        allClerks: List<ClerkEntity>,
        clerksWithAdvocates: List<ClerkWithAdvocates>,
        filteredAdvocates: List<AdvocateWithClerk>,
        searchQuery: String,
        docId: String = type.name
    ): ByteArray {
        if (type == DirectoryDocumentType.APPEARANCE_NOTE) {
            return context.assets.open("APPEARANCE_NOTE.pdf").use { it.readBytes() }
        }
        if (type == DirectoryDocumentType.CUSTOM_PDF) {
            val file = File(File(context.filesDir, "custom_pdfs"), "$docId.pdf")
            if (file.exists()) return file.readBytes()
        }
        return generatePdfBytes(
            type = type,
            allAdvocates = allAdvocates,
            allClerks = allClerks,
            clerksWithAdvocates = clerksWithAdvocates,
            filteredAdvocates = filteredAdvocates,
            searchQuery = searchQuery
        )
    }

    fun generatePdfBytes(
        type: DirectoryDocumentType,
        allAdvocates: List<AdvocateWithClerk>,
        allClerks: List<ClerkEntity>,
        clerksWithAdvocates: List<ClerkWithAdvocates>,
        filteredAdvocates: List<AdvocateWithClerk>,
        searchQuery: String
    ): ByteArray {
        val clerkMap = allClerks.associateBy { it.id }

        val (docTitle, docSubtitle, columns, rows) = when (type) {
            DirectoryDocumentType.CUSTOM_PDF -> {
                val cols = listOf(TableColumn("Document", 140f), TableColumn("Status", 391f))
                Quad("Custom PDF Document", "Uploaded Document", cols, emptyList())
            }
            DirectoryDocumentType.APPEARANCE_NOTE -> {
                val cols = listOf(TableColumn("Form", 120f), TableColumn("Details", 411f))
                val dataRows = listOf(
                    TableRow(listOf("APPEARANCE NOTE", "To, The Registrar, High Court of Gujarat, Ahmedabad. Sub:- Appearance Note / VP filing."))
                )
                Quad("Appearance Note", "High Court of Gujarat, Ahmedabad", cols, dataRows)
            }
            DirectoryDocumentType.FULL_DIRECTORY -> {
                val cols = listOf(
                    TableColumn("Sr", 30f),
                    TableColumn("Code", 46f),
                    TableColumn("Advocate Name & Mobile", 195f),
                    TableColumn("Assigned Clerk(s) & Contact", 170f),
                    TableColumn("Panel", 90f)
                )
                val dataRows = allAdvocates.mapIndexed { idx, item ->
                    val adv = item.advocate
                    val advPhones = listOf(adv.primaryMobile, adv.secondaryMobile).filter { it.isNotBlank() }.joinToString(" / ")
                    val advCell = if (advPhones.isNotBlank()) "${adv.name}\nMob: $advPhones" else adv.name

                    val clerks = resolveClerks(item, clerkMap)
                    val clerkCell = if (clerks.isEmpty()) {
                        "-"
                    } else {
                        clerks.joinToString("\n") { c ->
                            val cPhones = listOf(c.primaryMobile, c.secondaryMobile).filter { it.isNotBlank() }.joinToString(", ")
                            if (cPhones.isNotBlank()) "${c.name} ($cPhones)" else c.name
                        }
                    }

                    val panels = adv.getPanelList().joinToString("; ")
                    TableRow(
                        listOf(
                            (adv.initialSrNo ?: (idx + 1)).toString(),
                            adv.code.ifBlank { "-" },
                            advCell,
                            clerkCell,
                            panels.ifBlank { "-" }
                        )
                    )
                }
                Quad(
                    "Complete Advocate & Clerk Directory",
                    "All Registered High Court Advocates (${dataRows.size} Records)",
                    cols,
                    dataRows
                )
            }

            DirectoryDocumentType.PANEL_ADVOCATES -> {
                val cols = listOf(
                    TableColumn("Sr", 30f),
                    TableColumn("Code", 46f),
                    TableColumn("Advocate Name", 155f),
                    TableColumn("Empaneled Organizations / Boards", 180f),
                    TableColumn("Clerk & Mobile", 120f)
                )
                val panelAdvs = allAdvocates.filter { it.advocate.panelAdvocate.isNotBlank() }
                val dataRows = panelAdvs.mapIndexed { idx, item ->
                    val adv = item.advocate
                    val clerks = resolveClerks(item, clerkMap)
                    val clerkCell = clerks.joinToString("\n") { c ->
                        val mob = c.primaryMobile.ifBlank { c.secondaryMobile }
                        if (mob.isNotBlank()) "${c.name} ($mob)" else c.name
                    }.ifBlank { "-" }
                    val panelText = adv.getPanelList().joinToString("\n")
                    TableRow(
                        listOf(
                            (idx + 1).toString(),
                            adv.code.ifBlank { "-" },
                            adv.name,
                            panelText,
                            clerkCell
                        )
                    )
                }
                Quad(
                    "Panel & Empaneled Advocates Register",
                    "Banks, Insurance, Govt. & Statutory Panels (${dataRows.size} Advocates)",
                    cols,
                    dataRows
                )
            }

            DirectoryDocumentType.CLERKS_REGISTER -> {
                val cols = listOf(
                    TableColumn("Sr", 30f),
                    TableColumn("Clerk Name", 155f),
                    TableColumn("Mobile Number(s)", 135f),
                    TableColumn("Linked Advocates", 211f)
                )
                val sourceClerks = if (clerksWithAdvocates.isNotEmpty()) {
                    clerksWithAdvocates
                } else {
                    allClerks.map { c ->
                        ClerkWithAdvocates(
                            clerk = c,
                            advocates = allAdvocates.filter { it.advocate.getAllClerkIds().contains(c.id) }.map { it.advocate }
                        )
                    }
                }
                val dataRows = sourceClerks.mapIndexed { idx, item ->
                    val c = item.clerk
                    val phones = listOf(c.primaryMobile, c.secondaryMobile).filter { it.isNotBlank() }.joinToString("\n").ifBlank { "-" }
                    val linked = allAdvocates
                        .filter { it.advocate.getAllClerkIds().contains(c.id) }
                        .joinToString(", ") { adv ->
                            if (adv.advocate.code.isNotBlank()) "${adv.advocate.name} (${adv.advocate.code})" else adv.advocate.name
                        }
                        .ifBlank { "-" }
                    TableRow(listOf((idx + 1).toString(), c.name, phones, linked))
                }
                Quad(
                    "Advocate Clerks Contact Register",
                    "Registered High Court Clerks (${dataRows.size} Clerks)",
                    cols,
                    dataRows
                )
            }

            DirectoryDocumentType.ADVOCATE_CODE_INDEX -> {
                val cols = listOf(
                    TableColumn("Code", 54f),
                    TableColumn("Advocate Name", 220f),
                    TableColumn("Advocate Mobile", 102f),
                    TableColumn("Primary Clerk", 155f)
                )
                val sorted = allAdvocates.sortedWith(
                    compareBy<AdvocateWithClerk>(
                        { it.advocate.code.toIntOrNull() ?: Int.MAX_VALUE },
                        { it.advocate.code },
                        { it.advocate.name }
                    )
                )
                val dataRows = sorted.map { item ->
                    val adv = item.advocate
                    val clerks = resolveClerks(item, clerkMap)
                    val clerkText = clerks.firstOrNull()?.let { c ->
                        if (c.primaryMobile.isNotBlank()) "${c.name} (${c.primaryMobile})" else c.name
                    } ?: "-"
                    val mob = listOf(adv.primaryMobile, adv.secondaryMobile).filter { it.isNotBlank() }.joinToString(", ").ifBlank { "-" }
                    TableRow(listOf(adv.code.ifBlank { "-" }, adv.name, mob, clerkText))
                }
                Quad(
                    "Advocate Code Quick Lookup Index",
                    "Sorted by Bar Code (${dataRows.size} Advocates)",
                    cols,
                    dataRows
                )
            }

            DirectoryDocumentType.MULTI_CLERK_ASSIGNMENTS -> {
                val cols = listOf(
                    TableColumn("Sr", 30f),
                    TableColumn("Code", 46f),
                    TableColumn("Advocate Name", 170f),
                    TableColumn("Clerk 1 & Contact", 142f),
                    TableColumn("Clerk 2 & Contact", 143f)
                )
                val multi = allAdvocates.filter { it.advocate.getAllClerkIds().size >= 2 }
                val dataRows = multi.mapIndexed { idx, item ->
                    val adv = item.advocate
                    val clerks = resolveClerks(item, clerkMap)
                    fun fmtClerk(c: ClerkEntity?): String {
                        if (c == null) return "-"
                        val mob = listOf(c.primaryMobile, c.secondaryMobile).filter { it.isNotBlank() }.joinToString(", ")
                        return if (mob.isNotBlank()) "${c.name}\n$mob" else c.name
                    }
                    TableRow(
                        listOf(
                            (idx + 1).toString(),
                            adv.code.ifBlank { "-" },
                            adv.name,
                            fmtClerk(clerks.getOrNull(0)),
                            fmtClerk(clerks.getOrNull(1))
                        )
                    )
                }
                Quad(
                    "Dual & Multi-Clerk Assignments",
                    "Advocates with 2+ Assigned Clerks (${dataRows.size} Advocates)",
                    cols,
                    dataRows
                )
            }

            DirectoryDocumentType.FILTERED_RESULTS -> {
                val hasActiveSearch = searchQuery.isNotBlank()
                val target = if (hasActiveSearch) {
                    filteredAdvocates
                } else {
                    allAdvocates.filter { it.advocate.getAllClerkIds().isEmpty() }
                }
                val cols = listOf(
                    TableColumn("Sr", 30f),
                    TableColumn("Code", 46f),
                    TableColumn("Advocate Name", 190f),
                    TableColumn("Mobile Number(s)", 115f),
                    TableColumn(if (hasActiveSearch) "Clerk / Panel" else "Panel", 150f)
                )
                val dataRows = target.mapIndexed { idx, item ->
                    val adv = item.advocate
                    val mob = listOf(adv.primaryMobile, adv.secondaryMobile).filter { it.isNotBlank() }.joinToString(", ").ifBlank { "-" }
                    val extra = if (hasActiveSearch) {
                        val clerks = resolveClerks(item, clerkMap).joinToString(", ") { it.name }
                        val panels = adv.getPanelList().joinToString(", ")
                        listOf(clerks, panels).filter { it.isNotBlank() }.joinToString("\n").ifBlank { "-" }
                    } else {
                        adv.getPanelList().joinToString(", ").ifBlank { "-" }
                    }
                    TableRow(listOf((idx + 1).toString(), adv.code.ifBlank { "-" }, adv.name, mob, extra))
                }
                val title = if (hasActiveSearch) "Search Results Export" else "Advocates Without Assigned Clerk"
                val sub = if (hasActiveSearch) {
                    "Query: \"${searchQuery.trim()}\" (${dataRows.size} Matches)"
                } else {
                    "Unassigned Advocates Register (${dataRows.size} Records)"
                }
                Quad(title, sub, cols, dataRows)
            }
        }

        return renderPdfDocument(docTitle, docSubtitle, columns, rows)
    }

    private fun resolveClerks(item: AdvocateWithClerk, clerkMap: Map<Long, ClerkEntity>): List<ClerkEntity> {
        val ids = item.advocate.getAllClerkIds()
        if (ids.isEmpty()) {
            return if (item.clerk != null) listOf(item.clerk) else emptyList()
        }
        val list = ids.mapNotNull { clerkMap[it] }
        return if (list.isEmpty() && item.clerk != null) listOf(item.clerk) else list
    }

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

    private fun renderPdfDocument(
        title: String,
        subtitle: String,
        columns: List<TableColumn>,
        rows: List<TableRow>
    ): ByteArray {
        val timestamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        val pagesStreams = mutableListOf<String>()
        var cmds = mutableListOf<String>()

        fun startNewPage(isFirstPage: Boolean): Float {
            cmds = mutableListOf()
            // Navy Header Banner & Amber Accent Stripe
            cmds.add("0.118 0.227 0.541 rg 0 794 $PAGE_WIDTH 48 re f")
            cmds.add("0.851 0.467 0.024 rg 0 791 $PAGE_WIDTH 3 re f")
            cmds.add("BT /F2 12 Tf 1 1 1 rg 32 818 Td (${sanitizePdfText("ADV & CLERK DIRECTORY - GUJARAT HIGH COURT")}) Tj ET")
            cmds.add("BT /F1 8.5 Tf 0.859 0.918 0.996 rg 32 804 Td (${sanitizePdfText("Official Legal Bar & Clerks Register | Generated $timestamp")}) Tj ET")

            var yTop = 774f
            if (isFirstPage) {
                cmds.add("BT /F2 11.5 Tf 0.059 0.090 0.165 rg 32 $yTop Td (${sanitizePdfText(title)}) Tj ET")
                yTop -= 13f
                cmds.add("BT /F1 8.5 Tf 0.278 0.333 0.412 rg 32 $yTop Td (${sanitizePdfText(subtitle)}) Tj ET")
                yTop -= 14f
            } else {
                cmds.add("BT /F1 8.5 Tf 0.278 0.333 0.412 rg 32 $yTop Td (${sanitizePdfText("$title (Continued)")}) Tj ET")
                yTop -= 10f
            }

            // Table Header Row
            val headerHeight = 18f
            val headerBottom = yTop - headerHeight
            cmds.add("0.859 0.918 0.996 rg $MARGIN_LEFT $headerBottom $CONTENT_WIDTH $headerHeight re f")
            cmds.add("0.796 0.835 0.882 RG 0.6 w $MARGIN_LEFT $headerBottom $CONTENT_WIDTH $headerHeight re S")
            var x = MARGIN_LEFT
            for (col in columns) {
                cmds.add("BT /F2 8 Tf 0.118 0.227 0.541 rg ${x + 4f} ${headerBottom + 5.5f} Td (${sanitizePdfText(col.header)}) Tj ET")
                x += col.width
                if (x < MARGIN_RIGHT - 1f) {
                    cmds.add("0.796 0.835 0.882 RG 0.6 w $x $headerBottom m $x $yTop l S")
                }
            }
            return headerBottom
        }

        var cursorY = startNewPage(true)

        if (rows.isEmpty()) {
            cmds.add("BT /F1 9 Tf 0.278 0.333 0.412 rg ${MARGIN_LEFT + 8f} ${cursorY - 22f} Td (${sanitizePdfText("No matching records available for this document.")}) Tj ET")
        } else {
            val lineHeight = 10f
            rows.forEachIndexed { rowIndex, row ->
                val wrappedCols = columns.mapIndexed { colIdx, col ->
                    val maxChars = kotlin.math.max(4, ((col.width - 8f) / 4.3f).toInt())
                    wrapText(row.cells.getOrElse(colIdx) { "" }, maxChars, maxLines = 4)
                }
                val maxLines = wrappedCols.maxOfOrNull { it.size } ?: 1
                val rowHeight = kotlin.math.max(16f, maxLines * lineHeight + 6f)

                if (cursorY - rowHeight < 48f) {
                    pagesStreams.add(cmds.joinToString("\n"))
                    cursorY = startNewPage(false)
                }

                val rowBottom = cursorY - rowHeight
                if (rowIndex % 2 == 1) {
                    cmds.add("0.973 0.980 0.988 rg $MARGIN_LEFT $rowBottom $CONTENT_WIDTH $rowHeight re f")
                }
                cmds.add("0.886 0.910 0.941 RG 0.5 w $MARGIN_LEFT $rowBottom $CONTENT_WIDTH $rowHeight re S")

                var x = MARGIN_LEFT
                columns.forEachIndexed { colIdx, col ->
                    val lines = wrappedCols[colIdx]
                    var textY = cursorY - 11f
                    for (line in lines) {
                        cmds.add("BT /F1 7.6 Tf 0.059 0.090 0.165 rg ${x + 4f} $textY Td (${sanitizePdfText(line)}) Tj ET")
                        textY -= lineHeight
                    }
                    x += col.width
                    if (x < MARGIN_RIGHT - 1f) {
                        cmds.add("0.886 0.910 0.941 RG 0.5 w $x $rowBottom m $x $cursorY l S")
                    }
                }
                cursorY = rowBottom
            }
        }

        pagesStreams.add(cmds.joinToString("\n"))

        val totalPages = pagesStreams.size
        val finalizedStreams = pagesStreams.mapIndexed { idx, body ->
            val footer = buildString {
                append("\n0.796 0.835 0.882 RG 0.6 w 32 36 m 563 36 l S\n")
                append("BT /F1 8 Tf 0.392 0.455 0.545 rg 32 24 Td (${sanitizePdfText("Adv & Clerk Directory | Print-Ready A4 Document")}) Tj ET\n")
                append("BT /F1 8 Tf 0.392 0.455 0.545 rg 505 24 Td (${sanitizePdfText("Page ${idx + 1} of $totalPages")}) Tj ET")
            }
            body + footer
        }

        val objects = mutableListOf<String>()
        fun addObj(body: String): Int {
            objects.add(body)
            return objects.size
        }

        val catalogId = addObj("")
        val pagesId = addObj("")
        val fontRegularId = addObj("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>")
        val fontBoldId = addObj("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>")

        val pageIds = mutableListOf<Int>()
        for (streamStr in finalizedStreams) {
            val streamBytesLen = streamStr.toByteArray(Charsets.ISO_8859_1).size
            val contentId = addObj("<< /Length $streamBytesLen >>\nstream\n$streamStr\nendstream")
            val pageId = addObj(
                "<< /Type /Page /Parent $pagesId 0 R /MediaBox [0 0 $PAGE_WIDTH $PAGE_HEIGHT] " +
                    "/Resources << /Font << /F1 $fontRegularId 0 R /F2 $fontBoldId 0 R >> >> " +
                    "/Contents $contentId 0 R >>"
            )
            pageIds.add(pageId)
        }

        objects[catalogId - 1] = "<< /Type /Catalog /Pages $pagesId 0 R >>"
        objects[pagesId - 1] = "<< /Type /Pages /Kids [${pageIds.joinToString(" ") { "$it 0 R" }}] /Count ${pageIds.size} >>"

        val out = ByteArrayOutputStream()
        fun writeAscii(s: String) {
            out.write(s.toByteArray(Charsets.ISO_8859_1))
        }

        writeAscii("%PDF-1.4\n")
        val offsets = mutableListOf(0)
        objects.forEachIndexed { idx, obj ->
            offsets.add(out.size())
            writeAscii("${idx + 1} 0 obj\n$obj\nendobj\n")
        }

        val xrefOffset = out.size()
        writeAscii("xref\n0 ${objects.size + 1}\n")
        writeAscii("0000000000 65535 f \n")
        for (i in 1..objects.size) {
            writeAscii("${offsets[i].toString().padStart(10, '0')} 00000 n \n")
        }
        writeAscii("trailer\n<< /Size ${objects.size + 1} /Root $catalogId 0 R >>\nstartxref\n$xrefOffset\n%%EOF")
        return out.toByteArray()
    }

    private fun sanitizePdfText(raw: String): String {
        val sb = StringBuilder(raw.length)
        for (ch in raw) {
            val c = if (ch.code in 32..126) ch else ' '
            when (c) {
                '\\' -> sb.append("\\\\")
                '(' -> sb.append("\\(")
                ')' -> sb.append("\\)")
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    private fun wrapText(text: String, maxChars: Int, maxLines: Int = 4): List<String> {
        if (text.isBlank()) return listOf("-")
        val result = mutableListOf<String>()
        val paragraphs = text.split("\n")
        for (para in paragraphs) {
            if (result.size >= maxLines) break
            var rem = para.trim().replace(Regex("\\s+"), " ")
            while (rem.length > maxChars && result.size < maxLines - 1) {
                var cut = rem.lastIndexOf(' ', maxChars)
                if (cut <= 0) cut = maxChars
                result.add(rem.substring(0, cut).trim())
                rem = rem.substring(cut).trim()
            }
            if (rem.isNotEmpty() && result.size < maxLines) {
                if (rem.length > maxChars) {
                    result.add(rem.substring(0, kotlin.math.max(1, maxChars - 3)) + "...")
                } else {
                    result.add(rem)
                }
            }
        }
        return if (result.isEmpty()) listOf("-") else result
    }

    fun savePdfToDownloads(context: Context, fileName: String, pdfBytes: ByteArray): PdfSaveResult {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/AdvClerkDirectory")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { os ->
                    os.write(pdfBytes)
                    os.flush()
                }
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
                return PdfSaveResult(
                    uri = uri,
                    displayLocation = "Downloads/AdvClerkDirectory/$fileName",
                    savedToPublicDownloads = true
                )
            }
        }

        // Pre-API 29 zero-permission app-scoped Downloads + FileProvider URI
        val dir = File(context.cacheDir, "pdfs")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, fileName)
        file.writeBytes(pdfBytes)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return PdfSaveResult(
            uri = uri,
            displayLocation = fileName,
            savedToPublicDownloads = false
        )
    }

    fun createShareablePdfUri(context: Context, fileName: String, pdfBytes: ByteArray): Uri {
        val dir = File(context.cacheDir, "pdfs")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, fileName)
        file.writeBytes(pdfBytes)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
