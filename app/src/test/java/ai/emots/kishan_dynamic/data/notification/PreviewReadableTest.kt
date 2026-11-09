package ai.emots.kishan_dynamic.data.notification

import ai.emots.kishan_dynamic.data.model.NotificationInfo
import org.junit.Assert.*
import org.junit.Test

class PreviewReadableTest {
    @Test fun slowRenderReceivesFullLifetimeAndAcknowledgementIsIdempotent() {
        val engine=IslandBehaviorEngine()
        val ingest=engine.reduce(IslandBehaviorState(),IslandEngineEvent.IngestNotification(NotificationInfo("sample","demo","Demo","Review","Ready")),1000)
        assertTrue(ingest.effects.none {it is IslandSideEffect.StartPreviewTimer})
        val readable=engine.reduce(ingest.state,IslandEngineEvent.PreviewReadable(ingest.state.activeToken),20000)
        assertEquals(listOf(IslandSideEffect.StartPreviewTimer(ingest.state.activeToken,3500)),readable.effects.filterIsInstance<IslandSideEffect.StartPreviewTimer>())
        val duplicate=engine.reduce(readable.state,IslandEngineEvent.PreviewReadable(ingest.state.activeToken),21000)
        assertTrue(duplicate.effects.none { it is IslandSideEffect.StartPreviewTimer })
        val stale=engine.reduce(readable.state,IslandEngineEvent.PreviewReadable(ingest.state.activeToken-1),22000)
        assertTrue(stale.effects.none { it is IslandSideEffect.StartPreviewTimer })
    }
    @Test fun userExpansionRejectsDelayedRendererAcknowledgement() {
        val engine=IslandBehaviorEngine()
        val preview=engine.reduce(IslandBehaviorState(),IslandEngineEvent.IngestNotification(NotificationInfo("sample","demo","Demo","Review","Ready")),1000).state
        val expanded=engine.reduce(preview,IslandEngineEvent.TapCompact,1100).state
        val delayed=engine.reduce(expanded,IslandEngineEvent.PreviewReadable(preview.activeToken),2000)
        assertTrue(delayed.effects.none { it is IslandSideEffect.StartPreviewTimer })
    }
    @Test fun deliberateReaderHasNoShortExpiryAfterNinetySeconds() {
        val engine = IslandBehaviorEngine()
        val preview = engine.reduce(IslandBehaviorState(), IslandEngineEvent.IngestNotification(NotificationInfo("reader", "demo", "Demo", "Review", "Ready")), 0).state
        val reader = engine.reduce(preview, IslandEngineEvent.TapCompact, 100).state
        val late = engine.reduce(reader, IslandEngineEvent.PreviewTimeout(preview.activeToken), 90_100)
        assertEquals(reader.ownership, late.state.ownership)
        assertEquals(reader.selectedKey, late.state.selectedKey)
        assertTrue(late.effects.none { it is IslandSideEffect.StartPreviewTimer })
    }
}
