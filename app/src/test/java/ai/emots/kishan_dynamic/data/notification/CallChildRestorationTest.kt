package ai.emots.kishan_dynamic.data.notification

import ai.emots.kishan_dynamic.data.model.NotificationInfo
import org.junit.Assert.*
import org.junit.Test

class CallChildRestorationTest {
    @Test fun callRestoresSelectedReaderWithoutExtendingChildDeadline() {
        val e=IslandBehaviorEngine()
        fun item(id:String)=NotificationInfo(id,"demo","Demo","Review","Ready")
        var s=e.reduce(IslandBehaviorState(),IslandEngineEvent.IngestNotification(item("main"),autoExpand=false),100).state
        s=e.reduce(s,IslandEngineEvent.TapCompact,110).state
        s=e.reduce(s,IslandEngineEvent.IngestNotification(item("child")),120).state
        s=e.reduce(s,IslandEngineEvent.ChildPhaseChanged("child",ChildLifecycle.VISIBLE),200).state
        val deadline=s.childNotices.single().expiresAtMonotonic
        val interrupted=e.reduce(s,IslandEngineEvent.IncomingCallPreempt,300).state
        val restored=e.reduce(interrupted,IslandEngineEvent.CallEnded,400)
        assertEquals("main",restored.state.selectedKey)
        assertEquals(s.ownership,restored.state.ownership)
        assertEquals(deadline,restored.state.childNotices.single().expiresAtMonotonic)
        assertTrue(restored.effects.contains(IslandSideEffect.StartChildTimer("child",s.childNotices.single().token,deadline-400)))
        val late=e.reduce(interrupted,IslandEngineEvent.CallEnded,deadline+1).state
        assertTrue(late.childNotices.isEmpty())
    }
}
