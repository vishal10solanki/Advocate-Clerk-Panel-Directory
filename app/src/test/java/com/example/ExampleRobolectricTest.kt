package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.example.model.getAllClerkIds
import com.example.model.getPanelList

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read app name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Adv & Clerk Directory", appName)
  }

  @Test
  fun `test directory viewmodel and repository preload`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val db = com.example.data.AppDatabase.getDatabase(app)
    val dao = db.directoryDao()
    val repo = com.example.data.DirectoryRepository(app, dao)
    dao.clearAdvocates()
    dao.clearClerks()
    repo.preloadIfNeeded()
    val afterCount = dao.getAdvocateCount()
    assertEquals(1638, afterCount)
  }

  @Test
  fun `launch MainActivity test`() {
    val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
    val activity = controller.get()
    org.junit.Assert.assertNotNull(activity)
  }

  @Test
  fun `test advocate search does not match clerk details`() {
    val clerk = com.example.model.ClerkEntity(
      id = 5,
      name = "Ramesh Kumar Clerk",
      primaryMobile = "9825112233"
    )
    val adv = com.example.model.AdvocateWithClerk(
      advocate = com.example.model.AdvocateEntity(
        id = 1,
        name = "Kunal Sharma",
        code = "KS/101",
        primaryMobile = "9876543210",
        panelAdvocate = "State Bank of India (SBI) Panel",
        clerkId = 5
      ),
      clerk = clerk
    )

    // Advocate search should match advocate name, code, panel
    org.junit.Assert.assertTrue(adv.matchesQuery("Kunal"))
    org.junit.Assert.assertTrue(adv.matchesQuery("KS/101"))
    org.junit.Assert.assertTrue(adv.matchesQuery("SBI"))

    // Advocate search must NOT match mobile numbers or clerk details
    org.junit.Assert.assertFalse(adv.matchesQuery("9876543210"))
    org.junit.Assert.assertFalse(adv.matchesQuery("Ramesh"))
    org.junit.Assert.assertFalse(adv.matchesQuery("9825112233"))
  }

  @Test
  fun `test clerk search does not match advocate details`() {
    val clerk = com.example.model.ClerkEntity(
      id = 5,
      name = "Ramesh Kumar Clerk",
      primaryMobile = "9825112233"
    )
    val adv = com.example.model.AdvocateEntity(
      id = 1,
      name = "Kunal Sharma",
      code = "KS/101",
      primaryMobile = "9876543210",
      clerkId = 5
    )
    val clerkWithAdvocates = com.example.model.ClerkWithAdvocates(
      clerk = clerk,
      advocates = listOf(adv)
    )

    // Clerk search should match clerk name
    org.junit.Assert.assertTrue(clerkWithAdvocates.matchesQuery("Ramesh"))

    // Clerk search must NOT match mobile numbers or advocate details
    org.junit.Assert.assertFalse(clerkWithAdvocates.matchesQuery("9825112233"))
    org.junit.Assert.assertFalse(clerkWithAdvocates.matchesQuery("Kunal"))
    org.junit.Assert.assertFalse(clerkWithAdvocates.matchesQuery("KS/101"))
    org.junit.Assert.assertFalse(clerkWithAdvocates.matchesQuery("9876543210"))
  }

  @Test
  fun `test ready to share message contains advocate and clerk details`() {
    val advocate = com.example.model.AdvocateEntity(
      id = 1,
      name = "Ashok Parekh",
      code = "AP/442",
      primaryMobile = "9898011223",
      secondaryMobile = "9898011224",
      panelAdvocate = "SBI Panel"
    )
    val clerk = com.example.model.ClerkEntity(
      id = 2,
      name = "Dinesh Solanki",
      primaryMobile = "9426055667",
      secondaryMobile = "9426055668"
    )

    val message = com.example.ui.DirectoryActions.buildAdvocateShareMessage(advocate, clerk)

    // Must include advocate name, code, clerk name, and clerk mobile number(s)
    org.junit.Assert.assertTrue(message.contains("Ashok Parekh"))
    org.junit.Assert.assertTrue(message.contains("AP/442"))
    org.junit.Assert.assertTrue(message.contains("Dinesh Solanki"))
    org.junit.Assert.assertTrue(message.contains("9426055667"))
    org.junit.Assert.assertTrue(message.contains("9426055668"))

    // Must NOT include advocate mobile number or panel list
    org.junit.Assert.assertFalse(message.contains("9898011223"))
    org.junit.Assert.assertFalse(message.contains("9898011224"))
    org.junit.Assert.assertFalse(message.contains("SBI Panel"))
  }

  @Test
  fun `test advocate with multiple panels parsing and search`() {
    val adv = com.example.model.AdvocateEntity(
      id = 1,
      name = "Nitin M Amin",
      code = "126",
      primaryMobile = "9825049155",
      panelAdvocate = "State Bank of India (SBI) Panel\nBank of Baroda Panel\nLife Insurance Corp. (LIC) Panel"
    )

    val panels = adv.getPanelList()
    assertEquals(3, panels.size)
    assertEquals("State Bank of India (SBI) Panel", panels[0])
    assertEquals("Bank of Baroda Panel", panels[1])
    assertEquals("Life Insurance Corp. (LIC) Panel", panels[2])

    val item = com.example.model.AdvocateWithClerk(
      advocate = adv,
      clerk = null
    )

    // Should match any of the individual panels
    org.junit.Assert.assertTrue(item.matchesQuery("SBI"))
    org.junit.Assert.assertTrue(item.matchesQuery("Baroda"))
    org.junit.Assert.assertTrue(item.matchesQuery("LIC"))
    org.junit.Assert.assertTrue(item.matchesQuery("Panel"))
  }

  @Test
  fun `test settings options and in-app controls in DirectoryViewModel`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.DirectoryViewModel(app)

    // Initial default settings
    assertEquals(com.example.ui.ThemeMode.SYSTEM, viewModel.themeMode.value)
    org.junit.Assert.assertFalse(viewModel.isCompactView.value)
    org.junit.Assert.assertTrue(viewModel.showClerkPhoneOnAdvocateCard.value)
    org.junit.Assert.assertTrue(viewModel.showPanelsOnCards.value)
    org.junit.Assert.assertFalse(viewModel.isSettingsOpen.value)

    // Modify settings directly in app
    viewModel.setThemeMode(com.example.ui.ThemeMode.DARK)
    assertEquals(com.example.ui.ThemeMode.DARK, viewModel.themeMode.value)

    viewModel.setCompactView(true)
    org.junit.Assert.assertTrue(viewModel.isCompactView.value)

    viewModel.setShowClerkPhoneOnAdvocateCard(false)
    org.junit.Assert.assertFalse(viewModel.showClerkPhoneOnAdvocateCard.value)

    viewModel.setShowPanelsOnCards(false)
    org.junit.Assert.assertFalse(viewModel.showPanelsOnCards.value)

    viewModel.openSettings()
    org.junit.Assert.assertTrue(viewModel.isSettingsOpen.value)

    viewModel.closeSettings()
    org.junit.Assert.assertFalse(viewModel.isSettingsOpen.value)
  }

  @Test
  fun `test import csv in merge mode updates existing and adds new records`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val db = com.example.data.AppDatabase.getDatabase(app)
    val dao = db.directoryDao()
    val repo = com.example.data.DirectoryRepository(app, dao)

    // Clear and insert 1 existing advocate
    dao.clearAdvocates()
    dao.clearClerks()
    dao.insertAdvocate(
      com.example.model.AdvocateEntity(
        id = 1,
        name = "Nitin M Amin",
        code = "126",
        primaryMobile = "9825049155",
        secondaryMobile = "",
        panelAdvocate = "SBI Panel"
      )
    )

    val csvData = """
      Sr No,Advocate Name,Advocate Code,Panel Advocate,Advocate Primary Mobile,Advocate Secondary Mobile,Clerk Name,Clerk Primary Mobile,Clerk Secondary Mobile
      1,Nitin M Amin,126,SBI Panel & Bank of Baroda,9825049155,9825049156,Pradip Shukla,9988776655,
      2,Brand New Advocate,999,High Court Panel,9123456780,,New Clerk,9111222333,
    """.trimIndent()

    val stream = java.io.ByteArrayInputStream(csvData.toByteArray(Charsets.UTF_8))
    val result = repo.importCsvFromStream(stream, overwrite = false)

    org.junit.Assert.assertTrue(result.success)
    assertEquals(2, result.totalProcessed)
    assertEquals(1, result.updatedAdvocates)
    assertEquals(1, result.insertedAdvocates)

    val updatedAdv = dao.getAllAdvocatesSnapshot().first { it.code == "126" }
    assertEquals("9825049156", updatedAdv.secondaryMobile)
    assertEquals("SBI Panel & Bank of Baroda", updatedAdv.panelAdvocate)

    val newAdv = dao.getAllAdvocatesSnapshot().first { it.code == "999" }
    assertEquals("Brand New Advocate", newAdv.name)
    assertEquals("9123456780", newAdv.primaryMobile)
  }

  @Test
  fun `test import csv in overwrite mode replaces entire database`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val db = com.example.data.AppDatabase.getDatabase(app)
    val dao = db.directoryDao()
    val repo = com.example.data.DirectoryRepository(app, dao)

    dao.clearAdvocates()
    dao.clearClerks()
    dao.insertAdvocate(
      com.example.model.AdvocateEntity(
        id = 1,
        name = "Old Advocate",
        code = "OLD1",
        primaryMobile = "9000000000"
      )
    )

    val csvData = """
      Sr No,Advocate Name,Advocate Code,Panel Advocate,Advocate Primary Mobile,Advocate Secondary Mobile,Clerk Name,Clerk Primary Mobile,Clerk Secondary Mobile
      1,Fresh Advocate A,FA1,LIC Panel,9876543210,,Clerk A,9123456789,
    """.trimIndent()

    val stream = java.io.ByteArrayInputStream(csvData.toByteArray(Charsets.UTF_8))
    val result = repo.importCsvFromStream(stream, overwrite = true)

    org.junit.Assert.assertTrue(result.success)
    assertEquals(1, result.totalProcessed)
    assertEquals(1, result.insertedAdvocates)

    val allAdvs = dao.getAllAdvocatesSnapshot()
    assertEquals(1, allAdvs.size)
    assertEquals("Fresh Advocate A", allAdvs[0].name)
  }

  @Test
  fun `test import csv from text directly in merge mode`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val db = com.example.data.AppDatabase.getDatabase(app)
    val dao = db.directoryDao()
    val repo = com.example.data.DirectoryRepository(app, dao)

    dao.clearAdvocates()
    dao.clearClerks()

    val csvText = """
      sr_no,advocate_name,advocate_code,advocate_mobile_primary,advocate_mobile_secondary,clerk_name,clerk_mobile_primary,clerk_mobile_secondary
      1,Harsh V Patel,HVP/77,9898112233,9898445566,Babubhai Clerk,9426112233,
    """.trimIndent()

    val result = repo.importCsvFromText(csvText, overwrite = false)
    org.junit.Assert.assertTrue(result.success)
    assertEquals(1, result.totalProcessed)
    assertEquals(1, result.insertedAdvocates)

    val adv = dao.getAllAdvocatesSnapshot().first { it.code == "HVP/77" }
    assertEquals("Harsh V Patel", adv.name)
    assertEquals("9898112233", adv.primaryMobile)
    assertEquals("9898445566", adv.secondaryMobile)

    val clerk = dao.getAllClerksSnapshot().first { it.name.contains("Babubhai") }
    assertEquals("9426112233", clerk.primaryMobile)
  }

  @Test
  fun `test admin login password verification with HighCourt541`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.DirectoryViewModel(app)

    // Initially, admin should be false
    viewModel.exitAdminMode()
    org.junit.Assert.assertFalse(viewModel.isAdmin.value)

    // Attempt with wrong password
    val failResult = viewModel.verifyAndEnableAdmin("WrongPassword123")
    org.junit.Assert.assertFalse(failResult)
    org.junit.Assert.assertFalse(viewModel.isAdmin.value)

    // Attempt with correct password "HighCourt541"
    val successResult = viewModel.verifyAndEnableAdmin("HighCourt541")
    org.junit.Assert.assertTrue(successResult)
    org.junit.Assert.assertTrue(viewModel.isAdmin.value)

    // Exit admin mode
    viewModel.exitAdminMode()
    org.junit.Assert.assertFalse(viewModel.isAdmin.value)

    // Attempt with trimmed correct password " HighCourt541 "
    val trimmedSuccess = viewModel.verifyAndEnableAdmin(" HighCourt541 ")
    org.junit.Assert.assertTrue(trimmedSuccess)
    org.junit.Assert.assertTrue(viewModel.isAdmin.value)
  }

  @Test
  fun `test force refresh and cache busting reloads seed dataset`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val db = com.example.data.AppDatabase.getDatabase(app)
    val dao = db.directoryDao()
    val repo = com.example.data.DirectoryRepository(app, dao)

    // Force refresh data
    val result = repo.forceRefreshData()
    org.junit.Assert.assertTrue(result.success)
    org.junit.Assert.assertEquals(1638, dao.getAdvocateCount())

    // Verify checksum is non-empty
    val checksum = repo.getSeedChecksum()
    org.junit.Assert.assertTrue(checksum.isNotBlank())
    org.junit.Assert.assertNotEquals("default_seed_checksum", checksum)
  }

  @Test
  fun `test suggest edit message builder formats correct WhatsApp payload`() {
    val adv = com.example.model.AdvocateEntity(
      id = 1,
      name = "NITIN M AMIN",
      code = "126",
      primaryMobile = "9825049155",
      panelAdvocate = "State Bank of India (SBI) Panel"
    )
    val clerk = com.example.model.ClerkEntity(
      id = 1,
      name = "PRADIPBHAI S SHUKLA",
      primaryMobile = "9825049155"
    )

    val advMsg = com.example.ui.DirectoryActions.buildSuggestEditAdvocateMessage(
      advocate = adv,
      clerk = clerk,
      fieldHint = "Mobile Number"
    )
    org.junit.Assert.assertTrue(advMsg.contains("SUGGESTION FOR ADVOCATE UPDATE"))
    org.junit.Assert.assertTrue(advMsg.contains("NITIN M AMIN"))
    org.junit.Assert.assertTrue(advMsg.contains("126"))
    org.junit.Assert.assertTrue(advMsg.contains("PRADIPBHAI S SHUKLA"))

    val clerkMsg = com.example.ui.DirectoryActions.buildSuggestEditClerkMessage(
      clerk = clerk,
      fieldHint = "Clerk Details"
    )
    org.junit.Assert.assertTrue(clerkMsg.contains("SUGGESTION FOR CLERK UPDATE"))
    org.junit.Assert.assertTrue(clerkMsg.contains("PRADIPBHAI S SHUKLA"))
    org.junit.Assert.assertTrue(clerkMsg.contains("9825049155"))
  }

  @Test
  fun `test 7 taps on app logo opens admin login dialog`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.DirectoryViewModel(app)
    viewModel.exitAdminMode()
    viewModel.closeAdminAuthDialog()
    org.junit.Assert.assertFalse(viewModel.showAdminAuthDialog.value)

    // Tap 6 times: dialog should not open
    for (i in 1..6) {
      viewModel.onAppLogoClicked()
    }
    org.junit.Assert.assertFalse(viewModel.showAdminAuthDialog.value)

    // 7th tap: opens admin login dialog
    viewModel.onAppLogoClicked()
    org.junit.Assert.assertTrue(viewModel.showAdminAuthDialog.value)
  }

  @Test
  fun `test advocate with multiple clerks support and share message`() {
    val adv = com.example.model.AdvocateEntity(
      id = 10,
      name = "TEST ADVOCATE",
      code = "999",
      clerkId = 1,
      additionalClerkIds = "2,3"
    )
    val clerk1 = com.example.model.ClerkEntity(id = 1, name = "CLERK ONE", primaryMobile = "9800000001")
    val clerk2 = com.example.model.ClerkEntity(id = 2, name = "CLERK TWO", primaryMobile = "9800000002")
    val clerk3 = com.example.model.ClerkEntity(id = 3, name = "CLERK THREE", primaryMobile = "9800000003")

    val clerkIds = adv.getAllClerkIds()
    org.junit.Assert.assertEquals(3, clerkIds.size)
    org.junit.Assert.assertEquals(listOf(1L, 2L, 3L), clerkIds)

    val multiShareMsg = com.example.ui.DirectoryActions.buildAdvocateMultiClerkShareMessage(
      advocate = adv,
      clerks = listOf(clerk1, clerk2, clerk3)
    )
    org.junit.Assert.assertTrue(multiShareMsg.contains("Assigned Clerks (3)"))
    org.junit.Assert.assertTrue(multiShareMsg.contains("CLERK ONE"))
    org.junit.Assert.assertTrue(multiShareMsg.contains("CLERK TWO"))
    org.junit.Assert.assertTrue(multiShareMsg.contains("CLERK THREE"))
  }

  @Test
  fun `test pdf document catalog and valid pdf generation`() {
    val clerk1 = com.example.model.ClerkEntity(id = 1, name = "PRADIPBHAI S SHUKLA", primaryMobile = "9825049155")
    val clerk2 = com.example.model.ClerkEntity(id = 2, name = "RAMESH SOLANKI", primaryMobile = "9825000001")
    val adv1 = com.example.model.AdvocateWithClerk(
      advocate = com.example.model.AdvocateEntity(
        id = 1,
        name = "NITIN M AMIN",
        code = "126",
        primaryMobile = "9825049155",
        panelAdvocate = "State Bank of India (SBI) Panel",
        clerkId = 1,
        additionalClerkIds = "2",
        initialSrNo = 1
      ),
      clerk = clerk1
    )
    val adv2 = com.example.model.AdvocateWithClerk(
      advocate = com.example.model.AdvocateEntity(
        id = 2,
        name = "B G PATEL",
        code = "599",
        initialSrNo = 2
      ),
      clerk = null
    )
    val clerksWithAdvs = listOf(
      com.example.model.ClerkWithAdvocates(clerk = clerk1, advocates = listOf(adv1.advocate))
    )

    val catalog = com.example.data.PdfDirectoryGenerator.buildDocumentCatalog(
      allAdvocates = listOf(adv1, adv2),
      allClerks = listOf(clerk1, clerk2),
      clerksWithAdvocates = clerksWithAdvs,
      filteredAdvocates = listOf(adv1),
      searchQuery = "SBI"
    )

    // Default catalog has 0 built-in PDFs
    assertEquals(0, catalog.size)

    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    com.example.data.DirectoryDocumentType.values().forEach { docType ->
      val pdfBytes = com.example.data.PdfDirectoryGenerator.loadOrGeneratePdfBytes(
        context = app,
        type = docType,
        allAdvocates = listOf(adv1, adv2),
        allClerks = listOf(clerk1, clerk2),
        clerksWithAdvocates = clerksWithAdvs,
        filteredAdvocates = listOf(adv1),
        searchQuery = "SBI"
      )
      org.junit.Assert.assertTrue("PDF bytes should not be empty for $docType", pdfBytes.isNotEmpty())
      val header = String(pdfBytes.copyOfRange(0, minOf(8, pdfBytes.size)), Charsets.US_ASCII)
      org.junit.Assert.assertTrue("PDF should start with %PDF- for $docType, got $header", header.startsWith("%PDF-"))
    }
  }

  @Test
  fun `test switching to documents tab in viewmodel`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.DirectoryViewModel(app)
    viewModel.onTabSelected(com.example.ui.DirectoryTab.DOCUMENTS)
    assertEquals(com.example.ui.DirectoryTab.DOCUMENTS, viewModel.selectedTab.value)
  }

  @Test
  fun `test admin-only pdf add delete and restore defaults`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    // Reset any custom/hidden PDF state
    app.getSharedPreferences("custom_pdfs_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    java.io.File(app.filesDir, "custom_pdfs").deleteRecursively()

    val viewModel = com.example.ui.DirectoryViewModel(app)
    viewModel.exitAdminMode()

    val samplePdfBytes = "%PDF-1.4\n1 0 obj<<>>endobj\ntrailer<<>>\n%%EOF".toByteArray(Charsets.US_ASCII)

    // Non-admin must NOT be able to open Add PDF dialog or save/delete PDFs
    viewModel.openAddPdfDialog()
    org.junit.Assert.assertFalse(viewModel.showAddPdfDialog.value)

    viewModel.saveCustomPdf("Blocked Form", "Desc", "Court Forms", "blocked.pdf", samplePdfBytes)
    org.junit.Assert.assertTrue(com.example.data.PdfDirectoryGenerator.loadCustomPdfs(app).isEmpty())

    val dummyDoc = com.example.data.DirectoryPdfDocument(
      type = com.example.data.DirectoryDocumentType.CUSTOM_PDF,
      title = "Dummy",
      description = "Desc",
      category = "Custom",
      fileName = "dummy.pdf",
      recordCount = 1,
      estimatedPages = 1,
      id = "custom_dummy",
      isCustom = true
    )
    viewModel.confirmDeletePdf(dummyDoc)
    org.junit.Assert.assertNull(viewModel.deletePdfTarget.value)

    // Authenticate as Admin
    org.junit.Assert.assertTrue(viewModel.verifyAndEnableAdmin("HighCourt541"))
    viewModel.openAddPdfDialog()
    org.junit.Assert.assertTrue(viewModel.showAddPdfDialog.value)

    // Admin adds custom PDF
    val saved = com.example.data.PdfDirectoryGenerator.saveCustomPdf(
      context = app,
      title = "Vakalatnama Form",
      description = "Official filing form",
      category = "Court Forms",
      fileName = "vakalatnama.pdf",
      pdfBytes = samplePdfBytes
    )
    val customList = com.example.data.PdfDirectoryGenerator.loadCustomPdfs(app)
    assertEquals(1, customList.size)
    assertEquals("Vakalatnama Form", customList[0].title)

    // Verify loadOrGeneratePdfBytes reads back exact uploaded PDF bytes
    val loadedBytes = com.example.data.PdfDirectoryGenerator.loadOrGeneratePdfBytes(
      context = app,
      type = com.example.data.DirectoryDocumentType.CUSTOM_PDF,
      allAdvocates = emptyList(),
      allClerks = emptyList(),
      clerksWithAdvocates = emptyList(),
      filteredAdvocates = emptyList(),
      searchQuery = "",
      docId = saved.id
    )
    org.junit.Assert.assertArrayEquals(samplePdfBytes, loadedBytes)

    // Verify catalog includes only the 1 custom PDF
    val updatedCatalog = com.example.data.PdfDirectoryGenerator.buildDocumentCatalog(
      allAdvocates = emptyList(),
      allClerks = emptyList(),
      clerksWithAdvocates = emptyList(),
      filteredAdvocates = emptyList(),
      searchQuery = "",
      customPdfs = customList,
      hiddenBuiltInIds = com.example.data.PdfDirectoryGenerator.getHiddenBuiltInIds(app)
    )
    assertEquals(1, updatedCatalog.size)
    assertEquals(saved.id, updatedCatalog[0].id)
    org.junit.Assert.assertTrue(updatedCatalog[0].isCustom)

    // Admin deletes custom PDF
    com.example.data.PdfDirectoryGenerator.deleteDocument(app, updatedCatalog[0])
    org.junit.Assert.assertTrue(com.example.data.PdfDirectoryGenerator.loadCustomPdfs(app).isEmpty())
  }
}


