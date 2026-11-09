# Touch, privacy and platform

Realme RMX3085 Android13, serial192.168.29.20:5555. Current inspection:
`Bound services:{}` and `Enabled services:{}` in dumpsys accessibility.
Production TYPE_ACCESSIBILITY_OVERLAY tests: BLOCKED. User-controlled path:
Settings → Accessibility → Downloaded/installed apps → Aurora → enable service.
No secure setting or production accessibility authorization changed.

Touch classes separately: outside native rectangle NOT RUN; transparent gutter
inside rectangle intercepted by default/NOT RUN; interactive surface NOT RUN.
Compose fixture captures cannot establish these properties.

ARTEMIS diagnosis: missing multimodal key; user explicitly authorized direct
ADB/Compose instead. Device was unlocked. Only synthetic fixture content is captured.
Existing privacy policy/analytics boundaries retained. Gallery actions are local no-ops.
Real call, external RemoteInput, keyguard, TalkBack and external media acceptance NOT RUN.

Latest check: phone disconnected; ADB devices empty, known Wi-Fi reconnect refused. Diagnosis self-heal restarted ADB but found no device/AVD. Final APK installation and device rerun are BLOCKED pending reconnection.
