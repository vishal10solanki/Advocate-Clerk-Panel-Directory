package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.DirectoryTab
import com.example.ui.theme.MyApplicationTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectoryTopBar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    selectedTab: DirectoryTab,
    onTabSelected: (DirectoryTab) -> Unit,
    advocateCount: Int,
    clerkCount: Int,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onShareApp: () -> Unit,
    onExportCsv: () -> Unit,
    onResetData: () -> Unit,
    onRefreshData: () -> Unit = {},
    onOpenAdminAuth: () -> Unit = {},
    modifier: Modifier = Modifier,
    isAdmin: Boolean = false,
    onLogoClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
    ) {
        // App Title Row with Three-Lined Button (Hamburger Menu)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // Three lined hamburger button
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier.testTag("menu_hamburger_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Navigation Menu",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Clickable App Logo and Title on top side (Click 7 times for Admin mode)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onLogoClick() }
                    .padding(vertical = 4.dp, horizontal = 2.dp)
                    .testTag("app_logo_top")
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isAdmin) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Gavel,
                            contentDescription = "App Logo - Click 7 times for Admin",
                            tint = if (isAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isAdmin) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "ADMIN",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = if (isAdmin) "Admin Active • Full Edit" else "Legal Bar & Clerks Register",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 1. 🔄 Refresh Data button
            IconButton(
                onClick = onRefreshData,
                modifier = Modifier.testTag("topbar_refresh_data_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "🔄 Refresh Data",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // 2. Settings button
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("topbar_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 4. Share App button
            IconButton(
                onClick = onShareApp,
                modifier = Modifier.testTag("topbar_share_app_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share App",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Global Real-time Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = {
                Text(
                    if (selectedTab == DirectoryTab.ADVOCATES) {
                        "Search advocate name, code, panel..."
                    } else {
                        "Search clerk name..."
                    }
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChanged("") },
                        modifier = Modifier.testTag("clear_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("global_search_input")
        )

        // Quick Filters row for fast panel discovery
        if (selectedTab == DirectoryTab.ADVOCATES) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isPanelAllActive = searchQuery.equals("panel", ignoreCase = true)
                FilterChip(
                    selected = isPanelAllActive,
                    onClick = {
                        if (isPanelAllActive) onSearchQueryChanged("") else onSearchQueryChanged("panel")
                    },
                    label = { Text("Panel Advocates", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    modifier = Modifier.testTag("filter_panel_advocates")
                )

                listOf("SBI Panel", "LIC Panel", "Govt. Panel", "Bank of Baroda", "Insurance Panel").forEach { quickTag ->
                    val isActive = searchQuery.equals(quickTag, ignoreCase = true)
                    FilterChip(
                        selected = isActive,
                        onClick = {
                            if (isActive) onSearchQueryChanged("") else onSearchQueryChanged(quickTag)
                        },
                        label = { Text(quickTag, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("filter_${quickTag.lowercase().replace(" ", "_")}")
                    )
                }
            }
        }

        // Tabs
        PrimaryTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == DirectoryTab.ADVOCATES,
                onClick = { onTabSelected(DirectoryTab.ADVOCATES) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Advocates ($advocateCount)")
                    }
                },
                modifier = Modifier.testTag("tab_advocates")
            )

            Tab(
                selected = selectedTab == DirectoryTab.CLERKS,
                onClick = { onTabSelected(DirectoryTab.CLERKS) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clerk View ($clerkCount)")
                    }
                },
                modifier = Modifier.testTag("tab_clerks")
            )
        }
    }
}

@Preview(name = "Directory Top Bar Preview", showBackground = true)
@Composable
fun DirectoryTopBarPreview() {
    MyApplicationTheme {
        DirectoryTopBar(
            searchQuery = "Amin",
            onSearchQueryChanged = {},
            selectedTab = DirectoryTab.ADVOCATES,
            onTabSelected = {},
            advocateCount = 1143,
            clerkCount = 68,
            onOpenDrawer = {},
            onOpenSettings = {},
            onShareApp = {},
            onExportCsv = {},
            onResetData = {}
        )
    }
}
