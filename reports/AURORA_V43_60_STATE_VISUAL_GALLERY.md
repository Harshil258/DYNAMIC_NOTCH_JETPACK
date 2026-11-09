# V4.3 60-state visual gallery

Fresh device capture:1200 configurations,60 states. Figma comparison corrections are recorded in../docs/AURORA_FIGMA_VISUAL_RULES.md. These are synthetic shared-renderer Compose fixtures on Realme Android13.
They do not prove60 platform integrations or production accessibility-overlay behavior.
The image names are unique per source enum. Radius is the renderer target, not a fitted pixel measurement.

| State | Expected | Observed dp | Evidence | Review status |
|---|---|---|---|---|
| Idle | 127×36 R18 | 127.00 × 36.50 | [PNG](aurora-v43/Idle-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| Minimal | 108×32 R15.9018 | 108.00 × 32.00 | [PNG](aurora-v43/Minimal-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| MusicCompact | 190×36.67 R18.335 | 190.00 × 36.50 | [PNG](aurora-v43/MusicCompact-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| MusicExpanded | 367×177 R42 | 367.00 × 177.00 | [PNG](aurora-v43/MusicExpanded-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| CallCompact | 152×36.67 R18.335 | 152.00 × 36.50 | [PNG](aurora-v43/CallCompact-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| CallExpanded | 367×166 R42 | 367.00 × 152.50 | [PNG](aurora-v43/CallExpanded-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| CallSummaryExpanded | 367×112 R42 | 367.00 × 122.00 | [PNG](aurora-v43/CallSummaryExpanded-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| FaceTimeCallExpanded | 367×168 R42 | 367.00 × 168.00 | [PNG](aurora-v43/FaceTimeCallExpanded-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| SharedMediaCallExpanded | 367×172.64 R42 | 367.00 × 172.50 | [PNG](aurora-v43/SharedMediaCallExpanded-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| OngoingCallBanner | 367×86 R43 | 367.00 × 86.00 | [PNG](aurora-v43/OngoingCallBanner-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| NotificationCompact | intrinsic 120–250×36.67 R18.335 | 189.00 × 36.50 | [PNG](aurora-v43/NotificationCompact-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| NotificationStacked | main 120–250×36.67 R18.335; expanded stack uses MINI | 189.00 × 36.50 | [PNG](aurora-v43/NotificationStacked-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| NotificationWithMusicCompact | 190×36.67 R18.335 + 36.7 bubble, gap11 | 190.00 × 36.50 | [PNG](aurora-v43/NotificationWithMusicCompact-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| NotificationExpanded | 367×86–162 R43 (86); R42 (142+) | 367.00 × 74.00 | [PNG](aurora-v43/NotificationExpanded-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| SilentModeCompact | 152×36.67 R18.335 | 152.00 × 36.50 | [PNG](aurora-v43/SilentModeCompact-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| SilentModeExpanded | 367×86 R43 | 367.00 × 86.00 | [PNG](aurora-v43/SilentModeExpanded-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| RingerModeExpanded | 367×86 R43 | 367.00 × 86.00 | [PNG](aurora-v43/RingerModeExpanded-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| VibrateModeCompact | 172×36.67 R18.335 | 172.00 × 36.50 | [PNG](aurora-v43/VibrateModeCompact-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| NormalModeCompact | 172×36.67 R18.335 | 172.00 × 36.50 | [PNG](aurora-v43/NormalModeCompact-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| RingerVolumeCompact | 172×36.67 R18.335 | 172.00 × 36.50 | [PNG](aurora-v43/RingerVolumeCompact-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| MediaVolumeCompact | 172×36.67 R18.335 | 172.00 × 36.50 | [PNG](aurora-v43/MediaVolumeCompact-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| BluetoothConnected | 196×36.67 R18.335; contextual 366×95 R46 | 196.00 × 36.50 | [PNG](aurora-v43/BluetoothConnected-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| BluetoothConnecting | 196×36.67 R18.335 | 196.00 × 36.50 | [PNG](aurora-v43/BluetoothConnecting-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| ActionControlExpanded | 367×210 R42, dynamic height | 367.00 × 292.00 | [PNG](aurora-v43/ActionControlExpanded-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| PremiumExpiryExpanded | 367×112 R42 (proposal) | 367.00 × 123.50 | [PNG](aurora-v43/PremiumExpiryExpanded-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| ChargingCompact | 132×36.67 R18.335 | 132.00 × 36.50 | [PNG](aurora-v43/ChargingCompact-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| ChargingExpanded | 367×86 R43 | 367.00 × 86.00 | [PNG](aurora-v43/ChargingExpanded-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| LowBatteryCompact | 132×36.67 R18.335 | 132.00 × 36.50 | [PNG](aurora-v43/LowBatteryCompact-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| LowBatteryExpanded | 367×86 R43 | 367.00 × 86.00 | [PNG](aurora-v43/LowBatteryExpanded-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| TimerCompact | 222×36.67 R18.335 | 222.00 × 36.50 | [PNG](aurora-v43/TimerCompact-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| TimerExpanded | 367×85.73 R43 | 367.00 × 85.50 | [PNG](aurora-v43/TimerExpanded-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| DeliveryCompact | 160×36.67 R18.335 | 160.00 × 36.50 | [PNG](aurora-v43/DeliveryCompact-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| DeliveryExpanded | 367×144 R42 | 367.00 × 144.00 | [PNG](aurora-v43/DeliveryExpanded-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| FlightCompact | 160×36.67 R18.335 | 160.00 × 36.50 | [PNG](aurora-v43/FlightCompact-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| FlightExpanded | 367×144 R42 | 367.00 × 144.00 | [PNG](aurora-v43/FlightExpanded-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| SportsCompact | 156×36.67 R18.335 | 156.00 × 36.50 | [PNG](aurora-v43/SportsCompact-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| SportsExpanded | 367×144 R42 | 367.00 × 144.00 | [PNG](aurora-v43/SportsExpanded-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| NavigationCompact | 156×36.67 R18.335; optional 408×96 R44 | 156.00 × 36.50 | [PNG](aurora-v43/NavigationCompact-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| VoiceMemoExpanded | 367×86 R43 | 367.00 × 86.00 | [PNG](aurora-v43/VoiceMemoExpanded-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| ScreenRecordingExpanded | 367×86 R43 | 367.00 × 86.00 | [PNG](aurora-v43/ScreenRecordingExpanded-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| ShortcutExpanded | 367×85.08 R43 | 367.00 × 85.00 | [PNG](aurora-v43/ShortcutExpanded-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| FocusModeExpanded | 367×86 R43 | 367.00 × 86.00 | [PNG](aurora-v43/FocusModeExpanded-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| AirPodsConnected | 367×86 R43 (atlas) / 366×95 R46 (community) | 367.00 × 86.00 | [PNG](aurora-v43/AirPodsConnected-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| SatelliteConnected | 367×86 R43 | 367.00 × 86.00 | [PNG](aurora-v43/SatelliteConnected-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| FindMyAlert | 367×86 R43 | 367.00 × 86.00 | [PNG](aurora-v43/FindMyAlert-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| MovedToIPhone | 367×86 R43 | 367.00 × 86.00 | [PNG](aurora-v43/MovedToIPhone-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| VideoRemoteExpanded | 367×177 R42 | 367.00 × 177.00 | [PNG](aurora-v43/VideoRemoteExpanded-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| AirDropActivity | 367×86 R43 | 367.00 × 86.00 | [PNG](aurora-v43/AirDropActivity-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| TimerImage | 367×86 R43 (contextual); small compact only if separate variant | 367.00 × 86.00 | [PNG](aurora-v43/TimerImage-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| NotificationImage | 367×86–126 R43/R44 by content | 367.00 × 126.00 | [PNG](aurora-v43/NotificationImage-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| CallAvatars | 367×86 R43; expandable to 166 R42 if real actions | 367.00 × 86.00 | [PNG](aurora-v43/CallAvatars-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| TransportActivity | 367×148 R42 | 367.00 × 148.00 | [PNG](aurora-v43/TransportActivity-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| FlightTrackerExpanded | 367×144–162 R42 | 367.00 × 144.00 | [PNG](aurora-v43/FlightTrackerExpanded-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| SubscriptionPricing | 367×162 R42 | 367.00 × 162.00 | [PNG](aurora-v43/SubscriptionPricing-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| MovieCard | 367×142 R42 | 367.00 × 142.00 | [PNG](aurora-v43/MovieCard-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| ColorOptions | 367×185.3 R42 | 367.00 × 185.50 | [PNG](aurora-v43/ColorOptions-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| AirplaneAlert | 367×148 R42 | 367.00 × 148.00 | [PNG](aurora-v43/AirplaneAlert-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| ScreenMirroringAlert | 367×144 R42 | 367.00 × 144.00 | [PNG](aurora-v43/ScreenMirroringAlert-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| MobileDataAlert | 367×162 R42 | 367.00 × 162.00 | [PNG](aurora-v43/MobileDataAlert-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
| TransitRouteAlert | 367×142 R42 | 367.00 × 142.00 | [PNG](aurora-v43/TransitRouteAlert-baseline.png) | CAPTURED — current shared renderer; synthetic fixture |
