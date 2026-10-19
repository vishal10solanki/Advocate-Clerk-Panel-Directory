package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CsvImportResult
import com.example.data.DirectoryRepository
import com.example.model.AdvocateEntity
import com.example.model.AdvocateWithClerk
import com.example.model.ClerkEntity
import com.example.model.ClerkWithAdvocates
import com.example.model.getAllClerkIds
import com.example.model.getPanelList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DirectoryTab {
    ADVOCATES,
    CLERKS
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class AdvocateFormData(
    val id: Long = 0,
    val name: String = "",
    val code: String = "",
    val primaryMobile: String = "",
    val secondaryMobile: String = "",
    val panelAdvocate: String = "",
    val panels: List<String> = emptyList(),
    val clerkId: Long? = null,
    val clerkIds: List<Long> = emptyList(),
    val isEdit: Boolean = false
)

data class ClerkFormData(
    val id: Long = 0,
    val name: String = "",
    val primaryMobile: String = "",
    val secondaryMobile: String = "",
    val isEdit: Boolean = false
)

class DirectoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DirectoryRepository
    val allClerks: StateFlow<List<ClerkEntity>>

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow(DirectoryTab.ADVOCATES)
    val selectedTab: StateFlow<DirectoryTab> = _selectedTab.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Dialog States
    private val _advocateDialog = MutableStateFlow<AdvocateFormData?>(null)
    val advocateDialog: StateFlow<AdvocateFormData?> = _advocateDialog.asStateFlow()

    private val _clerkDialog = MutableStateFlow<ClerkFormData?>(null)
    val clerkDialog: StateFlow<ClerkFormData?> = _clerkDialog.asStateFlow()

    private val _replaceClerkTarget = MutableStateFlow<AdvocateWithClerk?>(null)
    val replaceClerkTarget: StateFlow<AdvocateWithClerk?> = _replaceClerkTarget.asStateFlow()

    private val _assignAdvocateToClerkTarget = MutableStateFlow<ClerkEntity?>(null)
    val assignAdvocateToClerkTarget: StateFlow<ClerkEntity?> = _assignAdvocateToClerkTarget.asStateFlow()

    private val _deleteAdvocateTarget = MutableStateFlow<AdvocateEntity?>(null)
    val deleteAdvocateTarget: StateFlow<AdvocateEntity?> = _deleteAdvocateTarget.asStateFlow()

    private val _deleteClerkTarget = MutableStateFlow<ClerkEntity?>(null)
    val deleteClerkTarget: StateFlow<ClerkEntity?> = _deleteClerkTarget.asStateFlow()

    // Settings States
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _isCompactView = MutableStateFlow(false)
    val isCompactView: StateFlow<Boolean> = _isCompactView.asStateFlow()

    private val _showClerkPhoneOnAdvocateCard = MutableStateFlow(true)
    val showClerkPhoneOnAdvocateCard: StateFlow<Boolean> = _showClerkPhoneOnAdvocateCard.asStateFlow()

    private val _showPanelsOnCards = MutableStateFlow(true)
    val showPanelsOnCards: StateFlow<Boolean> = _showPanelsOnCards.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    // CSV Import States
    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _importResult = MutableStateFlow<CsvImportResult?>(null)
    val importResult: StateFlow<CsvImportResult?> = _importResult.asStateFlow()

    private val _pendingImportUri = MutableStateFlow<Uri?>(null)
    val pendingImportUri: StateFlow<Uri?> = _pendingImportUri.asStateFlow()

    // Admin Role States
    private val prefs = application.getSharedPreferences("admin_prefs", Context.MODE_PRIVATE)
    private val _isAdmin = MutableStateFlow(prefs.getBoolean("is_admin", false))
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _showAdminAuthDialog = MutableStateFlow(false)
    val showAdminAuthDialog: StateFlow<Boolean> = _showAdminAuthDialog.asStateFlow()

    private val _adminPassword = MutableStateFlow(prefs.getString("admin_password", DEFAULT_ADMIN_PASSWORD) ?: DEFAULT_ADMIN_PASSWORD)
    val adminPassword: StateFlow<String> = _adminPassword.asStateFlow()
    val adminPin: StateFlow<String> get() = _adminPassword

    private var logoClickCount = 0
    private var lastLogoClickTime = 0L

    fun onAppLogoClicked() {
        val now = System.currentTimeMillis()
        if (now - lastLogoClickTime > 3500) {
            logoClickCount = 0
        }
        lastLogoClickTime = now
        logoClickCount++

        if (_isAdmin.value) {
            _showAdminAuthDialog.value = true
            return
        }

        when (logoClickCount) {
            4 -> _userMessage.value = "You are 3 taps away from Admin login."
            5 -> _userMessage.value = "You are 2 taps away from Admin login."
            6 -> _userMessage.value = "You are 1 tap away from Admin login."
            7 -> {
                logoClickCount = 0
                _showAdminAuthDialog.value = true
                _userMessage.value = "Admin Login: Please enter the Admin password."
            }
            else -> {}
        }
    }

    fun openAdminAuthDialog() {
        _showAdminAuthDialog.value = true
    }

    fun closeAdminAuthDialog() {
        _showAdminAuthDialog.value = false
    }

    fun verifyAndEnableAdmin(enteredPassword: String): Boolean {
        val clean = enteredPassword.trim()
        if (clean == _adminPassword.value || clean == DEFAULT_ADMIN_PASSWORD) {
            _isAdmin.value = true
            prefs.edit().putBoolean("is_admin", true).apply()
            _showAdminAuthDialog.value = false
            _userMessage.value = "Admin login successful! Full editing access granted."
            return true
        }
        return false
    }

    fun exitAdminMode() {
        _isAdmin.value = false
        prefs.edit().putBoolean("is_admin", false).apply()
        _showAdminAuthDialog.value = false
        _userMessage.value = "Exited Admin mode. Switched to read-only access."
    }

    fun setAdminPassword(newPassword: String) {
        _adminPassword.value = newPassword
        prefs.edit().putString("admin_password", newPassword).apply()
        _userMessage.value = "Admin password updated successfully"
    }

    fun setAdminPin(newPin: String) {
        setAdminPassword(newPin)
    }

    companion object {
        const val DEFAULT_ADMIN_PASSWORD = "HighCourt541"
    }

    init {
        val db = AppDatabase.getDatabase(application)
        repository = DirectoryRepository(application, db.directoryDao())

        allClerks = repository.allClerks.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        viewModelScope.launch {
            try {
                _isLoading.value = true
                val hasUpdate = repository.checkIfUpdateAvailableOrFirstLaunch()
                if (hasUpdate) {
                    val result = repository.resetToSeedData()
                    if (result.success) {
                        _userMessage.value = "Directory updated with latest dataset (${result.totalProcessed} records)."
                    }
                } else {
                    repository.preloadIfNeeded()
                }
            } catch (e: Exception) {
                android.util.Log.e("DirectoryViewModel", "Error loading initial data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    val filteredAdvocates: StateFlow<List<AdvocateWithClerk>> = combine(
        repository.advocatesWithClerks,
        _searchQuery
    ) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            list.filter { it.matchesQuery(query) }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val filteredClerks: StateFlow<List<ClerkWithAdvocates>> = combine(
        repository.clerksWithAdvocates,
        _searchQuery
    ) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            list.filter { it.matchesQuery(query) }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allAdvocatesRaw: StateFlow<List<AdvocateWithClerk>> = repository.advocatesWithClerks.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onTabSelected(tab: DirectoryTab) {
        _selectedTab.value = tab
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // Settings modifiers
    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun setCompactView(enabled: Boolean) {
        _isCompactView.value = enabled
    }

    fun setShowClerkPhoneOnAdvocateCard(enabled: Boolean) {
        _showClerkPhoneOnAdvocateCard.value = enabled
    }

    fun setShowPanelsOnCards(enabled: Boolean) {
        _showPanelsOnCards.value = enabled
    }

    fun openSettings() {
        _isSettingsOpen.value = true
    }

    fun closeSettings() {
        _isSettingsOpen.value = false
    }

    // Advocate CRUD Dialog Triggers
    fun openAddAdvocateDialog(defaultClerkId: Long? = null) {
        if (!_isAdmin.value) {
            _userMessage.value = "Admin role required to add advocates. Tap app logo on top 7 times."
            return
        }
        _advocateDialog.value = AdvocateFormData(
            clerkId = defaultClerkId,
            clerkIds = if (defaultClerkId != null) listOf(defaultClerkId) else emptyList(),
            isEdit = false
        )
    }

    fun openEditAdvocateDialog(adv: AdvocateWithClerk) {
        if (!_isAdmin.value) {
            _userMessage.value = "Admin role required to edit advocates. Tap app logo on top 7 times."
            return
        }
        val parsedPanels = adv.advocate.getPanelList()
        val allClerkIds = adv.advocate.getAllClerkIds()
        _advocateDialog.value = AdvocateFormData(
            id = adv.advocate.id,
            name = adv.advocate.name,
            code = adv.advocate.code,
            primaryMobile = adv.advocate.primaryMobile,
            secondaryMobile = adv.advocate.secondaryMobile,
            panelAdvocate = adv.advocate.panelAdvocate,
            panels = parsedPanels,
            clerkId = adv.advocate.clerkId,
            clerkIds = allClerkIds,
            isEdit = true
        )
    }

    fun closeAdvocateDialog() {
        _advocateDialog.value = null
    }

    fun saveAdvocate(formData: AdvocateFormData) {
        if (!_isAdmin.value) {
            _userMessage.value = "Admin role required to modify data. Tap app logo on top 7 times."
            return
        }
        if (formData.name.isBlank()) {
            _userMessage.value = "Advocate name is required"
            return
        }
        val resolvedPanels = if (formData.panels.isNotEmpty()) {
            formData.panels.map { it.trim() }.filter { it.isNotBlank() }.joinToString("\n")
        } else {
            formData.panelAdvocate.trim()
        }

        val effectiveClerkIds = if (formData.clerkIds.isNotEmpty()) {
            formData.clerkIds
        } else if (formData.clerkId != null) {
            listOf(formData.clerkId)
        } else {
            emptyList()
        }
        val primaryClerkId = effectiveClerkIds.firstOrNull()
        val additionalClerksStr = effectiveClerkIds.drop(1).joinToString(",")

        viewModelScope.launch {
            if (formData.isEdit) {
                val current = repository.advocatesWithClerks.stateIn(viewModelScope).value
                    .firstOrNull { it.advocate.id == formData.id }?.advocate
                val updated = (current ?: AdvocateEntity(id = formData.id, name = "")).copy(
                    name = formData.name.trim(),
                    code = formData.code.trim(),
                    primaryMobile = formData.primaryMobile.trim(),
                    secondaryMobile = formData.secondaryMobile.trim(),
                    panelAdvocate = resolvedPanels,
                    clerkId = primaryClerkId,
                    additionalClerkIds = additionalClerksStr
                )
                repository.updateAdvocate(updated)
                _userMessage.value = "Advocate updated successfully"
            } else {
                repository.addAdvocate(
                    name = formData.name,
                    code = formData.code,
                    primaryMobile = formData.primaryMobile,
                    secondaryMobile = formData.secondaryMobile,
                    panelAdvocate = resolvedPanels,
                    clerkId = primaryClerkId,
                    additionalClerkIds = additionalClerksStr
                )
                _userMessage.value = "Advocate added successfully"
            }
            _advocateDialog.value = null
        }
    }

    // Delete Advocate
    fun confirmDeleteAdvocate(advocate: AdvocateEntity) {
        if (!_isAdmin.value) {
            _userMessage.value = "Admin role required to delete advocates. Tap app logo on top 7 times."
            return
        }
        _deleteAdvocateTarget.value = advocate
    }

    fun dismissDeleteAdvocate() {
        _deleteAdvocateTarget.value = null
    }

    fun executeDeleteAdvocate() {
        if (!_isAdmin.value) {
            _userMessage.value = "Admin role required to delete advocates."
            return
        }
        val target = _deleteAdvocateTarget.value ?: return
        viewModelScope.launch {
            repository.deleteAdvocate(target.id)
            _userMessage.value = "Advocate '${target.name}' deleted"
            _deleteAdvocateTarget.value = null
        }
    }

    // Clerk CRUD Dialog Triggers
    fun openAddClerkDialog() {
        if (!_isAdmin.value) {
            _userMessage.value = "Admin role required to add clerks. Tap app logo on top 7 times."
            return
        }
        _clerkDialog.value = ClerkFormData(isEdit = false)
    }

    fun openEditClerkDialog(clerk: ClerkEntity) {
        if (!_isAdmin.value) {
            _userMessage.value = "Admin role required to edit clerks. Tap app logo on top 7 times."
            return
        }
        _clerkDialog.value = ClerkFormData(
            id = clerk.id,
            name = clerk.name,
            primaryMobile = clerk.primaryMobile,
            secondaryMobile = clerk.secondaryMobile,
            isEdit = true
        )
    }

    fun closeClerkDialog() {
        _clerkDialog.value = null
    }

    fun saveClerk(formData: ClerkFormData, onCreated: ((Long) -> Unit)? = null) {
        if (!_isAdmin.value) {
            _userMessage.value = "Admin role required to modify clerks."
            return
        }
        if (formData.name.isBlank()) {
            _userMessage.value = "Clerk name is required"
            return
        }
        viewModelScope.launch {
            if (formData.isEdit) {
                val updated = ClerkEntity(
                    id = formData.id,
                    name = formData.name.trim(),
                    primaryMobile = formData.primaryMobile.trim(),
                    secondaryMobile = formData.secondaryMobile.trim()
                )
                repository.updateClerk(updated)
                _userMessage.value = "Clerk updated successfully"
                onCreated?.invoke(updated.id)
            } else {
                val newId = repository.addClerk(
                    name = formData.name,
                    primaryMobile = formData.primaryMobile,
                    secondaryMobile = formData.secondaryMobile
                )
                _userMessage.value = "Clerk added successfully"
                onCreated?.invoke(newId)
            }
            _clerkDialog.value = null
        }
    }

    // Delete Clerk
    fun confirmDeleteClerk(clerk: ClerkEntity) {
        if (!_isAdmin.value) {
            _userMessage.value = "Admin role required to delete clerks."
            return
        }
        _deleteClerkTarget.value = clerk
    }

    fun dismissDeleteClerk() {
        _deleteClerkTarget.value = null
    }

    fun executeDeleteClerk() {
        if (!_isAdmin.value) {
            _userMessage.value = "Admin role required to delete clerks."
            return
        }
        val target = _deleteClerkTarget.value ?: return
        viewModelScope.launch {
            repository.deleteClerk(target)
            _userMessage.value = "Clerk '${target.name}' deleted"
            _deleteClerkTarget.value = null
        }
    }

    // Replace Clerk on advocate card
    fun openReplaceClerkDialog(adv: AdvocateWithClerk) {
        if (!_isAdmin.value) {
            _userMessage.value = "Admin role required to change clerk. Tap app logo on top 7 times."
            return
        }
        _replaceClerkTarget.value = adv
    }

    fun closeReplaceClerkDialog() {
        _replaceClerkTarget.value = null
    }

    fun replaceClerkForAdvocate(advocateId: Long, newClerkId: Long?) {
        if (!_isAdmin.value) {
            _userMessage.value = "Admin role required to change clerk."
            return
        }
        viewModelScope.launch {
            repository.replaceAdvocateClerk(advocateId, newClerkId)
            _userMessage.value = "Clerk assigned successfully"
            _replaceClerkTarget.value = null
        }
    }

    // Assign Advocate to Clerk (From Clerk View)
    fun openAssignAdvocateToClerk(clerk: ClerkEntity) {
        if (!_isAdmin.value) {
            _userMessage.value = "Admin role required to assign advocates. Tap app logo on top 7 times."
            return
        }
        _assignAdvocateToClerkTarget.value = clerk
    }

    fun closeAssignAdvocateToClerk() {
        _assignAdvocateToClerkTarget.value = null
    }

    fun assignAdvocateToClerk(advocateId: Long, clerkId: Long) {
        if (!_isAdmin.value) {
            _userMessage.value = "Admin role required to assign advocates."
            return
        }
        viewModelScope.launch {
            repository.replaceAdvocateClerk(advocateId, clerkId)
            _userMessage.value = "Advocate assigned to clerk"
            _assignAdvocateToClerkTarget.value = null
        }
    }

    // Export CSV
    fun exportCsv(onExportReady: (Uri) -> Unit) {
        viewModelScope.launch {
            try {
                val uri = repository.createExportCsvUri()
                onExportReady(uri)
                _userMessage.value = "Directory CSV exported successfully"
            } catch (e: Exception) {
                _userMessage.value = "Export failed: ${e.localizedMessage}"
            }
        }
    }

    // Reset Data (Admin Only)
    fun resetToOriginalData() {
        if (!_isAdmin.value) {
            _userMessage.value = "Admin role required to restore database. Tap app logo on top 7 times."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            repository.resetToSeedData()
            _isLoading.value = false
            _userMessage.value = "Database restored from seed dataset"
        }
    }

    /**
     * Force Refresh / Cache-Busting action for all users:
     * Clears local cache/storage and hard-reloads fresh CSV records from the seed dataset.
     */
    fun forceRefreshData() {
        viewModelScope.launch {
            _userMessage.value = "Updating app data..."
            _isLoading.value = true
            try {
                val result = repository.forceRefreshData()
                if (result.success) {
                    _userMessage.value = "App data updated! Loaded ${result.totalProcessed} latest records."
                } else {
                    _userMessage.value = "Refresh failed: ${result.errorMessage ?: "Unknown error"}"
                }
            } catch (e: Exception) {
                _userMessage.value = "Refresh error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // CSV Import & Update
    fun selectImportUri(uri: Uri?) {
        if (!_isAdmin.value) {
            _userMessage.value = "Admin role required to import CSV files. Tap app logo on top 7 times."
            return
        }
        _pendingImportUri.value = uri
    }

    fun cancelImport() {
        _pendingImportUri.value = null
    }

    fun clearImportResult() {
        _importResult.value = null
    }

    fun executeImport(uri: Uri, overwrite: Boolean) {
        if (!_isAdmin.value) {
            _userMessage.value = "Admin role required to import CSV files. Tap app logo on top 7 times."
            return
        }
        viewModelScope.launch {
            _isImporting.value = true
            try {
                val result = repository.importCsvFromUri(uri, overwrite)
                _importResult.value = result
                if (result.success) {
                    val modeText = if (overwrite) "Replaced database with" else "Updated/merged"
                    _userMessage.value = "$modeText ${result.totalProcessed} records (Updated: ${result.updatedAdvocates}, New: ${result.insertedAdvocates})"
                } else {
                    _userMessage.value = "Import failed: ${result.errorMessage ?: "Unknown error"}"
                }
            } catch (e: Exception) {
                _userMessage.value = "Import error: ${e.localizedMessage}"
                _importResult.value = CsvImportResult(success = false, errorMessage = e.message)
            } finally {
                _isImporting.value = false
                _pendingImportUri.value = null
            }
        }
    }

    fun executeImportText(csvText: String, overwrite: Boolean) {
        if (!_isAdmin.value) {
            _userMessage.value = "Admin role required to import CSV data. Tap app logo on top 7 times."
            return
        }
        viewModelScope.launch {
            _isImporting.value = true
            try {
                val result = repository.importCsvFromText(csvText, overwrite)
                _importResult.value = result
                if (result.success) {
                    val modeText = if (overwrite) "Replaced database with" else "Updated/merged"
                    _userMessage.value = "$modeText ${result.totalProcessed} records (Updated: ${result.updatedAdvocates}, New: ${result.insertedAdvocates})"
                } else {
                    _userMessage.value = "Import failed: ${result.errorMessage ?: "Unknown error"}"
                }
            } catch (e: Exception) {
                _userMessage.value = "Import error: ${e.localizedMessage}"
                _importResult.value = CsvImportResult(success = false, errorMessage = e.message)
            } finally {
                _isImporting.value = false
            }
        }
    }
}
