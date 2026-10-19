package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.model.AdvocateEntity
import com.example.model.ClerkEntity

object DirectoryActions {

    fun dialPhoneNumber(context: Context, phoneNumber: String) {
        val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
        if (cleanNumber.isBlank()) {
            Toast.makeText(context, "No valid phone number", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$cleanNumber")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to dial: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openWhatsApp(context: Context, phoneNumber: String) {
        var cleanNumber = phoneNumber.replace(Regex("[^0-9]"), "")
        if (cleanNumber.isBlank()) {
            Toast.makeText(context, "No valid phone number", Toast.LENGTH_SHORT).show()
            return
        }
        // If 10-digit Indian phone number without country code, prefix 91
        if (cleanNumber.length == 10) {
            cleanNumber = "91$cleanNumber"
        }
        try {
            // Standard WhatsApp URL that opens WhatsApp app on Android
            val url = "https://api.whatsapp.com/send?phone=$cleanNumber"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val fallbackUrl = "https://wa.me/$cleanNumber"
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Unable to open WhatsApp: ${ex.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Builds a neat, ready-to-share message strictly formatted with:
     * 👤 Advocate Name
     * 🔖 Advocate Code
     * 👔 Clerk Name
     * 📱 Clerk Mobile Number(s)
     *
     * Note: Does NOT include advocate mobile number or panel list as requested.
     */
    fun buildAdvocateShareMessage(advocate: AdvocateEntity, clerk: ClerkEntity?): String {
        val sb = StringBuilder()
        sb.append("📋 *ADVOCATE DETAILS*\n")
        sb.append("─────────────────────\n")
        sb.append("👤 *Advocate Name:* ${advocate.name}\n")
        if (advocate.code.isNotBlank()) {
            sb.append("🔖 *Advocate Code:* ${advocate.code}\n")
        }
        if (clerk != null && clerk.name.isNotBlank()) {
            sb.append("👔 *Clerk Name:* ${clerk.name}\n")
            val clerkPhones = listOf(clerk.primaryMobile, clerk.secondaryMobile).filter { it.isNotBlank() }
            if (clerkPhones.isNotEmpty()) {
                sb.append("📱 *Clerk Mobile Number(s):* ${clerkPhones.joinToString(", ")}\n")
            } else {
                sb.append("📱 *Clerk Mobile Number(s):* Not Available\n")
            }
        } else {
            sb.append("👔 *Clerk Name:* Not Assigned\n")
            sb.append("📱 *Clerk Mobile Number(s):* -\n")
        }
        sb.append("─────────────────────\n")
        sb.append("Shared via Adv & Clerk Directory App")
        return sb.toString()
    }

    /**
     * Builds a neat share message when an advocate has multiple clerks.
     */
    fun buildAdvocateMultiClerkShareMessage(advocate: AdvocateEntity, clerks: List<ClerkEntity>): String {
        if (clerks.size <= 1) {
            return buildAdvocateShareMessage(advocate, clerks.firstOrNull())
        }
        val sb = StringBuilder()
        sb.append("📋 *ADVOCATE DETAILS*\n")
        sb.append("─────────────────────\n")
        sb.append("👤 *Advocate Name:* ${advocate.name}\n")
        if (advocate.code.isNotBlank()) {
            sb.append("🔖 *Advocate Code:* ${advocate.code}\n")
        }
        sb.append("👔 *Assigned Clerks (${clerks.size}):*\n")
        clerks.forEachIndexed { index, clerk ->
            val phones = listOf(clerk.primaryMobile, clerk.secondaryMobile).filter { it.isNotBlank() }
            val phoneStr = if (phones.isNotEmpty()) phones.joinToString(", ") else "Not Available"
            sb.append("  ${index + 1}. *${clerk.name}* (📱 $phoneStr)\n")
        }
        sb.append("─────────────────────\n")
        sb.append("Shared via Adv & Clerk Directory App")
        return sb.toString()
    }

    /**
     * Builds a structured WhatsApp message to suggest updates/corrections to an Advocate's details.
     */
    fun buildSuggestEditAdvocateMessage(
        advocate: AdvocateEntity,
        clerk: ClerkEntity?,
        fieldHint: String = "Advocate Details"
    ): String {
        val sb = StringBuilder()
        sb.append("📝 *SUGGESTION FOR ADVOCATE UPDATE*\n")
        sb.append("─────────────────────\n")
        sb.append("👤 *Advocate Name:* ${advocate.name}\n")
        if (advocate.code.isNotBlank()) {
            sb.append("🔖 *Advocate Code:* ${advocate.code}\n")
        }
        if (advocate.primaryMobile.isNotBlank()) {
            sb.append("📱 *Primary Mobile:* ${advocate.primaryMobile}\n")
        }
        if (advocate.secondaryMobile.isNotBlank()) {
            sb.append("📱 *Secondary Mobile:* ${advocate.secondaryMobile}\n")
        }
        if (advocate.panelAdvocate.isNotBlank()) {
            sb.append("⚖️ *Empanelment/Panel:* ${advocate.panelAdvocate.replace("\n", ", ")}\n")
        }
        if (clerk != null && clerk.name.isNotBlank()) {
            sb.append("👔 *Assigned Clerk:* ${clerk.name}\n")
        }
        sb.append("─────────────────────\n")
        sb.append("📌 *Field to Correct:* $fieldHint\n")
        sb.append("✏️ *Suggested Correction / New Info:*\n")
        sb.append("[Please type your suggested update here]\n\n")
        sb.append("(Sent to Admin via Adv & Clerk Directory)")
        return sb.toString()
    }

    /**
     * Builds a structured WhatsApp message to suggest updates/corrections to a Clerk's details.
     */
    fun buildSuggestEditClerkMessage(
        clerk: ClerkEntity,
        fieldHint: String = "Clerk Details"
    ): String {
        val sb = StringBuilder()
        sb.append("📝 *SUGGESTION FOR CLERK UPDATE*\n")
        sb.append("─────────────────────\n")
        sb.append("👔 *Clerk Name:* ${clerk.name}\n")
        if (clerk.primaryMobile.isNotBlank()) {
            sb.append("📱 *Primary Mobile:* ${clerk.primaryMobile}\n")
        }
        if (clerk.secondaryMobile.isNotBlank()) {
            sb.append("📱 *Secondary Mobile:* ${clerk.secondaryMobile}\n")
        }
        sb.append("─────────────────────\n")
        sb.append("📌 *Field to Correct:* $fieldHint\n")
        sb.append("✏️ *Suggested Correction / New Info:*\n")
        sb.append("[Please type your suggested update here]\n\n")
        sb.append("(Sent to Admin via Adv & Clerk Directory)")
        return sb.toString()
    }

    /**
     * Sends a suggested edit directly to WhatsApp, allowing the user to select the Admin contact
     * or sending directly if an Admin WhatsApp number is known.
     */
    fun suggestEditViaWhatsApp(
        context: Context,
        message: String,
        adminPhoneNumber: String? = null
    ) {
        val cleanNumber = adminPhoneNumber?.replace(Regex("[^0-9]"), "")?.let {
            if (it.length == 10) "91$it" else it
        }

        if (!cleanNumber.isNullOrBlank()) {
            try {
                val encodedMessage = Uri.encode(message)
                val url = "https://api.whatsapp.com/send?phone=$cleanNumber&text=$encodedMessage"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return
            } catch (e: Exception) {
                // Fallback to general WhatsApp share
            }
        }

        shareViaWhatsApp(context, message)
    }

    /**
     * Shares pre-formatted text directly to WhatsApp or WhatsApp Business,
     * falling back to the standard Android share sheet if WhatsApp isn't installed.
     */
    fun shareViaWhatsApp(context: Context, message: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                setPackage("com.whatsapp")
                putExtra(Intent.EXTRA_TEXT, message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val bizIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    setPackage("com.whatsapp.w4b")
                    putExtra(Intent.EXTRA_TEXT, message)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(bizIntent)
            } catch (ex: Exception) {
                // Fallback to general share dialog
                shareGeneral(context, message, "Share via WhatsApp")
            }
        }
    }

    /**
     * General share chooser for sharing text to any app.
     */
    fun shareGeneral(context: Context, text: String, title: String = "Share Advocate Details") {
        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val chooser = Intent.createChooser(sendIntent, title).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to share: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard(context: Context, text: String, label: String = "Advocate Details") {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(context, "Details copied to clipboard", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to copy: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareExportedCsv(context: Context, uri: Uri) {
        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Adv & Clerk Directory Backup")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(sendIntent, "Export Adv & Clerk Directory CSV")
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Share error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shares the app with colleagues, advocates, and clerks via standard Android share sheet.
     */
    fun shareApp(context: Context) {
        try {
            val shareText = """
                ⚖️ *Adv & Clerk Directory*
                Official Legal Bar & Clerks Register Application
                
                Quickly search Advocates, Legal Clerks, Bar Codes, and Bank/Insurance Panel Advocates with one-tap Call and WhatsApp actions!
                
                Download & explore: Adv & Clerk Directory
            """.trimIndent()
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Adv & Clerk Directory App")
                putExtra(Intent.EXTRA_TEXT, shareText)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val chooser = Intent.createChooser(sendIntent, "Share Adv & Clerk Directory App")
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to share app: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

