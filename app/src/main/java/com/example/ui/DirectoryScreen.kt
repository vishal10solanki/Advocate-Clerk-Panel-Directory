package com.example.ui

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AdvocateEntity
import com.example.model.AdvocateWithClerk
import com.example.model.ClerkEntity
import com.example.model.ClerkWithAdvocates
import com.example.ui.components.AdvocateCard
import com.example.ui.components.ClerkCard
import com.example.ui.components.DirectoryTopBar
import com.example.ui.dialogs.AdminAuthDialog
import com.example.ui.dialogs.AdvocateEditDialog
import com.example.ui.dialogs.AssignAdvocateDialog
import com.example.ui.dialogs.ClerkEditDialog
import com.example.ui.dialogs.ConfirmDeleteDialog
import com.example.ui.dialogs.CsvImportConfirmDialog
import com.example.ui.dialogs.CsvImportResultDialog
import com.example.ui.dialogs.ReplaceClerkDialog
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

@Composable
fun DirectoryScreen(
    viewModel: DirectoryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val filteredAdvocates by viewModel.filteredAdvocates.collectAsState()
    val filteredClerks by viewModel.filteredClerks.collectAsState()
    val allAdvocatesRaw by viewModel.allAdvocatesRaw.collectAsState()
    val allClerks by viewModel.allClerks.collectAsState()

    // Settings States
    val themeMode by viewModel.themeMode.collectAsState()
    val isCompactView by viewModel.isCompactView.collectAsState()
    val showClerkPhoneOnAdvocateCard by viewModel.showClerkPhoneOnAdvocateCard.collectAsState()
    val showPanelsOnCards by viewModel.showPanelsOnCards.collectAsState()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()

    // Import CSV States
    val isImporting by viewModel.isImporting.collectAsState()
    val importResult by viewModel.importResult.collectAsState()
    val pendingImportUri by viewModel.pendingImportUri.collectAsState()

    // Admin Role States
    val isAdmin by viewModel.isAdmin.collectAsState()
    val showAdminAuthDialog by viewModel.showAdminAuthDialog.collectAsState()

    val drawerCsvPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.selectImportUri(it) }
    }

    val totalPanelAdvocatesCount = remember(allAdvocatesRaw) {
        allAdvocatesRaw.count { it.advocate.panelAdvocate.isNotBlank() }
    }

    // Dialog states
    val advocateDialogData by viewModel.advocateDialog.collectAsState()
    val clerkDialogData by viewModel.clerkDialog.collectAsState()
    val replaceClerkTarget by viewModel.replaceClerkTarget.collectAsState()
    val assignAdvocateTarget by viewModel.assignAdvocateToClerkTarget.collectAsState()
    val deleteAdvocateTarget by viewModel.deleteAdvocateTarget.collectAsState()
    val deleteClerkTarget by viewModel.deleteClerkTarget.collectAsState()

    var showResetConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearUserMessage()
        }
    }

    if (isSettingsOpen) {
        // Dedicated In-App Settings Screen
        SettingsScreen(
            themeMode = themeMode,
            onThemeModeChange = viewModel::setThemeMode,
            isCompactView = isCompactView,
            onCompactViewChange = viewModel::setCompactView,
            showClerkPhoneOnAdvocateCard = showClerkPhoneOnAdvocateCard,
            onShowClerkPhoneChange = viewModel::setShowClerkPhoneOnAdvocateCard,
            showPanelsOnCards = showPanelsOnCards,
            onShowPanelsChange = viewModel::setShowPanelsOnCards,
            totalAdvocatesCount = allAdvocatesRaw.size,
            totalClerksCount = allClerks.size,
            totalPanelAdvocatesCount = totalPanelAdvocatesCount,
            onAddNewAdvocate = { viewModel.openAddAdvocateDialog() },
            onAddNewClerk = { viewModel.openAddClerkDialog() },
            onExportCsv = {
                viewModel.exportCsv { uri ->
                    DirectoryActions.shareExportedCsv(context, uri)
                }
            },
            onSelectImportCsv = { uri -> viewModel.selectImportUri(uri) },
            isImporting = isImporting,
            onImportCsvText = { text, overwrite -> viewModel.executeImportText(text, overwrite) },
            onResetData = { showResetConfirmation = true },
            onForceRefreshData = { viewModel.forceRefreshData() },
            onBack = { viewModel.closeSettings() },
            isAdmin = isAdmin,
            onOpenAdminAuth = { viewModel.openAdminAuthDialog() },
            onExitAdmin = { viewModel.exitAdminMode() }
        )
    } else {
        // Main Screen with Three-Lined Navigation Drawer
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = MaterialTheme.colorScheme.surface,
                    drawerTonalElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Gavel,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Legal Bar & Clerks Register",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Spacer(modifier = Modifier.height(8.dp))

                    // 1. Settings Option (Requested in three-lined button)
                    NavigationDrawerItem(
                        label = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            viewModel.openSettings()
                        },
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .testTag("drawer_item_settings")
                    )

                    // 2. Share App Option (Requested in three-lined button)
                    NavigationDrawerItem(
                        label = { Text("Share App", fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.Share, contentDescription = "Share App") },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            DirectoryActions.shareApp(context)
                        },
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .testTag("drawer_item_share_app")
                    )

                    // 3. 🔄 Refresh Data Option (Force Update & Clear Cache)
                    NavigationDrawerItem(
                        label = { Text("🔄 Refresh Data", fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.Refresh, contentDescription = "Refresh Data", tint = MaterialTheme.colorScheme.primary) },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            viewModel.forceRefreshData()
                        },
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .testTag("drawer_item_refresh_data")
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                    )

                    // 3. Advocate Directory
                    NavigationDrawerItem(
                        label = { Text("Advocates (${filteredAdvocates.size})") },
                        icon = { Icon(Icons.Default.People, contentDescription = null) },
                        selected = selectedTab == DirectoryTab.ADVOCATES,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            viewModel.onTabSelected(DirectoryTab.ADVOCATES)
                        },
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .testTag("drawer_item_advocates")
                    )

                    // 4. Clerk Directory
                    NavigationDrawerItem(
                        label = { Text("Clerk View (${filteredClerks.size})") },
                        icon = { Icon(Icons.Default.SupportAgent, contentDescription = null) },
                        selected = selectedTab == DirectoryTab.CLERKS,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            viewModel.onTabSelected(DirectoryTab.CLERKS)
                        },
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .testTag("drawer_item_clerks")
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                    )

                    // Admin Role Section in Drawer (only shown when admin is active)
                    if (isAdmin) {
                        NavigationDrawerItem(
                            label = { Text("Admin Active (Tap to Exit)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                            icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            selected = true,
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                viewModel.openAdminAuthDialog()
                            },
                            modifier = Modifier
                                .padding(NavigationDrawerItemDefaults.ItemPadding)
                                .testTag("drawer_item_admin_status")
                        )
                    }

                    // 5. Export CSV
                    NavigationDrawerItem(
                        label = { Text("Export CSV Backup") },
                        icon = { Icon(Icons.Default.FileDownload, contentDescription = null) },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            viewModel.exportCsv { uri ->
                                DirectoryActions.shareExportedCsv(context, uri)
                            }
                        },
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .testTag("drawer_item_export_csv")
                    )

                    // 6. Update / Import CSV (Admin Only)
                    if (isAdmin) {
                        NavigationDrawerItem(
                            label = { Text("Update / Import CSV") },
                            icon = { Icon(Icons.Default.FileUpload, contentDescription = null) },
                            selected = false,
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                drawerCsvPickerLauncher.launch(
                                    arrayOf("text/csv", "text/comma-separated-values", "text/*", "*/*")
                                )
                            },
                            modifier = Modifier
                                .padding(NavigationDrawerItemDefaults.ItemPadding)
                                .testTag("drawer_item_import_csv")
                        )

                        // 7. Reset Data (Admin Only)
                        NavigationDrawerItem(
                            label = { Text("Reload Seed Data") },
                            icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                            selected = false,
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                showResetConfirmation = true
                            },
                            modifier = Modifier
                                .padding(NavigationDrawerItemDefaults.ItemPadding)
                                .testTag("drawer_item_reset_data")
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "Adv & Clerk Directory • v1.0.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 24.dp, bottom = 24.dp)
                    )
                }
            }
        ) {
            DirectoryContent(
                searchQuery = searchQuery,
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                selectedTab = selectedTab,
                onTabSelected = viewModel::onTabSelected,
                isLoading = isLoading,
                filteredAdvocates = filteredAdvocates,
                filteredClerks = filteredClerks,
                isCompactView = isCompactView,
                showClerkPhoneOnAdvocateCard = showClerkPhoneOnAdvocateCard,
                showPanelsOnCards = showPanelsOnCards,
                onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                onOpenSettings = { viewModel.openSettings() },
                onShareApp = { DirectoryActions.shareApp(context) },
                onExportCsv = {
                    viewModel.exportCsv { uri ->
                        DirectoryActions.shareExportedCsv(context, uri)
                    }
                },
                onResetData = { showResetConfirmation = true },
                onRefreshData = { viewModel.forceRefreshData() },
                onOpenAdminAuth = { viewModel.openAdminAuthDialog() },
                onAddEntry = {
                    if (selectedTab == DirectoryTab.ADVOCATES) {
                        viewModel.openAddAdvocateDialog()
                    } else {
                        viewModel.openAddClerkDialog()
                    }
                },
                onEditAdvocate = { viewModel.openEditAdvocateDialog(it) },
                onDeleteAdvocate = { viewModel.confirmDeleteAdvocate(it) },
                onReplaceClerk = { viewModel.openReplaceClerkDialog(it) },
                onEditClerk = { viewModel.openEditClerkDialog(it) },
                onDeleteClerk = { viewModel.confirmDeleteClerk(it) },
                onAddAdvocateToClerk = { viewModel.openAssignAdvocateToClerk(it) },
                onEditAdvocateFromClerkView = { advId ->
                    val adv = allAdvocatesRaw.firstOrNull { it.advocate.id == advId }
                    if (adv != null) viewModel.openEditAdvocateDialog(adv)
                },
                allClerks = allClerks,
                isAdmin = isAdmin,
                onLogoClick = { viewModel.onAppLogoClicked() },
                snackbarHostState = snackbarHostState,
                modifier = modifier
            )
        }
    }

    // Advocate Add / Edit Modal
    advocateDialogData?.let { formData ->
        AdvocateEditDialog(
            initialData = formData,
            availableClerks = allClerks,
            onSave = { updatedData -> viewModel.saveAdvocate(updatedData) },
            onOpenAddClerk = { viewModel.openAddClerkDialog() },
            onDismiss = { viewModel.closeAdvocateDialog() }
        )
    }

    // Clerk Add / Edit Modal
    clerkDialogData?.let { formData ->
        ClerkEditDialog(
            initialData = formData,
            onSave = { updatedData -> viewModel.saveClerk(updatedData) },
            onDismiss = { viewModel.closeClerkDialog() }
        )
    }

    // Replace Clerk Dialog on Advocate Card
    replaceClerkTarget?.let { target ->
        ReplaceClerkDialog(
            targetAdvocate = target,
            availableClerks = allClerks,
            onSelectClerk = { newClerkId ->
                viewModel.replaceClerkForAdvocate(target.advocate.id, newClerkId)
            },
            onCreateNewClerk = {
                viewModel.closeReplaceClerkDialog()
                viewModel.openAddClerkDialog()
            },
            onDismiss = { viewModel.closeReplaceClerkDialog() }
        )
    }

    // Assign Advocate to Clerk Dialog (from Clerk View)
    assignAdvocateTarget?.let { targetClerk ->
        AssignAdvocateDialog(
            targetClerk = targetClerk,
            allAdvocates = allAdvocatesRaw,
            onAssignAdvocate = { advocateId ->
                viewModel.assignAdvocateToClerk(advocateId, targetClerk.id)
            },
            onCreateNewAdvocate = {
                viewModel.closeAssignAdvocateToClerk()
                viewModel.openAddAdvocateDialog(defaultClerkId = targetClerk.id)
            },
            onDismiss = { viewModel.closeAssignAdvocateToClerk() }
        )
    }

    // Confirm Delete Advocate
    deleteAdvocateTarget?.let { advocate ->
        ConfirmDeleteDialog(
            title = "Delete Advocate",
            message = "Are you sure you want to delete '${advocate.name}'? This action cannot be undone.",
            onConfirm = { viewModel.executeDeleteAdvocate() },
            onDismiss = { viewModel.dismissDeleteAdvocate() }
        )
    }

    // Confirm Delete Clerk
    deleteClerkTarget?.let { clerk ->
        ConfirmDeleteDialog(
            title = "Delete Clerk",
            message = "Are you sure you want to delete clerk '${clerk.name}'? Advocates assigned to this clerk will become unassigned.",
            onConfirm = { viewModel.executeDeleteClerk() },
            onDismiss = { viewModel.dismissDeleteClerk() }
        )
    }

    // Confirm Reset Data
    if (showResetConfirmation) {
        ConfirmDeleteDialog(
            title = "Restore Original Dataset",
            message = "This will reload the 1,180 records from the seed CSV and overwrite any manual edits. Do you want to proceed?",
            onConfirm = {
                showResetConfirmation = false
                viewModel.resetToOriginalData()
            },
            onDismiss = { showResetConfirmation = false }
        )
    }

    // CSV Import Confirmation Dialog
    pendingImportUri?.let { uri ->
        CsvImportConfirmDialog(
            selectedUri = uri,
            isImporting = isImporting,
            onConfirmImport = { overwrite ->
                viewModel.executeImport(uri, overwrite)
            },
            onDismiss = { viewModel.cancelImport() }
        )
    }

    // CSV Import Result Dialog
    importResult?.let { result ->
        CsvImportResultDialog(
            result = result,
            onDismiss = { viewModel.clearImportResult() }
        )
    }

    // Admin Authentication / Status Dialog
    if (showAdminAuthDialog) {
        AdminAuthDialog(
            isAdmin = isAdmin,
            onUnlock = { pin -> viewModel.verifyAndEnableAdmin(pin) },
            onExitAdmin = { viewModel.exitAdminMode() },
            onDismiss = { viewModel.closeAdminAuthDialog() }
        )
    }
}

@Composable
fun DirectoryContent(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    selectedTab: DirectoryTab,
    onTabSelected: (DirectoryTab) -> Unit,
    isLoading: Boolean,
    filteredAdvocates: List<AdvocateWithClerk>,
    filteredClerks: List<ClerkWithAdvocates>,
    isCompactView: Boolean = false,
    showClerkPhoneOnAdvocateCard: Boolean = true,
    showPanelsOnCards: Boolean = true,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onShareApp: () -> Unit,
    onExportCsv: () -> Unit,
    onResetData: () -> Unit,
    onRefreshData: () -> Unit = {},
    onOpenAdminAuth: () -> Unit = {},
    onAddEntry: () -> Unit,
    onEditAdvocate: (AdvocateWithClerk) -> Unit,
    onDeleteAdvocate: (AdvocateEntity) -> Unit,
    onReplaceClerk: (AdvocateWithClerk) -> Unit,
    onEditClerk: (ClerkEntity) -> Unit,
    onDeleteClerk: (ClerkEntity) -> Unit,
    onAddAdvocateToClerk: (ClerkEntity) -> Unit,
    onEditAdvocateFromClerkView: (Long) -> Unit,
    allClerks: List<ClerkEntity> = emptyList(),
    isAdmin: Boolean = false,
    onLogoClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier
) {
    val advocateListState = rememberLazyListState()
    val clerkListState = rememberLazyListState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            DirectoryTopBar(
                searchQuery = searchQuery,
                onSearchQueryChanged = onSearchQueryChanged,
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                advocateCount = filteredAdvocates.size,
                clerkCount = filteredClerks.size,
                onOpenDrawer = onOpenDrawer,
                onOpenSettings = onOpenSettings,
                onShareApp = onShareApp,
                onExportCsv = onExportCsv,
                onResetData = onResetData,
                onRefreshData = onRefreshData,
                onOpenAdminAuth = onOpenAdminAuth,
                isAdmin = isAdmin,
                onLogoClick = onLogoClick
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                ExtendedFloatingActionButton(
                    onClick = onAddEntry,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = {
                        Text(
                            if (selectedTab == DirectoryTab.ADVOCATES) "Add Advocate" else "Add Clerk"
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_add_entry")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading Directory Database...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                when (selectedTab) {
                    DirectoryTab.ADVOCATES -> {
                        if (filteredAdvocates.isEmpty()) {
                            EmptySearchResultView(
                                query = searchQuery,
                                message = if (searchQuery.isBlank()) "No advocates available in the directory."
                                else "No advocates found matching '$searchQuery'"
                            )
                        } else {
                            LazyColumn(
                                state = advocateListState,
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                                verticalArrangement = Arrangement.spacedBy(if (isCompactView) 8.dp else 12.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("advocates_list")
                            ) {
                                items(filteredAdvocates, key = { it.advocate.id }) { item ->
                                    AdvocateCard(
                                        item = item,
                                        onEdit = { onEditAdvocate(item) },
                                        onDelete = { onDeleteAdvocate(item.advocate) },
                                        onReplaceClerk = { onReplaceClerk(item) },
                                        isCompact = isCompactView,
                                        showClerkPhone = showClerkPhoneOnAdvocateCard,
                                        showPanels = showPanelsOnCards,
                                        isAdmin = isAdmin,
                                        allClerks = allClerks,
                                        searchQuery = searchQuery
                                    )
                                }
                            }
                        }
                    }

                    DirectoryTab.CLERKS -> {
                        if (filteredClerks.isEmpty()) {
                            EmptySearchResultView(
                                query = searchQuery,
                                message = if (searchQuery.isBlank()) "No clerks available in the directory."
                                else "No clerks found matching '$searchQuery'"
                            )
                        } else {
                            LazyColumn(
                                state = clerkListState,
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                                verticalArrangement = Arrangement.spacedBy(if (isCompactView) 8.dp else 12.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("clerks_list")
                            ) {
                                items(filteredClerks, key = { it.clerk.id }) { item ->
                                    ClerkCard(
                                        item = item,
                                        onEditClerk = { onEditClerk(item.clerk) },
                                        onDeleteClerk = { onDeleteClerk(item.clerk) },
                                        onAddAdvocateToClerk = { onAddAdvocateToClerk(item.clerk) },
                                        onEditAdvocate = onEditAdvocateFromClerkView,
                                        isCompact = isCompactView,
                                        isAdmin = isAdmin
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

@Composable
fun EmptySearchResultView(
    query: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.PersonSearch,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (query.isBlank()) "No Records Found" else "No Matching Results",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(name = "Directory Screen Light", showBackground = true)
@Composable
fun DirectoryScreenLightPreview() {
    val sampleAdvocate = AdvocateWithClerk(
        advocate = AdvocateEntity(
            id = 1,
            name = "NITIN M AMIN",
            code = "126",
            primaryMobile = "9825049155",
            secondaryMobile = "9825049156",
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

    val sampleClerk = ClerkWithAdvocates(
        clerk = ClerkEntity(
            id = 1,
            name = "PRADIPBHAI S SHUKLA",
            primaryMobile = "9825049155",
            secondaryMobile = "9825049156"
        ),
        advocates = listOf(sampleAdvocate.advocate)
    )

    MyApplicationTheme {
        DirectoryContent(
            searchQuery = "",
            onSearchQueryChanged = {},
            selectedTab = DirectoryTab.ADVOCATES,
            onTabSelected = {},
            isLoading = false,
            filteredAdvocates = listOf(sampleAdvocate),
            filteredClerks = listOf(sampleClerk),
            isCompactView = false,
            showClerkPhoneOnAdvocateCard = true,
            showPanelsOnCards = true,
            onOpenDrawer = {},
            onOpenSettings = {},
            onShareApp = {},
            onExportCsv = {},
            onResetData = {},
            onAddEntry = {},
            onEditAdvocate = {},
            onDeleteAdvocate = {},
            onReplaceClerk = {},
            onEditClerk = {},
            onDeleteClerk = {},
            onAddAdvocateToClerk = {},
            onEditAdvocateFromClerkView = {}
        )
    }
}

@Preview(name = "Directory Screen Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun DirectoryScreenDarkPreview() {
    val sampleAdvocate = AdvocateWithClerk(
        advocate = AdvocateEntity(
            id = 1,
            name = "SANJAY M AMIN",
            code = "127",
            primaryMobile = "9825049157",
            secondaryMobile = "",
            clerkId = 1,
            initialSrNo = 2
        ),
        clerk = ClerkEntity(
            id = 1,
            name = "PRADIPBHAI S SHUKLA",
            primaryMobile = "9825049155",
            secondaryMobile = "9825049156"
        )
    )

    MyApplicationTheme(darkTheme = true) {
        DirectoryContent(
            searchQuery = "Amin",
            onSearchQueryChanged = {},
            selectedTab = DirectoryTab.ADVOCATES,
            onTabSelected = {},
            isLoading = false,
            filteredAdvocates = listOf(sampleAdvocate),
            filteredClerks = emptyList(),
            isCompactView = false,
            showClerkPhoneOnAdvocateCard = true,
            showPanelsOnCards = true,
            onOpenDrawer = {},
            onOpenSettings = {},
            onShareApp = {},
            onExportCsv = {},
            onResetData = {},
            onAddEntry = {},
            onEditAdvocate = {},
            onDeleteAdvocate = {},
            onReplaceClerk = {},
            onEditClerk = {},
            onDeleteClerk = {},
            onAddAdvocateToClerk = {},
            onEditAdvocateFromClerkView = {}
        )
    }
}
