package com.example.ui.components

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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdvocateEntity
import com.example.model.AdvocateWithClerk
import com.example.model.ClerkEntity
import com.example.model.getAllClerkIds
import com.example.model.getPanelList
import com.example.ui.DirectoryActions
import com.example.ui.theme.CallBlue
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.WhatsAppGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdvocateCard(
    item: AdvocateWithClerk,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReplaceClerk: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    showClerkPhone: Boolean = true,
    showPanels: Boolean = true,
    isAdmin: Boolean = false,
    allClerks: List<ClerkEntity> = emptyList()
) {
    val context = LocalContext.current
    val advocate = item.advocate
    val clerk = item.clerk
    val allClerkIds = remember(advocate.clerkId, advocate.additionalClerkIds) { advocate.getAllClerkIds() }
    val assignedClerks = remember(advocate, clerk, allClerks, allClerkIds) {
        if (allClerkIds.isEmpty()) {
            if (clerk != null) listOf(clerk) else emptyList()
        } else {
            val list = allClerkIds.mapNotNull { id -> allClerks.firstOrNull { it.id == id } }
            if (list.isEmpty() && clerk != null) listOf(clerk) else list
        }
    }
    val panelList = remember(advocate.panelAdvocate) { advocate.getPanelList() }
    val shareMessage = remember(advocate, assignedClerks) {
        DirectoryActions.buildAdvocateMultiClerkShareMessage(advocate, assignedClerks)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("advocate_card_${advocate.id}"),
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
            // Header Row: Avatar, Name & Code, Edit & Delete actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = advocate.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (advocate.code.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Badge,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Code: ${advocate.code}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            if (advocate.initialSrNo != null) {
                                Text(
                                    text = "#${advocate.initialSrNo}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }


                IconButton(
                    onClick = {
                        DirectoryActions.shareViaWhatsApp(context, shareMessage)
                    },
                    modifier = Modifier.testTag("share_advocate_${advocate.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share via WhatsApp",
                        tint = WhatsAppGreen
                    )
                }

                if (isAdmin) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("edit_advocate_${advocate.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Advocate",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_advocate_${advocate.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Advocate",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Advocate Mobile Numbers, Panel Advocate & One-Tap Actions
            val hasAdvPrimary = advocate.primaryMobile.isNotBlank()
            val hasAdvSecondary = advocate.secondaryMobile.isNotBlank()
            val hasPanelAdvocate = panelList.isNotEmpty()

            if (hasAdvPrimary || hasAdvSecondary || hasPanelAdvocate) {
                Text(
                    text = "Advocate Contact Details",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (hasAdvPrimary) {
                        PhoneActionChip(
                            label = "Primary: ${advocate.primaryMobile}",
                            number = advocate.primaryMobile,
                            onCall = { DirectoryActions.dialPhoneNumber(context, advocate.primaryMobile) },
                            onWhatsApp = { DirectoryActions.openWhatsApp(context, advocate.primaryMobile) },
                            testTagPrefix = "adv_pri_${advocate.id}"
                        )
                    }
                    if (hasAdvSecondary) {
                        PhoneActionChip(
                            label = "Secondary: ${advocate.secondaryMobile}",
                            number = advocate.secondaryMobile,
                            onCall = { DirectoryActions.dialPhoneNumber(context, advocate.secondaryMobile) },
                            onWhatsApp = { DirectoryActions.openWhatsApp(context, advocate.secondaryMobile) },
                            testTagPrefix = "adv_sec_${advocate.id}"
                        )
                    }

                    // Dedicated Panel Advocate Section with Multi-Panel and "Add More" option
                    if (showPanels && hasPanelAdvocate) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("panel_advocate_section_${advocate.id}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Gavel,
                                                contentDescription = "Panel Advocate",
                                                tint = MaterialTheme.colorScheme.onSecondary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (panelList.size > 1) "Panel Advocate (${panelList.size} Panels)" else "Panel Advocate",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Text(
                                            text = "Official Empanelment List",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
                                        )
                                    }

                                    // "Add More" Button (Admin Only)
                                    if (isAdmin) {
                                        FilledTonalButton(
                                            onClick = onEdit,
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                                                contentColor = MaterialTheme.colorScheme.secondary
                                            ),
                                            modifier = Modifier.testTag("panel_add_more_button_${advocate.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Add More",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                    thickness = 0.8.dp
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    panelList.forEachIndexed { idx, panelName ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                                    modifier = Modifier.size(20.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = "${idx + 1}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.secondary,
                                                            fontSize = 10.sp
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = panelName,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (isAdmin) {
                        OutlinedButton(
                            onClick = onEdit,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.secondary
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_panel_button_${advocate.id}")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "+ Add Panel Advocate Affiliation",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            } else if (isAdmin) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_panel_button_${advocate.id}")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+ Add Panel Advocate Affiliation",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Clerk Section (Supports Multiple Clerks)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.PersonOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (assignedClerks.size > 1) "Assigned Clerks (${assignedClerks.size})" else "Assigned Clerk",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )

                if (isAdmin) {
                    FilledTonalButton(
                        onClick = onEdit,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("manage_clerks_button_${advocate.id}")
                    ) {
                        Icon(
                            imageVector = if (assignedClerks.isEmpty()) Icons.Default.Add else Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (assignedClerks.isEmpty()) "Assign Clerk" else if (assignedClerks.size > 1) "Manage Clerks" else "+ Add / Change",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (assignedClerks.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    assignedClerks.forEachIndexed { index, c ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = c.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (assignedClerks.size > 1) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (index == 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = if (index == 0) "Primary Clerk" else "Clerk #${index + 1}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (index == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                val hasClerkPri = c.primaryMobile.isNotBlank()
                                val hasClerkSec = c.secondaryMobile.isNotBlank()

                                if (showClerkPhone && (hasClerkPri || hasClerkSec)) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (hasClerkPri) {
                                            PhoneActionChip(
                                                label = "Primary: ${c.primaryMobile}",
                                                number = c.primaryMobile,
                                                onCall = { DirectoryActions.dialPhoneNumber(context, c.primaryMobile) },
                                                onWhatsApp = { DirectoryActions.openWhatsApp(context, c.primaryMobile) },
                                                testTagPrefix = "clerk_pri_${advocate.id}_${c.id}"
                                            )
                                        }
                                        if (hasClerkSec) {
                                            PhoneActionChip(
                                                label = "Secondary: ${c.secondaryMobile}",
                                                number = c.secondaryMobile,
                                                onCall = { DirectoryActions.dialPhoneNumber(context, c.secondaryMobile) },
                                                onWhatsApp = { DirectoryActions.openWhatsApp(context, c.secondaryMobile) },
                                                testTagPrefix = "clerk_sec_${advocate.id}_${c.id}"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "No clerk currently assigned.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Ready to share via WhatsApp section
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = WhatsAppGreen.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, WhatsAppGreen.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("share_whatsapp_container_${advocate.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            DirectoryActions.shareViaWhatsApp(context, shareMessage)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WhatsAppGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_whatsapp_button_${advocate.id}")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Share via WhatsApp",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Share via WhatsApp",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    FilledTonalIconButton(
                        onClick = {
                            DirectoryActions.copyToClipboard(context, shareMessage)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("copy_details_button_${advocate.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Details",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhoneActionChip(
    label: String,
    number: String,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    testTagPrefix: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                // One-tap Direct Call button
                FilledTonalButton(
                    onClick = onCall,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = CallBlue.copy(alpha = 0.15f),
                        contentColor = CallBlue
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("${testTagPrefix}_call")
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call $number",
                        tint = CallBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Call",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CallBlue
                    )
                }

                // One-tap Direct WhatsApp button
                Button(
                    onClick = onWhatsApp,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("${testTagPrefix}_whatsapp")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "WhatsApp $number",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "WhatsApp",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview(name = "Advocate Card Preview", showBackground = true)
@Composable
fun AdvocateCardPreview() {
    val sample = AdvocateWithClerk(
        advocate = AdvocateEntity(
            id = 1,
            name = "NITIN M AMIN",
            code = "126",
            primaryMobile = "9825049155",
            secondaryMobile = "9825049156",
            panelAdvocate = "State Bank of India (SBI) Panel\nBank of Baroda Panel\nLife Insurance Corp. (LIC) Panel",
            clerkId = 1,
            initialSrNo = 1
        ),
        clerk = ClerkEntity(
            id = 1,
            name = "PRADIPBHAI S SHUKLA",
            primaryMobile = "9825049155",
            secondaryMobile = "9825049156"
        )
    )

    MyApplicationTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            AdvocateCard(
                item = sample,
                onEdit = {},
                onDelete = {},
                onReplaceClerk = {}
            )
        }
    }
}
