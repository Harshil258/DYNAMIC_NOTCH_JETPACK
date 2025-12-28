# Requirements Document

## Introduction

This feature adds outside-click detection to the ActionControl island state, allowing users to dismiss the expanded control panel by tapping anywhere outside the island area. This improves the user experience by providing an intuitive way to exit the ActionControl view without requiring a specific close button.

## Glossary

- **ActionControl**: The expanded control panel state of the Dynamic Island that displays system toggles, app shortcuts, and sliders
- **Dynamic_Island**: The main UI component that morphs between different states
- **Outside_Click**: A user tap/click event that occurs outside the bounds of the ActionControl island
- **collapseToAppropriateState**: A function that determines and transitions the island to the correct collapsed state based on current system context
- **System_Context**: The current state of background activities (music playing, ongoing calls, notifications, etc.)

## Requirements

### Requirement 1: Outside Click Detection

**User Story:** As a user, I want to tap outside the ActionControl island to dismiss it, so that I can quickly exit the control panel without searching for a close button.

#### Acceptance Criteria

1. WHEN the island is in ActionControl state AND a user taps outside the island bounds, THEN the system SHALL detect the outside click event
2. WHEN an outside click is detected, THEN the system SHALL call the collapseToAppropriateState function
3. WHEN a user taps inside the ActionControl island bounds, THEN the system SHALL NOT trigger the collapse behavior
4. WHEN the island is NOT in ActionControl state, THEN outside clicks SHALL NOT trigger any collapse behavior

### Requirement 2: Appropriate State Determination

**User Story:** As a user, I want the island to return to a relevant state after dismissing ActionControl, so that I can see important ongoing activities like music or calls.

#### Acceptance Criteria

1. WHEN collapseToAppropriateState is called AND music is playing, THEN the system SHALL transition to CompactMusic state
2. WHEN collapseToAppropriateState is called AND a call is ongoing, THEN the system SHALL transition to OngoingCall state
3. WHEN collapseToAppropriateState is called AND the device is charging, THEN the system SHALL transition to Charging state
4. WHEN collapseToAppropriateState is called AND there are unread notifications, THEN the system SHALL transition to Notification state
5. WHEN collapseToAppropriateState is called AND no background activities exist, THEN the system SHALL transition to Minimal state
6. WHEN multiple background activities exist, THEN the system SHALL prioritize in the order: ongoing call, music playing, notifications, charging, then minimal

### Requirement 3: Smooth Transition Animation

**User Story:** As a user, I want smooth animations when the ActionControl collapses, so that the transition feels natural and polished.

#### Acceptance Criteria

1. WHEN the island collapses from ActionControl, THEN the system SHALL use the existing spring animation with dampingRatio 0.78f and stiffness 200f
2. WHEN transitioning to a new state, THEN the system SHALL animate width, height, and content changes simultaneously
3. WHEN the collapse animation completes, THEN the system SHALL display the appropriate state content

### Requirement 4: Touch Event Handling

**User Story:** As a developer, I want proper touch event handling that doesn't interfere with existing interactions, so that all island functionality continues to work correctly.

#### Acceptance Criteria

1. WHEN implementing outside click detection, THEN the system SHALL NOT interfere with existing button clicks within ActionControl
2. WHEN implementing outside click detection, THEN the system SHALL NOT interfere with slider interactions within ActionControl
3. WHEN a user interacts with ActionControl elements, THEN those interactions SHALL complete before any collapse can occur
4. WHEN the island is animating between states, THEN outside clicks SHALL be ignored until the animation completes
