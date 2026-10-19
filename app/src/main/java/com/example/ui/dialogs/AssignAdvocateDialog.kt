package com.example.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.model.AdvocateEntity
import com.example.model.AdvocateWithClerk
import com.example.model.ClerkEntity
import com.example.ui.theme.MyApplicationTheme

@Composable
fun AssignAdvocateDialog(
    targetClerk: ClerkEntity,
    allAdvocates: List<AdvocateWithClerk>,
    onAssignAdvocate: (advocateId: Long) -> Unit,
    onCreateNewAdvocate: () -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredAdvocates = remember(allAdvocates, searchQuery) {
        if (searchQuery.isBlank()) allAdvocates
        else {
            val q = searchQuery.trim().lowercase()
            allAdvocates.filter {
                it.advocate.name.lowercase().contains(q) ||
                        it.advocate.code.lowercase().contains(q)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Add Advocate to Clerk",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Assigning to: ${targetClerk.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search advocates by name, code...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_advocates_to_assign")
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onCreateNewAdvocate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_new_advocate_for_clerk_option")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Create Brand New Advocate")
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                ) {
                    if (filteredAdvocates.isEmpty()) {
                        item {
                            Text(
                                text = "No matching advocates found.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        items(filteredAdvocates, key = { it.advocate.id }) { item ->
                            val isAlreadyAssigned = item.advocate.clerkId == targetClerk.id
                            Surface(
                                color = if (isAlreadyAssigned) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (!isAlreadyAssigned) {
                                            onAssignAdvocate(item.advocate.id)
                                        }
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (isAlreadyAssigned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.advocate.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isAlreadyAssigned) FontWeight.Bold else FontWeight.Normal
                                        )
                                        val subInfo = buildString {
                                            if (item.advocate.code.isNotBlank()) append("Code: ${item.advocate.code} ")
                                            if (item.clerk != null) append("• Current: ${item.clerk.name}")
                                        }
                                        if (subInfo.isNotBlank()) {
                                            Text(
                                                text = subInfo,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (isAlreadyAssigned) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Already Assigned",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Preview(name = "Assign Advocate Dialog Preview", showBackground = true)
@Composable
fun AssignAdvocateDialogPreview() {
    MyApplicationTheme {
        AssignAdvocateDialog(
            targetClerk = ClerkEntity(id = 1, name = "PRADIPBHAI S SHUKLA", primaryMobile = "9825049155"),
            allAdvocates = listOf(
                AdvocateWithClerk(
                    advocate = AdvocateEntity(id = 1, name = "NITIN M AMIN", code = "126", primaryMobile = "9825049155", secondaryMobile = "", clerkId = 1, initialSrNo = 1),
                    clerk = ClerkEntity(id = 1, name = "PRADIPBHAI S SHUKLA", primaryMobile = "9825049155")
                ),
                AdvocateWithClerk(
                    advocate = AdvocateEntity(id = 2, name = "SANJAY M AMIN", code = "127", primaryMobile = "9825049157", secondaryMobile = "", clerkId = null, initialSrNo = 2),
                    clerk = null
                )
            ),
            onAssignAdvocate = {},
            onCreateNewAdvocate = {},
            onDismiss = {}
        )
    }
}
