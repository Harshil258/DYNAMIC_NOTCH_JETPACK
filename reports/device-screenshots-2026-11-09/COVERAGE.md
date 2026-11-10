# Aurora physical-device screenshot review

Open **index.html**, then choose a state to view all its conditions. Original PNGs retain their captured resolution.

| Group | Expected | Captured |
|---|---:|---:|
| Native phone,60states ×dark/light |120|120|
| Long content, missing content, unavailable controls ×60states |180|180|
| Family-specific notification/call/media/timer/battery/Bluetooth/ringer/source/cutout/hidden cases |177|177|
|60states ×5widths ×4font scales ×2directions ×2themes |4800|4800|

Expected main images: **5277**. Missing files: **0**. Invalid PNGs: **0**. Unrecorded expected files: **0**.

The original60-state inventory is enumerated automatically, including the separate diagnostic owner Minimal, split notification+music, incoming/group/active/summary calls, all activity cards and generic sports (no cricket). Hidden is an additional explicit condition.

Conditions: widths320/360/393/430/640dp (640 is simulated landscape), font100/130/150/200%, LTR/RTL, dark/light. Full-screen native images are1080×2400 on the Realme with its real density/font. Responsive images are crops of the real renderer executed on that same phone with simulated layout inputs. They are not screenshots from five different devices.

Source conditions include single/image/broken-image/inbox/progress/indeterminate/reply/redacted/multi-action/many-message/first/last notification pages; counts0/1/2/3/9/10/99/100; dialing/unknown/long-duration/carrier-controls/no-decline/private/missed calls; paused media/timers; expired/long timers and entitlement; battery0/1/19/20/99/100 and unknown; Bluetooth connecting; ringer modes and zero/full volume; cutout/flat-top renderer inputs;19live-activity kinds with running/paused/missing source fixtures. All source data is synthetic. A condition input may be visually irrelevant to some recipes; the screenshot preserves what the renderer actually produced.

Additional layered captures cover child stacks, promotion/merge, overflow bursts, vertical paging, large text/RTL, redaction and selected animation frames. Motion captures are selected deterministic frames, not a frame-rate certification.

## Not represented as completed real-system conditions

These captures do not claim real incoming/outgoing carrier calls, real notification intake or RemoteInput submission, actual lockscreen/keyguard, real IME animation, TalkBack traversal, OS permission denial/recovery, real hardware rotation, app-background/lifecycle recovery, production accessibility-overlay window correctness, different OEMs or performance. Those require separate system/device acceptance. Redacted and call screenshots here are mock-source visual states, not genuine private notifications or calls. No phone font, theme, rotation, notification, billing or production preferences were changed.

See captured.tsv for every image's inputs and size, capture-provenance.json for device/build hashes, and coverage.json for the missing-file check. Compilation and screenshot completeness are not pixel-parity verdicts.
