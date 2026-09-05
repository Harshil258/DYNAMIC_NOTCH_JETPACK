package ai.emots.kishan_dynamic.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ai.emots.kishan_dynamic.data.model.ActionAppShortcut
import ai.emots.kishan_dynamic.data.model.ActionContactShortcut
import ai.emots.kishan_dynamic.data.model.ActionCustomShortcut
import ai.emots.kishan_dynamic.data.model.ActionIslandConfig
import ai.emots.kishan_dynamic.data.model.ActionSystemTile
import ai.emots.kishan_dynamic.data.model.InstalledAppInfo
import ai.emots.kishan_dynamic.data.model.DeviceContactInfo
import ai.emots.kishan_dynamic.data.model.defaultActionSystemTiles
import ai.emots.kishan_dynamic.data.model.reconcilePinnedApps
import ai.emots.kishan_dynamic.data.preferences.AuroraPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.actionIslandDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "action_island_preferences"
)

/**
 * Owns Action Island customization only. The overlay consumes this read model;
 * it does not know how preferences are encoded or how installed apps are found.
 */
class ActionIslandRepository(private val context: Context) {

    private val packageManager = context.packageManager
    private val preferences = AuroraPreferences(context)

    private companion object {
        val ENABLED_KEY = booleanPreferencesKey("enabled")
        val SYSTEM_TILES_KEY = stringPreferencesKey("system_tiles")
        val APPS_KEY = stringPreferencesKey("apps")
        val CONTACTS_KEY = stringPreferencesKey("contacts")
        val CUSTOM_ACTIONS_KEY = stringPreferencesKey("custom_actions")
        const val ITEM_SEPARATOR = "\u001F"
        const val FIELD_SEPARATOR = "\u001E"
    }

    val config: Flow<ActionIslandConfig> = context.actionIslandDataStore.data.map { preferences ->
        ActionIslandConfig(
            isEnabled = preferences[ENABLED_KEY] ?: true,
            systemTiles = decodeSystemTiles(preferences[SYSTEM_TILES_KEY]),
            apps = decodeApps(preferences[APPS_KEY]),
            contacts = decodeContacts(preferences[CONTACTS_KEY]),
            customActions = decodeCustomActions(preferences[CUSTOM_ACTIONS_KEY])
        )
    }

    suspend fun setEnabled(enabled: Boolean) {
        context.actionIslandDataStore.edit { it[ENABLED_KEY] = enabled }
    }

    suspend fun setSystemTiles(tiles: List<ActionSystemTile>) {
        context.actionIslandDataStore.edit { preferences ->
            preferences[SYSTEM_TILES_KEY] = tiles.joinToString(ITEM_SEPARATOR) { it.storageKey }
        }
    }

    suspend fun addApp(app: InstalledAppInfo) {
        context.actionIslandDataStore.edit { preferences ->
            val current = decodeApps(preferences[APPS_KEY])
            val maxApps = if (this@ActionIslandRepository.preferences.isProActive.first()) {
                ActionIslandConfig().maxApps
            } else 3
            if (current.any { it.packageName == app.packageName } || current.size >= maxApps) return@edit
            preferences[APPS_KEY] = encodeApps(current + ActionAppShortcut(app.packageName, app.appName, current.size))
        }
    }

    suspend fun removeApp(packageName: String) {
        context.actionIslandDataStore.edit { preferences ->
            val updated = decodeApps(preferences[APPS_KEY])
                .filterNot { it.packageName == packageName }
                .mapIndexed { index, app -> app.copy(order = index) }
            preferences[APPS_KEY] = encodeApps(updated)
        }
    }

    suspend fun setAppEnabled(packageName: String, enabled: Boolean) {
        context.actionIslandDataStore.edit { preferences ->
            preferences[APPS_KEY] = encodeApps(
                decodeApps(preferences[APPS_KEY]).map { app ->
                    if (app.packageName == packageName) app.copy(isEnabled = enabled) else app
                }
            )
        }
    }

    suspend fun reorderApps(apps: List<ActionAppShortcut>) {
        context.actionIslandDataStore.edit { preferences ->
            preferences[APPS_KEY] = encodeApps(apps.mapIndexed { index, app -> app.copy(order = index) })
        }
    }

    suspend fun addContact(contact: DeviceContactInfo) {
        context.actionIslandDataStore.edit { preferences ->
            val current = decodeContacts(preferences[CONTACTS_KEY])
            val maxContacts = if (this@ActionIslandRepository.preferences.isProActive.first()) {
                ActionIslandConfig().maxContacts
            } else 2
            if (current.any { it.contactId == contact.contactId } || current.size >= maxContacts) return@edit
            preferences[CONTACTS_KEY] = encodeContacts(
                current + ActionContactShortcut(
                    contactId = contact.contactId,
                    name = contact.name,
                    phoneNumber = contact.phoneNumber,
                    photoUri = contact.photoUri,
                    order = current.size
                )
            )
        }
    }

    suspend fun removeContact(contactId: String) {
        context.actionIslandDataStore.edit { preferences ->
            val updated = decodeContacts(preferences[CONTACTS_KEY])
                .filterNot { it.contactId == contactId }
                .mapIndexed { index, contact -> contact.copy(order = index) }
            preferences[CONTACTS_KEY] = encodeContacts(updated)
        }
    }

    suspend fun setContactEnabled(contactId: String, enabled: Boolean) {
        context.actionIslandDataStore.edit { preferences ->
            preferences[CONTACTS_KEY] = encodeContacts(
                decodeContacts(preferences[CONTACTS_KEY]).map { contact ->
                    if (contact.contactId == contactId) contact.copy(isEnabled = enabled) else contact
                }
            )
        }
    }

    suspend fun reorderContacts(contacts: List<ActionContactShortcut>) {
        context.actionIslandDataStore.edit { preferences ->
            preferences[CONTACTS_KEY] = encodeContacts(
                contacts.mapIndexed { index, contact -> contact.copy(order = index) }
            )
        }
    }

    suspend fun addCustomAction(action: ActionCustomShortcut) {
        context.actionIslandDataStore.edit { preferences ->
            val current = decodeCustomActions(preferences[CUSTOM_ACTIONS_KEY])
            val maxActions = if (this@ActionIslandRepository.preferences.isProActive.first()) {
                ActionIslandConfig().maxCustomActions
            } else 2
            if (current.any { it.actionId == action.actionId } || current.size >= maxActions) return@edit
            preferences[CUSTOM_ACTIONS_KEY] = encodeCustomActions(
                current + action.copy(order = current.size)
            )
        }
    }

    suspend fun removeCustomAction(actionId: String) {
        context.actionIslandDataStore.edit { preferences ->
            val updated = decodeCustomActions(preferences[CUSTOM_ACTIONS_KEY])
                .filterNot { it.actionId == actionId }
                .mapIndexed { index, action -> action.copy(order = index) }
            preferences[CUSTOM_ACTIONS_KEY] = encodeCustomActions(updated)
        }
    }

    suspend fun reorderCustomActions(actions: List<ActionCustomShortcut>) {
        context.actionIslandDataStore.edit { preferences ->
            preferences[CUSTOM_ACTIONS_KEY] = encodeCustomActions(
                actions.mapIndexed { index, action -> action.copy(order = index) }
            )
        }
    }

    fun contacts(): List<DeviceContactInfo> {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) return emptyList()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )
        return runCatching {
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE NOCASE ASC"
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
                buildList {
                    val seen = mutableSetOf<String>()
                    while (cursor.moveToNext()) {
                        val id = cursor.getString(idIndex) ?: continue
                        if (!seen.add(id)) continue
                        add(
                            DeviceContactInfo(
                                contactId = id,
                                name = cursor.getString(nameIndex).orEmpty().ifBlank { "Contact" },
                                phoneNumber = cursor.getString(numberIndex).orEmpty(),
                                photoUri = cursor.getString(photoIndex)
                            )
                        )
                    }
                }
            }.orEmpty()
        }.getOrDefault(emptyList())
    }

    fun installedApps(): List<InstalledAppInfo> {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)
            .mapNotNull { resolveInfo ->
                val appInfo = resolveInfo.activityInfo?.applicationInfo ?: return@mapNotNull null
                if (appInfo.packageName == context.packageName) return@mapNotNull null
                InstalledAppInfo(
                    packageName = appInfo.packageName,
                    appName = packageManager.getApplicationLabel(appInfo).toString(),
                    isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }
            .distinctBy { it.packageName }
            .sortedWith(compareBy<InstalledAppInfo> { it.isSystemApp }.thenBy { it.appName.lowercase() })
    }

    /** Removes stale launcher shortcuts without touching an empty discovery snapshot. */
    suspend fun pruneMissingApps() {
        val availablePackages = installedApps().mapTo(mutableSetOf()) { it.packageName }
        if (availablePackages.isEmpty()) return

        context.actionIslandDataStore.edit { preferences ->
            val current = decodeApps(preferences[APPS_KEY])
            val reconciled = reconcilePinnedApps(current, availablePackages)
            if (reconciled != current) {
                preferences[APPS_KEY] = encodeApps(reconciled)
            }
        }
    }

    fun launchApp(packageName: String): Boolean {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: return false
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            context.startActivity(launchIntent)
            true
        }.getOrDefault(false)
    }

    private fun decodeSystemTiles(value: String?): List<ActionSystemTile> {
        if (value.isNullOrBlank()) return defaultActionSystemTiles
        return value.split(ITEM_SEPARATOR).mapNotNull { key ->
            ActionSystemTile.entries.firstOrNull { it.storageKey == key }
        }.ifEmpty { defaultActionSystemTiles }
    }

    private fun encodeApps(apps: List<ActionAppShortcut>): String = apps.joinToString(ITEM_SEPARATOR) {
        listOf(it.packageName, it.appName, it.order.toString(), it.isEnabled.toString())
            .joinToString(FIELD_SEPARATOR)
    }

    private fun decodeApps(value: String?): List<ActionAppShortcut> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(ITEM_SEPARATOR).mapNotNull { encoded ->
            val fields = encoded.split(FIELD_SEPARATOR)
            if (fields.size !in 3..4) return@mapNotNull null
            ActionAppShortcut(
                packageName = fields[0],
                appName = fields[1],
                order = fields[2].toIntOrNull() ?: return@mapNotNull null,
                isEnabled = fields.getOrNull(3)?.toBooleanStrictOrNull() ?: true
            )
        }.sortedBy { it.order }
    }

    private fun encodeContacts(contacts: List<ActionContactShortcut>): String = contacts.joinToString(ITEM_SEPARATOR) {
        listOf(it.contactId, it.name, it.phoneNumber, it.photoUri.orEmpty(), it.order.toString(), it.isEnabled.toString())
            .joinToString(FIELD_SEPARATOR)
    }

    private fun decodeContacts(value: String?): List<ActionContactShortcut> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(ITEM_SEPARATOR).mapNotNull { encoded ->
            val fields = encoded.split(FIELD_SEPARATOR)
            if (fields.size !in 5..6) return@mapNotNull null
            ActionContactShortcut(
                contactId = fields[0],
                name = fields[1],
                phoneNumber = fields[2],
                photoUri = fields[3].ifBlank { null },
                order = fields[4].toIntOrNull() ?: return@mapNotNull null,
                isEnabled = fields.getOrNull(5)?.toBooleanStrictOrNull() ?: true
            )
        }.sortedBy { it.order }
    }

    private fun encodeCustomActions(actions: List<ActionCustomShortcut>): String =
        actions.joinToString(ITEM_SEPARATOR) {
            listOf(it.actionId, it.label, it.iconKey, it.order.toString())
                .joinToString(FIELD_SEPARATOR)
        }

    private fun decodeCustomActions(value: String?): List<ActionCustomShortcut> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(ITEM_SEPARATOR).mapNotNull { encoded ->
            val fields = encoded.split(FIELD_SEPARATOR)
            if (fields.size != 4) return@mapNotNull null
            ActionCustomShortcut(
                actionId = fields[0],
                label = fields[1],
                iconKey = fields[2],
                order = fields[3].toIntOrNull() ?: return@mapNotNull null
            )
        }.sortedBy { it.order }
    }
}
