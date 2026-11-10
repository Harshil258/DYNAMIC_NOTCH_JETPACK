---
name: hotswan
description: Instructions for Compose HotSwan (hot reload for Jetpack Compose). Use when developing or iterating on Dynamic Island UI in real time on connected devices or emulators without rebuilding the entire APK.
---

# Compose HotSwan Skill (Hot Reload for Jetpack Compose)

This project is configured with **Compose HotSwan** for instant Compose UI updates on device without full Gradle builds.

## How It Works

1. **In Android Studio**:
   - Install the **Compose HotSwan** plugin from **Settings → Plugins → Marketplace**.
   - Restart Android Studio.
   - Run the app on the connected device (e.g. Realme 8) via the **Run** button.
   - Open **View → Tool Windows → HotSwan**, select your target device, and click **Start**.
   - Status changes to **WATCHING**.

2. **Hot Reloading on Save**:
   - Whenever any Compose file (e.g. `DynamicIslandPill.kt`, `IslandLayoutPreviews.kt`, `PlaygroundGallery.kt`) is edited and saved (`Cmd+S`), HotSwan compiles only the modified code and swaps it directly into the running app's memory in ~1 second with state, scroll position, and navigation stack preserved.
   - No APK reinstall, no full Gradle build cycle.

3. **What Applies In Place**:
   - Composable UI changes (modifiers, padding, shapes, colors, layout constraints).
   - Entire screen rewrites and structural additions/removals.
   - ViewModel methods, mappers, helper functions, and enum states.
   - Resource values (colors, strings, dimens).

4. **MCP Tooling (Optional)**:
   - In Android Studio, go to **Settings → Tools → Compose HotSwan** and click **Copy MCP Config to Clipboard**.
   - Paste into MCP server configuration to enable automated `hotswan_reload`, `hotswan_take_screenshot`, and `hotswan_get_semantic_tree`.

5. **Unit Tests Note**:
   - When running CLI unit tests with `./gradlew testDebugUnitTest`, use `-Photswan.dispatchRewriteEnabled=false` if running on non-Android JVM to ensure pure standard bytecode execution.
