package ai.emots.kishan_dynamic.data.model

object DummyData {
    val sampleMusicTracks = listOf(
        MusicTrack(
            title = "Heat Waves",
            artist = "Glass Animals",
            durationMs = 241000L,
            positionMs = 50000L
        ),
        MusicTrack(
            title = "Blinding Lights",
            artist = "The Weeknd",
            durationMs = 200000L,
            positionMs = 83000L
        ),
        MusicTrack(
            title = "Levitating",
            artist = "Dua Lipa",
            durationMs = 203000L,
            positionMs = 135000L
        )
    )

    val sampleContacts = listOf(
        ContactInfo(
            name = "Tamia Castillo",
            phoneNumber = "+1 555-0198"
        ),
        ContactInfo(
            name = "John Appleseed",
            phoneNumber = "+1 555-0142"
        ),
        ContactInfo(
            name = "Sarah Wilson",
            phoneNumber = "+1 555-0187"
        )
    )

    val sampleNotifications = listOf(
        NotificationInfo(
            id = "1",
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            title = "John Doe",
            text = "Hey! Are we still meeting at 5:00 PM for the project review?",
            isPriority = true
        ),
        NotificationInfo(
            id = "2",
            packageName = "com.google.android.gm",
            appName = "Gmail",
            title = "Flight Confirmation",
            text = "Your booking DL 492 from SFO to JFK is confirmed.",
            isPriority = false
        )
    )
}
