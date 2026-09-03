package ai.emots.kishan_dynamic.data.model

import androidx.compose.runtime.Immutable

/**
 * A language the app can be presented in.
 *
 * @param code     BCP-47 code persisted in preferences.
 * @param name     the language written in that language (endonym).
 * @param english  the English name, shown as the secondary line.
 * @param region   a short region hint used on the picker cards.
 * @param rtl      whether the script reads right-to-left.
 */
@Immutable
data class AppLanguage(
    val code: String,
    val name: String,
    val english: String,
    val region: String,
    val rtl: Boolean = false
)

object AppLanguages {

    val all: List<AppLanguage> = listOf(
        AppLanguage("en", "English", "English", "Global"),
        AppLanguage("hi", "हिंदी", "Hindi", "India"),
        AppLanguage("gu", "ગુજરાતી", "Gujarati", "India"),
        AppLanguage("bn", "বাংলা", "Bengali", "India"),
        AppLanguage("ta", "தமிழ்", "Tamil", "India"),
        AppLanguage("mr", "मराठी", "Marathi", "India"),
        AppLanguage("es", "Español", "Spanish", "Spain"),
        AppLanguage("pt", "Português", "Portuguese", "Brazil"),
        AppLanguage("fr", "Français", "French", "France"),
        AppLanguage("de", "Deutsch", "German", "Germany"),
        AppLanguage("it", "Italiano", "Italian", "Italy"),
        AppLanguage("tr", "Türkçe", "Turkish", "Türkiye"),
        AppLanguage("ru", "Русский", "Russian", "Russia"),
        AppLanguage("ar", "العربية", "Arabic", "Middle East", rtl = true),
        AppLanguage("fa", "فارسی", "Persian", "Iran", rtl = true),
        AppLanguage("id", "Bahasa Indonesia", "Indonesian", "Indonesia"),
        AppLanguage("vi", "Tiếng Việt", "Vietnamese", "Vietnam"),
        AppLanguage("th", "ไทย", "Thai", "Thailand"),
        AppLanguage("zh", "中文", "Chinese", "China"),
        AppLanguage("ja", "日本語", "Japanese", "Japan"),
        AppLanguage("ko", "한국어", "Korean", "Korea")
    )

    /** The handful we surface first, before the full alphabetical list. */
    val suggested: List<AppLanguage> = listOf("en", "hi", "gu", "es").mapNotNull(::byCode)

    fun byCode(code: String): AppLanguage? = all.firstOrNull { it.code == code }

    fun displayName(code: String): String = byCode(code)?.name ?: "English"

    fun search(query: String): List<AppLanguage> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return all
        return all.filter {
            it.name.lowercase().contains(q) ||
                it.english.lowercase().contains(q) ||
                it.region.lowercase().contains(q) ||
                it.code.lowercase().startsWith(q)
        }
    }
}
