#!/bin/bash
# Real host tests; never presented as checks run by the in-app policy button.
set -u
cd "$(dirname "$0")/.." || exit 1
serial="${1:-}"
if [ "$serial" = "--help" ] || [ "$serial" = "-h" ]; then
    echo "Usage: tools/test-island-playground.sh [ADB_SERIAL]"
    echo "Without a serial: build, JVM tests and release-manifest check only."
    exit 0
fi
report_dir="build/reports/island-playground"
mkdir -p "$report_dir"
gradle_status=0
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:processReleaseMainManifest --max-workers=2 --console=plain > "$report_dir/gradle.log" 2>&1 || gradle_status=$?
device_status="not_run"
if [ "$gradle_status" -eq 0 ] && [ -n "$serial" ]; then
    device_status="failed"
    if adb -s "$serial" install -r app/build/outputs/apk/debug/app-debug.apk > "$report_dir/install.log" 2>&1 &&
       adb -s "$serial" install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk >> "$report_dir/install.log" 2>&1; then
        adb -s "$serial" shell am instrument -w -r -e class ai.emots.kishan_dynamic.IslandLayoutInstrumentedTest,ai.emots.kishan_dynamic.DynamicIslandPlaygroundTest com.island.dynamicnotch.test/androidx.test.runner.AndroidJUnitRunner > "$report_dir/device.log" 2>&1
        if rg -q 'OK \([0-9]+ tests?\)' "$report_dir/device.log" && ! rg -q 'FAILURES!!!|INSTRUMENTATION_FAILED' "$report_dir/device.log"; then device_status="passed"; fi
    fi
fi
visual_status="not_run"
if [ "$device_status" = "passed" ]; then
    visual_status="failed"
    if python3 tools/check-island-playground-screenshots.py --serial "$serial" > "$report_dir/visual.log" 2>&1; then visual_status="passed"; fi
fi
export PLAYGROUND_VISUAL_STATUS="$visual_status"
export PLAYGROUND_GRADLE_STATUS="$gradle_status" PLAYGROUND_DEVICE_STATUS="$device_status"
python3 - <<'PY'
import datetime, json, os
from pathlib import Path
import xml.etree.ElementTree as ET
cases=[]
for path in sorted(Path('app/build/test-results/testDebugUnitTest').glob('TEST-*.xml')):
    for test in ET.parse(path).getroot().findall('testcase'):
        status='failed' if test.find('failure') is not None or test.find('error') is not None else 'skipped' if test.find('skipped') is not None else 'passed'
        cases.append({'class':test.get('classname',''), 'name':test.get('name',''), 'status':status})
passed=sum(c['status']=='passed' for c in cases)
release_files=list(Path('app/build/intermediates').glob('merged_manifests/release/**/AndroidManifest.xml')) + list(Path('app/build/intermediates').glob('merged_manifest/release/**/AndroidManifest.xml'))
release_clean=bool(release_files) and all('playground.' not in p.read_text() and 'Island Lab' not in p.read_text() for p in release_files)
data={'generated_at':datetime.datetime.now(datetime.timezone.utc).isoformat(),
      'gradle_status':int(os.environ['PLAYGROUND_GRADLE_STATUS']), 'device_status':os.environ['PLAYGROUND_DEVICE_STATUS'],
      'release_manifest_clean':release_clean, 'cases_current':int(os.environ['PLAYGROUND_GRADLE_STATUS'])==0,
      'visual_status':os.environ['PLAYGROUND_VISUAL_STATUS'], 'cases':cases,
      'summary':f"Build: {'PASS' if int(os.environ['PLAYGROUND_GRADLE_STATUS'])==0 else 'FAIL (JUnit may be cached)'}. JVM: {passed}/{len(cases)} passed. Device: {os.environ['PLAYGROUND_DEVICE_STATUS']}. Visual: {os.environ['PLAYGROUND_VISUAL_STATUS']}. Release isolation: {'PASS' if release_clean else 'UNVERIFIED/FAIL'}."}
target=Path('app/src/debug/assets/playground-test-results.json')
target.write_text(json.dumps(data,indent=2)+'\n')
Path('build/reports/island-playground/results.json').write_text(json.dumps(data,indent=2)+'\n')
print(data['summary'])
if not release_clean: raise SystemExit(2)
PY
report_status=$?
if [ "$gradle_status" -ne 0 ] || [ "$device_status" = "failed" ] || [ "$report_status" -ne 0 ] || [ "$visual_status" = "failed" ]; then
    echo "Validation failed; see $report_dir. No passing result is claimed."
    exit 1
fi
# Package the report snapshot for the debug Test Results section.
./gradlew :app:assembleDebug --max-workers=2 --console=plain > "$report_dir/package-report.log" 2>&1
