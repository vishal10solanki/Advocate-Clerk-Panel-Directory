package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.model.AdvocateEntity
import com.example.model.ClerkEntity
import com.example.model.ClerkWithAdvocates
import com.example.ui.DirectoryActions
import com.example.ui.theme.MyApplicationTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClerkCard(
    item: ClerkWithAdvocates,
    onEditClerk: () -> Unit,
    onDeleteClerk: () -> Unit,
    onAddAdvocateToClerk: () -> Unit,
    onEditAdvocate: (Long) -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    isAdmin: Boolean = false
) {
    val context = LocalContext.current
    val clerk = item.clerk
    val advocates = item.advocates
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("clerk_card_${clerk.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isCompact) 10.dp else 16.dp)
        ) {
            // Header Row: Clerk Icon, Name, Count badge, Edit & Delete buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SupportAgent,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = clerk.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "${advocates.size} Advocate${if (advocates.size == 1) "" else "s"} Assigned",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }


                if (isAdmin) {
                    IconButton(
                        onClick = onEditClerk,
                        modifier = Modifier.testTag("edit_clerk_${clerk.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Clerk",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDeleteClerk,
                        modifier = Modifier.testTag("delete_clerk_${clerk.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Clerk",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Clerk Mobiles & One-Tap Actions
            val hasClerkPri = clerk.primaryMobile.isNotBlank()
            val hasClerkSec = clerk.secondaryMobile.isNotBlank()

            if (hasClerkPri || hasClerkSec) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (hasClerkPri) {
                        PhoneActionChip(
                            label = "Primary: ${clerk.primaryMobile}",
                            number = clerk.primaryMobile,
                            onCall = { DirectoryActions.dialPhoneNumber(context, clerk.primaryMobile) },
                            onWhatsApp = { DirectoryActions.openWhatsApp(context, clerk.primaryMobile) },
                            testTagPrefix = "clerk_view_pri_${clerk.id}"
                        )
                    }
                    if (hasClerkSec) {
                        PhoneActionChip(
                            label = "Secondary: ${clerk.secondaryMobile}",
                            number = clerk.secondaryMobile,
                            onCall = { DirectoryActions.dialPhoneNumber(context, clerk.secondaryMobile) },
                            onWhatsApp = { DirectoryActions.openWhatsApp(context, clerk.secondaryMobile) },
                            testTagPrefix = "clerk_view_sec_${clerk.id}"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // "Add Advocate to this Clerk" Action Button (Admin only)
            if (isAdmin) {
                Button(
                    onClick = onAddAdvocateToClerk,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_advocate_to_clerk_button_${clerk.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Advocate to this Clerk")
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Assigned Advocates Section Header with Expand/Collapse toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "Assigned Advocates (${advocates.size})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (advocates.isEmpty()) {
                        Text(
                            text = "No advocates assigned to this clerk yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        advocates.forEachIndexed { index, advocate ->
                            if (index > 0) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = advocate.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (advocate.code.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = "Code: ${advocate.code}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                val advPhones = listOfNotNull(
                                    advocate.primaryMobile.takeIf { it.isNotBlank() },
                                    advocate.secondaryMobile.takeIf { it.isNotBlank() }
                                )

                                if (advPhones.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        advPhones.forEachIndexed { pIdx, phone ->
                                            PhoneActionChip(
                                                label = phone,
                                                number = phone,
                                                onCall = { DirectoryActions.dialPhoneNumber(context, phone) },
                                                onWhatsApp = { DirectoryActions.openWhatsApp(context, phone) },
                                                testTagPrefix = "clerk_adv_${advocate.id}_p$pIdx"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Clerk Card Preview", showBackground = true)
@Composable
fun ClerkCardPreview() {
    val sample = ClerkWithAdvocates(
        clerk = ClerkEntity(
            id = 1,
            name = "PRADIPBHAI S SHUKLA",
            primaryMobile = "9825049155",
            secondaryMobile = "9825049156"
        ),
        advocates = listOf(
            AdvocateEntity(
                id = 1,
                name = "NITIN M AMIN",
                code = "126",
                primaryMobile = "9825049155",
                secondaryMobile = "",
                clerkId = 1,
                initialSrNo = 1
            ),
            AdvocateEntity(
                id = 2,
                name = "SANJAY M AMIN",
                code = "127",
                primaryMobile = "9825049157",
                secondaryMobile = "",
                clerkId = 1,
                initialSrNo = 2
            )
        )
    )

    MyApplicationTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ClerkCard(
                item = sample,
                onEditClerk = {},
                onDeleteClerk = {},
                onAddAdvocateToClerk = {},
                onEditAdvocate = {}
            )
        }
    }
}
