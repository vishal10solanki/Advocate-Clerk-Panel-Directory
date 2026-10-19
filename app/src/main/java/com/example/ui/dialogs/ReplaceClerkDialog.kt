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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DividerDefaults
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
fun ReplaceClerkDialog(
    targetAdvocate: AdvocateWithClerk,
    availableClerks: List<ClerkEntity>,
    onSelectClerk: (Long?) -> Unit,
    onCreateNewClerk: () -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredClerks = remember(availableClerks, searchQuery) {
        if (searchQuery.isBlank()) availableClerks
        else {
            val q = searchQuery.trim().lowercase()
            availableClerks.filter {
                it.name.lowercase().contains(q)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Replace Clerk",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "For Advocate: ${targetAdvocate.advocate.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (targetAdvocate.clerk != null) {
                    Text(
                        text = "Current: ${targetAdvocate.clerk.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search clerks...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_clerks_to_replace")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onCreateNewClerk,
                        modifier = Modifier.testTag("create_new_clerk_option")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Create New Clerk")
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (targetAdvocate.advocate.clerkId != null) {
                        TextButton(
                            onClick = { onSelectClerk(null) },
                            modifier = Modifier.testTag("remove_clerk_option")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                ) {
                    if (filteredClerks.isEmpty()) {
                        item {
                            Text(
                                text = "No matching clerks found.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        items(filteredClerks, key = { it.id }) { clerk ->
                            val isSelected = targetAdvocate.advocate.clerkId == clerk.id
                            Surface(
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectClerk(clerk.id) }
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
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = clerk.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        val phones = listOf(clerk.primaryMobile, clerk.secondaryMobile).filter { it.isNotBlank() }
                                        if (phones.isNotEmpty()) {
                                            Text(
                                                text = phones.joinToString(" • "),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
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

@Preview(name = "Replace Clerk Dialog Preview", showBackground = true)
@Composable
fun ReplaceClerkDialogPreview() {
    MyApplicationTheme {
        ReplaceClerkDialog(
            targetAdvocate = AdvocateWithClerk(
                advocate = AdvocateEntity(id = 1, name = "NITIN M AMIN", code = "126", primaryMobile = "9825049155", secondaryMobile = "", clerkId = 1, initialSrNo = 1),
                clerk = ClerkEntity(id = 1, name = "PRADIPBHAI S SHUKLA", primaryMobile = "9825049155")
            ),
            availableClerks = listOf(
                ClerkEntity(id = 1, name = "PRADIPBHAI S SHUKLA", primaryMobile = "9825049155"),
                ClerkEntity(id = 2, name = "RAMESHBHAI PATEL", primaryMobile = "9825049199")
            ),
            onSelectClerk = {},
            onCreateNewClerk = {},
            onDismiss = {}
        )
    }
}
