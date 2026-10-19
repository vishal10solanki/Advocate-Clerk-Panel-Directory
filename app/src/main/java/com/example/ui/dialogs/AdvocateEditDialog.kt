package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.model.ClerkEntity
import com.example.model.parsePanelList
import com.example.ui.AdvocateFormData
import com.example.ui.theme.MyApplicationTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdvocateEditDialog(
    initialData: AdvocateFormData,
    availableClerks: List<ClerkEntity>,
    onSave: (AdvocateFormData) -> Unit,
    onOpenAddClerk: () -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialData.name) }
    var code by remember { mutableStateOf(initialData.code) }
    var primaryMobile by remember { mutableStateOf(initialData.primaryMobile) }
    var secondaryMobile by remember { mutableStateOf(initialData.secondaryMobile) }

    val initialPanels = remember(initialData) {
        val existing = if (initialData.panels.isNotEmpty()) {
            initialData.panels
        } else if (initialData.panelAdvocate.isNotBlank()) {
            parsePanelList(initialData.panelAdvocate)
        } else {
            emptyList()
        }
        if (existing.isEmpty()) listOf("") else existing
    }
    var panels by remember { mutableStateOf(initialPanels) }

    val initialClerkIds = remember(initialData) {
        if (initialData.clerkIds.isNotEmpty()) {
            initialData.clerkIds
        } else if (initialData.clerkId != null && initialData.clerkId > 0) {
            listOf(initialData.clerkId)
        } else {
            emptyList()
        }
    }
    var selectedClerkIds by remember { mutableStateOf(initialClerkIds) }
    var nameError by remember { mutableStateOf(false) }

    var clerkDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialData.isEdit) "Edit Advocate Record" else "Add New Advocate")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    label = { Text("Advocate Name *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Name is required") }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("advocate_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Advocate Code / Bar No.") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("advocate_code_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = primaryMobile,
                    onValueChange = { primaryMobile = it },
                    label = { Text("Primary Mobile Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("advocate_primary_mobile_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = secondaryMobile,
                    onValueChange = { secondaryMobile = it },
                    label = { Text("Secondary Mobile Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("advocate_secondary_mobile_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Multi-Panel Advocate Section with "Add More" option
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("advocate_panel_section")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        // Header with Icon, Title, and "Add More" button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Panel Advocate Affiliations",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                val count = panels.count { it.isNotBlank() }
                                Text(
                                    text = if (count == 0) "Multiple panels supported" else "$count panel(s) added",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // "Add More" Button
                            FilledTonalButton(
                                onClick = {
                                    panels = panels + ""
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("add_more_panel_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Add More",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Render each panel input row
                        if (panels.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No panels added yet",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedButton(
                                        onClick = { panels = listOf("") },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add First Panel")
                                    }
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                panels.forEachIndexed { index, panelItem ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = panelItem,
                                            onValueChange = { newVal ->
                                                panels = panels.toMutableList().apply {
                                                    this[index] = newVal
                                                }
                                            },
                                            label = { Text("Panel #${index + 1}") },
                                            placeholder = { Text("e.g. State Bank of India (SBI) Panel") },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Gavel,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            trailingIcon = {
                                                if (panels.size > 1 || panelItem.isNotBlank()) {
                                                    IconButton(
                                                        onClick = {
                                                            val updated = panels.toMutableList().apply {
                                                                removeAt(index)
                                                            }
                                                            panels = if (updated.isEmpty()) listOf("") else updated
                                                        },
                                                        modifier = Modifier.testTag("remove_panel_${index}")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Remove Panel #${index + 1}",
                                                            tint = MaterialTheme.colorScheme.error
                                                        )
                                                    }
                                                }
                                            },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag(if (index == 0) "advocate_panel_input" else "panel_input_${index}")
                                        )
                                    }
                                }

                                // Secondary Add More link at bottom of list
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = { panels = panels + "" },
                                        modifier = Modifier.testTag("add_more_panel_link")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("+ Add More Panel")
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Quick suggestions (tap to append):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                "Bank Panel",
                                "SBI Panel",
                                "Bank of Baroda Panel",
                                "Govt. Panel",
                                "Insurance Panel",
                                "LIC Panel",
                                "National Insurance Panel",
                                "Legal Aid Panel",
                                "Railway Panel"
                            ).forEach { chipText ->
                                AssistChip(
                                    onClick = {
                                        val trimmedChip = chipText.trim()
                                        val emptyIndex = panels.indexOfFirst { it.isBlank() }
                                        if (emptyIndex != -1) {
                                            panels = panels.toMutableList().apply {
                                                this[emptyIndex] = trimmedChip
                                            }
                                        } else if (!panels.contains(trimmedChip)) {
                                            panels = panels + trimmedChip
                                        }
                                    },
                                    label = { Text(chipText, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Multiple Clerks Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Assigned Clerks (${selectedClerkIds.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = onOpenAddClerk,
                        modifier = Modifier.testTag("quick_add_clerk_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Clerk")
                    }
                }

                Text(
                    text = "Some advocates have multiple clerks. Assign one or more clerks below.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                // List currently assigned clerks
                if (selectedClerkIds.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No clerk assigned yet. Tap below to select and assign clerks.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        selectedClerkIds.forEachIndexed { index, clerkId ->
                            val c = availableClerks.firstOrNull { it.id == clerkId }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = c?.name ?: "Clerk #$clerkId",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            if (index == 0 && selectedClerkIds.size > 1) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = "PRIMARY",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                        if (c != null && c.primaryMobile.isNotBlank()) {
                                            Text(
                                                text = c.primaryMobile,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            selectedClerkIds = selectedClerkIds.filter { it != clerkId }
                                        },
                                        modifier = Modifier.size(28.dp).testTag("remove_clerk_${clerkId}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove Clerk",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Dropdown to add/assign clerk
                val unassignedClerks = availableClerks.filter { !selectedClerkIds.contains(it.id) }
                ExposedDropdownMenuBox(
                    expanded = clerkDropdownExpanded,
                    onExpandedChange = { clerkDropdownExpanded = !clerkDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = if (selectedClerkIds.isEmpty()) "Select Clerk to Assign" else "+ Add Another Clerk",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (selectedClerkIds.isEmpty()) "Assign Clerk" else "Add More Clerks") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clerkDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                            .testTag("clerk_dropdown_selector")
                    )
                    ExposedDropdownMenu(
                        expanded = clerkDropdownExpanded,
                        onDismissRequest = { clerkDropdownExpanded = false }
                    ) {
                        if (unassignedClerks.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("All available clerks are already assigned") },
                                onClick = { clerkDropdownExpanded = false }
                            )
                        } else {
                            unassignedClerks.forEach { clerk ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(clerk.name, fontWeight = FontWeight.SemiBold)
                                            if (clerk.primaryMobile.isNotBlank()) {
                                                Text(
                                                    clerk.primaryMobile,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedClerkIds = selectedClerkIds + clerk.id
                                        clerkDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    } else {
                        val cleanedPanels = panels.map { it.trim() }.filter { it.isNotBlank() }
                        val joinedPanels = cleanedPanels.joinToString("\n")
                        onSave(
                            initialData.copy(
                                name = name.trim(),
                                code = code.trim(),
                                primaryMobile = primaryMobile.trim(),
                                secondaryMobile = secondaryMobile.trim(),
                                panelAdvocate = joinedPanels,
                                panels = cleanedPanels,
                                clerkId = selectedClerkIds.firstOrNull(),
                                clerkIds = selectedClerkIds
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("save_advocate_button")
            ) {
                Text(if (initialData.isEdit) "Save Changes" else "Add Record")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_advocate_button")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Preview(name = "Advocate Edit Dialog Preview", showBackground = true)
@Composable
fun AdvocateEditDialogPreview() {
    MyApplicationTheme {
        AdvocateEditDialog(
            initialData = AdvocateFormData(
                id = 1,
                name = "NITIN M AMIN",
                code = "126",
                primaryMobile = "9825049155",
                secondaryMobile = "9825049156",
                panelAdvocate = "State Bank of India (SBI) Panel\nBank of Baroda Panel",
                panels = listOf("State Bank of India (SBI) Panel", "Bank of Baroda Panel"),
                clerkId = 1,
                isEdit = true
            ),
            availableClerks = listOf(
                ClerkEntity(id = 1, name = "PRADIPBHAI S SHUKLA", primaryMobile = "9825049155")
            ),
            onSave = {},
            onOpenAddClerk = {},
            onDismiss = {}
        )
    }
}
