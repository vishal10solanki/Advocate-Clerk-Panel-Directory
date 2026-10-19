package com.example.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ui.ClerkFormData
import com.example.ui.theme.MyApplicationTheme

@Composable
fun ClerkEditDialog(
    initialData: ClerkFormData,
    onSave: (ClerkFormData) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialData.name) }
    var primaryMobile by remember { mutableStateOf(initialData.primaryMobile) }
    var secondaryMobile by remember { mutableStateOf(initialData.secondaryMobile) }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialData.isEdit) "Edit Clerk Details" else "Add New Clerk")
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    label = { Text("Clerk Name *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Clerk name cannot be empty") }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clerk_name_input")
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
                        .testTag("clerk_primary_mobile_input")
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
                        .testTag("clerk_secondary_mobile_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    } else {
                        onSave(
                            initialData.copy(
                                name = name.trim(),
                                primaryMobile = primaryMobile.trim(),
                                secondaryMobile = secondaryMobile.trim()
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("save_clerk_button")
            ) {
                Text(if (initialData.isEdit) "Save Changes" else "Add Clerk")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_clerk_button")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Preview(name = "Clerk Edit Dialog Preview", showBackground = true)
@Composable
fun ClerkEditDialogPreview() {
    MyApplicationTheme {
        ClerkEditDialog(
            initialData = ClerkFormData(
                id = 1,
                name = "PRADIPBHAI S SHUKLA",
                primaryMobile = "9825049155",
                secondaryMobile = "9825049156",
                isEdit = true
            ),
            onSave = {},
            onDismiss = {}
        )
    }
}
