package com.example.ui.dialogs

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun AddPdfDialog(
    onSave: (title: String, description: String, category: String, fileName: String, pdfBytes: ByteArray) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var selectedFileName by remember { mutableStateOf("") }
    var selectedPdfBytes by remember { mutableStateOf<ByteArray?>(null) }
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Court Forms") }
    var description by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val presetCategories = remember {
        listOf("Court Forms", "Notices", "Circulars", "Custom")
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val resolver = context.contentResolver
            var displayName = "document.pdf"
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) {
                        val queriedName = cursor.getString(idx)
                        if (!queriedName.isNullOrBlank()) {
                            displayName = queriedName
                        }
                    }
                }
            }

            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes == null || bytes.isEmpty()) {
                errorMessage = "Selected PDF file is empty or unreadable."
                selectedPdfBytes = null
                selectedFileName = ""
                return@rememberLauncherForActivityResult
            }

            val header = String(bytes.copyOfRange(0, minOf(5, bytes.size)), Charsets.US_ASCII)
            if (!header.startsWith("%PDF-")) {
                errorMessage = "Please select a valid PDF document (.pdf)."
                selectedPdfBytes = null
                selectedFileName = ""
                return@rememberLauncherForActivityResult
            }

            selectedPdfBytes = bytes
            selectedFileName = displayName
            errorMessage = null
            if (title.isBlank()) {
                title = displayName
                    .removeSuffix(".pdf")
                    .removeSuffix(".PDF")
                    .replace('_', ' ')
                    .replace('-', ' ')
                    .trim()
            }
        } catch (e: Exception) {
            errorMessage = "Could not read file: ${e.localizedMessage ?: "Unknown error"}"
            selectedPdfBytes = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Add PDF Document",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Admin only: Choose a PDF file from your device and enter its card details.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("pdf_picker_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedPdfBytes == null) "Choose PDF File" else "Change PDF File",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                selectedPdfBytes?.let { bytes ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedFileName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val sizeLabel = if (bytes.size < 1024 * 1024) {
                                    "${maxOf(1, bytes.size / 1024)} KB"
                                } else {
                                    String.format(Locale.US, "%.1f MB", bytes.size / (1024f * 1024f))
                                }
                                Text(
                                    text = "$sizeLabel • Ready to add",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("Document Title *") },
                    placeholder = { Text("e.g., Vakalatnama Form") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pdf_title_input")
                )

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetCategories.forEach { preset ->
                        FilterChip(
                            selected = category.equals(preset, ignoreCase = true),
                            onClick = { category = preset },
                            label = { Text(preset) }
                        )
                    }
                }

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category Label") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pdf_category_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("Short note shown on the PDF card") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pdf_description_input")
                )

                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bytes = selectedPdfBytes
                    if (bytes == null) {
                        errorMessage = "Please select a PDF file first."
                        return@Button
                    }
                    if (title.isBlank()) {
                        errorMessage = "Please enter a document title."
                        return@Button
                    }
                    onSave(
                        title.trim(),
                        description.trim(),
                        category.trim().ifBlank { "Custom" },
                        selectedFileName.ifBlank { "${title.trim()}.pdf" },
                        bytes
                    )
                },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("save_pdf_button")
            ) {
                Text("Add PDF")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("cancel_pdf_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
