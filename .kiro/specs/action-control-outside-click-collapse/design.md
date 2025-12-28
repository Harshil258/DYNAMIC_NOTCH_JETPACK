# Design Document: ActionControl Outside Click Collapse

## Overview

This design implements outside-click detection for the ActionControl island state, enabling users to dismiss the expanded control panel by tapping outside its bounds. The solution uses Compose's pointer input modifiers to detect clicks outside the island area and implements a state determination algorithm to transition to the most appropriate collapsed state based on current system context.

The implementation follows Android's Compose UI patterns and integrates seamlessly with the existing Dynamic Island animation system.

## Architecture

### Component Structure

```
MainActivity (State Management)
    ├── DynamicIsland (Main Container)
    │   ├── ActionControlIsland (Content)
    │   └── Outside Click Detector (New)
    └── collapseToAppropriateState() (New Function)
```

### Key Components

1. **Outside Click Detector**: A Box wrapper around the DynamicIsland that captures clicks outside the island bounds
2. **State Determination Logic**: A function that analyzes current system context to determine the appropriate collapsed state
3. **State Transition Handler**: Integration with existing state management to trigger smooth transitions

## Components and Interfaces

### 1. Outside Click Detection Layer

The outside click detection will be implemented as a wrapper Box in MainActivity that:
- Fills the entire screen
- Captures pointer events
- Determines if clicks are outside the island bounds
- Triggers collapse only when ActionControl is active

```kotlin
// Pseudocode structure
Box(modifier = Modifier.fillMaxSize()) {
    // Background click detector
    if (currentState is IslandState.ActionControl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // Check if tap is outside island bounds
                        if (isOutsideIsland(offset)) {
                            collapseToAppropriateState()
                        }
                    }
                }
        )
    }
    
    // Existing DynamicIsland
    DynamicIsland(...)
}
```

### 2. State Determination Function

The `collapseToAppropriateState()` function will:
- Accept current system context as parameters
- Evaluate background activities in priority order
- Return the appropriate IslandState

**Function Signature:**
```kotlin
fun collapseToAppropriateState(
    isMusicPlaying: Boolean,
    currentTrack: MusicTrack?,
    isCallOngoing: Boolean,
    callInfo: OngoingCallInfo?,
    isCharging: Boolean,
    batteryPercentage: Int,
    hasNotifications: Boolean,
    notifications: List<NotificationInfo>
): IslandState
```

**Priority Logic:**
1. Ongoing call → `IslandState.OngoingCall`
2. Music playing → `IslandState.CompactMusic`
3. Unread notifications → `IslandState.Notification`
4. Charging → `IslandState.Charging`
5. Default → `IslandState.Minimal`

### 3. Integration Points

**MainActivity Changes:**
- Add state variables to track background activities
- Implement `collapseToAppropriateState()` function
- Wrap DynamicIsland with outside click detector
- Pass collapse callback to DynamicIsland

**DynamicIsland Changes:**
- Accept optional `onOutsideClick` callback parameter
- No internal changes needed (handled at parent level)

## Data Models

### New Data Structures

No new data models are required. The implementation uses existing models:
- `IslandState` (existing sealed class)
- `MusicTrack` (existing)
- `NotificationInfo` (existing)
- `ActionControlConfig` (existing)

### State Tracking

MainActivity will maintain these state variables:
```kotlin
var currentState: IslandState
var isMusicPlaying: Boolean
var currentMusicTrack: MusicTrack?
var isCallOngoing: Boolean
var ongoingCallInfo: OngoingCallInfo?
var isCharging: Boolean
var batteryPercentage: Int
var notifications: List<NotificationInfo>
```

## Error Handling

### Edge Cases

1. **Rapid Clicks**: Debounce outside clicks to prevent multiple rapid state transitions
   - Solution: Use `LaunchedEffect` with delay or track last click timestamp

2. **Animation in Progress**: Prevent state changes during ongoing animations
   - Solution: Track animation state and ignore clicks during transitions

3. **Null Context**: Handle cases where background activity data is unavailable
   - Solution: Default to Minimal state if context is incomplete

4. **Touch Event Conflicts**: Ensure outside clicks don't interfere with island interactions
   - Solution: Use `pointerInput` with proper event consumption

### Error Recovery

- If state determination fails, default to `IslandState.Minimal`
- Log errors for debugging without crashing the app
- Maintain current state if collapse logic encounters exceptions

## Testing Strategy

### Unit Tests

Unit tests will verify specific examples and edge cases:

1. **State Determination Tests**
   - Test with music playing → returns CompactMusic
   - Test with call ongoing → returns OngoingCall
   - Test with no activities → returns Minimal
   - Test priority: call over music
   - Test priority: music over notifications

2. **Click Detection Tests**
   - Test click inside island bounds → no collapse
   - Test click outside island bounds → triggers collapse
   - Test click when not in ActionControl → no effect

3. **Edge Case Tests**
   - Test with null music track
   - Test with empty notification list
   - Test with multiple simultaneous activities

### Property-Based Tests

Property-based tests will verify universal properties across all inputs. Each property test will run a minimum of 100 iterations to ensure comprehensive coverage.

