package ai.emots.kishan_dynamic.ui.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/** The persisted app locale propagated through every Compose-owned surface. */
val LocalAppLanguage = staticCompositionLocalOf { "en" }

/**
 * Small app-owned copy catalog for the shared shell and settings vocabulary.
 * Missing entries intentionally fall back to the source copy until that
 * surface is translated; system-provided names are never rewritten.
 */
object AppLocalization {
    private val hindi = mapOf(
        "Island" to "आइलैंड",
        "Features" to "फीचर्स",
        "Live" to "लाइव",
        "Settings" to "सेटिंग्स",
        "Membership" to "सदस्यता",
        "Appearance" to "दिखावट",
        "Language" to "भाषा",
        "Support" to "सहायता",
        "About" to "जानकारी",
        "Permissions" to "अनुमतियां",
        "Theme" to "थीम",
        "App language" to "ऐप भाषा",
        "Rate the app" to "ऐप को रेट करें",
        "Send feedback" to "प्रतिक्रिया भेजें",
        "Share the app" to "ऐप साझा करें",
        "Check for updates" to "अपडेट जांचें",
        "Component gallery" to "कंपोनेंट गैलरी",
        "Reference playground" to "रेफरेंस प्लेग्राउंड",
        "Buttons" to "बटन",
        "Rows" to "रो",
        "Selection" to "चयन",
        "Values" to "मान",
        "Status" to "स्थिति",
        "Palette" to "पैलेट",
        "Type scale" to "टाइप स्केल",
        "Enjoying the app?" to "क्या आपको ऐप पसंद आ रहा है?",
        "Rate on Play Store" to "Play Store पर रेट करें",
        "Not now" to "अभी नहीं",
        "Cancel" to "रद्द करें",
        "Exit" to "बाहर निकलें",
        "Confirm" to "पुष्टि करें",
        "Done" to "हो गया",
        "System" to "सिस्टम",
        "Light" to "लाइट",
        "Dark" to "डार्क"
    )

    private val gujarati = mapOf(
        "Island" to "આઇલેન્ડ",
        "Features" to "ફીચર્સ",
        "Live" to "લાઇવ",
        "Settings" to "સેટિંગ્સ",
        "Membership" to "સભ્યપદ",
        "Appearance" to "દેખાવ",
        "Language" to "ભાષા",
        "Support" to "સહાય",
        "About" to "વિશે",
        "Permissions" to "પરવાનગીઓ",
        "Theme" to "થીમ",
        "App language" to "એપ ભાષા",
        "Rate the app" to "એપને રેટ કરો",
        "Send feedback" to "પ્રતિસાદ મોકલો",
        "Share the app" to "એપ શેર કરો",
        "Check for updates" to "અપડેટ તપાસો",
        "Component gallery" to "કમ્પોનન્ટ ગેલેરી",
        "Reference playground" to "રેફરન્સ પ્લેગ્રાઉન્ડ",
        "Buttons" to "બટનો",
        "Rows" to "રો",
        "Selection" to "પસંદગી",
        "Values" to "મૂલ્યો",
        "Status" to "સ્થિતિ",
        "Palette" to "પેલેટ",
        "Type scale" to "ટાઇપ સ્કેલ",
        "Enjoying the app?" to "શું તમને એપ ગમી રહી છે?",
        "Rate on Play Store" to "Play Store પર રેટ કરો",
        "Not now" to "અત્યારે નહીં",
        "Cancel" to "રદ કરો",
        "Exit" to "બહાર નીકળો",
        "Confirm" to "પુષ્ટિ કરો",
        "Done" to "થઈ ગયું",
        "System" to "સિસ્ટમ",
        "Light" to "લાઇટ",
        "Dark" to "ડાર્ક"
    )

    private val spanish = mapOf(
        "Island" to "Isla",
        "Features" to "Funciones",
        "Live" to "En vivo",
        "Settings" to "Ajustes",
        "Membership" to "Membresía",
        "Appearance" to "Apariencia",
        "Language" to "Idioma",
        "Support" to "Soporte",
        "About" to "Acerca de",
        "Permissions" to "Permisos",
        "Theme" to "Tema",
        "App language" to "Idioma de la app",
        "Rate the app" to "Valorar la app",
        "Send feedback" to "Enviar comentarios",
        "Share the app" to "Compartir la app",
        "Check for updates" to "Buscar actualizaciones",
        "Component gallery" to "Galería de componentes",
        "Reference playground" to "Laboratorio de referencia",
        "Buttons" to "Botones",
        "Rows" to "Filas",
        "Selection" to "Selección",
        "Values" to "Valores",
        "Status" to "Estado",
        "Palette" to "Paleta",
        "Type scale" to "Escala tipográfica",
        "Enjoying the app?" to "¿Te gusta la app?",
        "Rate on Play Store" to "Valorar en Play Store",
        "Not now" to "Ahora no",
        "Cancel" to "Cancelar",
        "Exit" to "Salir",
        "Confirm" to "Confirmar",
        "Done" to "Listo",
        "System" to "Sistema",
        "Light" to "Claro",
        "Dark" to "Oscuro"
    )

    private val arabic = mapOf(
        "Island" to "الجزيرة", "Features" to "الميزات", "Live" to "مباشر", "Settings" to "الإعدادات",
        "Membership" to "العضوية", "Appearance" to "المظهر", "Language" to "اللغة", "Support" to "الدعم",
        "About" to "حول", "Permissions" to "الأذونات", "Theme" to "السمة", "App language" to "لغة التطبيق",
        "Rate the app" to "قيّم التطبيق", "Send feedback" to "إرسال ملاحظات", "Share the app" to "مشاركة التطبيق",
        "Check for updates" to "التحقق من التحديثات", "Component gallery" to "معرض المكونات",
        "Reference playground" to "مساحة التجربة المرجعية", "Buttons" to "الأزرار", "Rows" to "الصفوف",
        "Selection" to "الاختيار", "Values" to "القيم", "Status" to "الحالة", "Palette" to "لوحة الألوان",
        "Type scale" to "مقياس الخط", "Enjoying the app?" to "هل تستمتع بالتطبيق؟",
        "Rate on Play Store" to "قيّم على Play Store", "Not now" to "ليس الآن", "Cancel" to "إلغاء",
        "Exit" to "خروج", "Confirm" to "تأكيد", "Done" to "تم", "System" to "النظام",
        "Light" to "فاتح", "Dark" to "داكن"
    )

    private val german = mapOf(
        "Island" to "Insel", "Features" to "Funktionen", "Live" to "Live", "Settings" to "Einstellungen",
        "Membership" to "Mitgliedschaft", "Appearance" to "Darstellung", "Language" to "Sprache",
        "Support" to "Support", "About" to "Über", "Permissions" to "Berechtigungen", "Theme" to "Design",
        "App language" to "App-Sprache", "Rate the app" to "App bewerten", "Send feedback" to "Feedback senden",
        "Share the app" to "App teilen", "Check for updates" to "Nach Updates suchen",
        "Component gallery" to "Komponentengalerie", "Reference playground" to "Referenz-Playground",
        "Buttons" to "Schaltflächen", "Rows" to "Zeilen", "Selection" to "Auswahl", "Values" to "Werte",
        "Status" to "Status", "Palette" to "Palette", "Type scale" to "Typografie",
        "Enjoying the app?" to "Gefällt dir die App?", "Rate on Play Store" to "Im Play Store bewerten",
        "Not now" to "Nicht jetzt", "Cancel" to "Abbrechen", "Exit" to "Beenden", "Confirm" to "Bestätigen",
        "Done" to "Fertig", "System" to "System", "Light" to "Hell", "Dark" to "Dunkel"
    )

    private val french = mapOf(
        "Island" to "Îlot", "Features" to "Fonctionnalités", "Live" to "En direct", "Settings" to "Réglages",
        "Membership" to "Abonnement", "Appearance" to "Apparence", "Language" to "Langue", "Support" to "Assistance",
        "About" to "À propos", "Permissions" to "Autorisations", "Theme" to "Thème", "App language" to "Langue de l’app",
        "Rate the app" to "Noter l’app", "Send feedback" to "Envoyer un avis", "Share the app" to "Partager l’app",
        "Check for updates" to "Rechercher des mises à jour", "Component gallery" to "Galerie de composants",
        "Reference playground" to "Terrain de référence", "Buttons" to "Boutons", "Rows" to "Lignes",
        "Selection" to "Sélection", "Values" to "Valeurs", "Status" to "État", "Palette" to "Palette",
        "Type scale" to "Échelle typographique", "Enjoying the app?" to "Vous aimez l’app ?",
        "Rate on Play Store" to "Noter sur Play Store", "Not now" to "Plus tard", "Cancel" to "Annuler",
        "Exit" to "Quitter", "Confirm" to "Confirmer", "Done" to "Terminé", "System" to "Système",
        "Light" to "Clair", "Dark" to "Sombre"
    )

    private val japanese = mapOf(
        "Island" to "アイランド", "Features" to "機能", "Live" to "ライブ", "Settings" to "設定",
        "Membership" to "メンバーシップ", "Appearance" to "外観", "Language" to "言語", "Support" to "サポート",
        "About" to "詳細", "Permissions" to "権限", "Theme" to "テーマ", "App language" to "アプリの言語",
        "Rate the app" to "アプリを評価", "Send feedback" to "フィードバックを送信", "Share the app" to "アプリを共有",
        "Check for updates" to "アップデートを確認", "Component gallery" to "コンポーネントギャラリー",
        "Reference playground" to "リファレンスプレイグラウンド", "Buttons" to "ボタン", "Rows" to "行",
        "Selection" to "選択", "Values" to "値", "Status" to "ステータス", "Palette" to "パレット",
        "Type scale" to "文字サイズ", "Enjoying the app?" to "アプリを楽しんでいますか？",
        "Rate on Play Store" to "Play Storeで評価", "Not now" to "今はしない", "Cancel" to "キャンセル",
        "Exit" to "終了", "Confirm" to "確認", "Done" to "完了", "System" to "システム",
        "Light" to "ライト", "Dark" to "ダーク"
    )

    private val korean = mapOf(
        "Island" to "아일랜드", "Features" to "기능", "Live" to "라이브", "Settings" to "설정",
        "Membership" to "멤버십", "Appearance" to "모양", "Language" to "언어", "Support" to "지원",
        "About" to "정보", "Permissions" to "권한", "Theme" to "테마", "App language" to "앱 언어",
        "Rate the app" to "앱 평가", "Send feedback" to "피드백 보내기", "Share the app" to "앱 공유",
        "Check for updates" to "업데이트 확인", "Component gallery" to "컴포넌트 갤러리",
        "Reference playground" to "레퍼런스 플레이그라운드", "Buttons" to "버튼", "Rows" to "행",
        "Selection" to "선택", "Values" to "값", "Status" to "상태", "Palette" to "팔레트",
        "Type scale" to "글꼴 크기", "Enjoying the app?" to "앱을 즐기고 계신가요?",
        "Rate on Play Store" to "Play Store에서 평가", "Not now" to "나중에", "Cancel" to "취소",
        "Exit" to "나가기", "Confirm" to "확인", "Done" to "완료", "System" to "시스템",
        "Light" to "라이트", "Dark" to "다크"
    )

    private val portuguese = mapOf(
        "Island" to "Ilha", "Features" to "Recursos", "Live" to "Ao vivo", "Settings" to "Configurações",
        "Membership" to "Assinatura", "Appearance" to "Aparência", "Language" to "Idioma", "Support" to "Suporte",
        "About" to "Sobre", "Permissions" to "Permissões", "Theme" to "Tema", "App language" to "Idioma do app",
        "Rate the app" to "Avaliar o app", "Send feedback" to "Enviar feedback", "Share the app" to "Compartilhar o app",
        "Check for updates" to "Verificar atualizações", "Component gallery" to "Galeria de componentes",
        "Reference playground" to "Laboratório de referência", "Buttons" to "Botões", "Rows" to "Linhas",
        "Selection" to "Seleção", "Values" to "Valores", "Status" to "Status", "Palette" to "Paleta",
        "Type scale" to "Escala tipográfica", "Enjoying the app?" to "Está gostando do app?",
        "Rate on Play Store" to "Avaliar na Play Store", "Not now" to "Agora não", "Cancel" to "Cancelar",
        "Exit" to "Sair", "Confirm" to "Confirmar", "Done" to "Concluído", "System" to "Sistema",
        "Light" to "Claro", "Dark" to "Escuro"
    )

    private val chinese = mapOf(
        "Island" to "灵动岛", "Features" to "功能", "Live" to "实时", "Settings" to "设置",
        "Membership" to "会员", "Appearance" to "外观", "Language" to "语言", "Support" to "支持",
        "About" to "关于", "Permissions" to "权限", "Theme" to "主题", "App language" to "应用语言",
        "Rate the app" to "评价应用", "Send feedback" to "发送反馈", "Share the app" to "分享应用",
        "Check for updates" to "检查更新", "Component gallery" to "组件库",
        "Reference playground" to "参考体验区", "Buttons" to "按钮", "Rows" to "列表行",
        "Selection" to "选择", "Values" to "数值", "Status" to "状态", "Palette" to "调色板",
        "Type scale" to "字体层级", "Enjoying the app?" to "喜欢这个应用吗？",
        "Rate on Play Store" to "在 Play Store 评价", "Not now" to "暂不", "Cancel" to "取消",
        "Exit" to "退出", "Confirm" to "确认", "Done" to "完成", "System" to "系统",
        "Light" to "浅色", "Dark" to "深色"
    )

    private val catalogs = mapOf(
        "hi" to hindi,
        "gu" to gujarati,
        "es" to spanish,
        "ar" to arabic,
        "de" to german,
        "fr" to french,
        "ja" to japanese,
        "ko" to korean,
        "pt" to portuguese,
        "zh" to chinese
    )

    val translatedLocaleCodes: Set<String> = setOf("en") + catalogs.keys

    fun translate(languageCode: String, source: String): String =
        catalogs[languageCode]?.get(source) ?: source

    fun hasTranslation(languageCode: String, source: String): Boolean =
        languageCode == "en" || catalogs[languageCode]?.containsKey(source) == true
}

@Composable
@ReadOnlyComposable
fun appString(source: String): String =
    AppLocalization.translate(LocalAppLanguage.current, source)
