package ai.emots.kishan_dynamic.ui.components

import ai.emots.kishan_dynamic.data.model.CallDirection
import ai.emots.kishan_dynamic.data.model.CallRecord
import ai.emots.kishan_dynamic.data.model.DurationAccuracy
import org.junit.Assert.assertEquals
import org.junit.Test

class CallSummaryDurationLabelTest {
    private val call = CallRecord("fixture","Caller","",startedAtMillis=0L,
        durationSeconds=125L,direction=CallDirection.INCOMING)
    @Test fun unknownDurationDoesNotClaimAZeroLengthCall() {
        assertEquals("Duration unavailable",callSummaryDurationLabel(call.copy(
            durationSeconds=0L,durationAccuracy=DurationAccuracy.UNKNOWN)))
    }
    @Test fun estimatedDurationIsDistinguishedFromMeasuredDuration() {
        assertEquals("02:05",callSummaryDurationLabel(call))
        assertEquals("≈02:05",callSummaryDurationLabel(call.copy(durationAccuracy=DurationAccuracy.INFERRED_WALL_CLOCK)))
    }
    @Test fun missedAndGenericSummariesDoNotInventAnElapsedDuration() {
        assertEquals("Missed",callSummaryDurationLabel(call.copy(direction=CallDirection.MISSED,durationSeconds=0L)))
        assertEquals("",callSummaryDurationLabel(null))
    }
    @Test fun callPhaseDistinguishesEndedCallsFromActiveRinging() {
        assertEquals("Call ended · Incoming", callSummaryPhaseLabel(call))
        assertEquals("Outgoing call ended", callSummaryPhaseLabel(call.copy(direction = CallDirection.OUTGOING)))
        assertEquals("Missed call", callSummaryPhaseLabel(call.copy(direction = CallDirection.MISSED)))
        assertEquals("Call ended", callSummaryPhaseLabel(null, isDemoPreview = false))
        assertEquals("Call ended · Incoming", callSummaryPhaseLabel(null, isDemoPreview = true))
    }
    @Test fun callSummaryExpandedTargetGeometryIsStrictly112dpNominalHeight() {
        val target = IslandFamilyGeometry.target(IslandDemoState.CallSummaryExpanded)
        assertEquals(367f, target.width, 0.001f)
        assertEquals(112f, target.height, 0.001f)
        assertEquals(42f, target.radius, 0.001f)
    }
}
