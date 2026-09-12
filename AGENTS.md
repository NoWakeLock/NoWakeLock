For files requiring reading with fewer than 1000 lines, read them in full at once.

Use context7 to get the latest documentation and best practice details.

## Xposed compatibility and delivery constraints

- Maintain the working legacy entry and official API 102 as separate runtime paths. Keep modern-only types isolated from old runtime class loading.
- Distinguish live rule activation from APK code activation. Rule edits must not require a reboot; a matching rule revision does not prove that a new Hook implementation is running.
- API 102 module code hot reload is tracked by `openspec/changes/add-api102-module-hot-reload/`. Until its lifecycle acceptance passes, do not claim that installing an APK automatically replaces existing system hooks. The currently deployed generation may require one normal target start to bootstrap reload support.
- Use official lifecycle/handle APIs for reload. Do not modify Vector, keep the application persistently running, or add storage I/O/mutex acquisition to hook decisions to support it.
- Never report reload success without checking required target generations and real hook behavior. Preserve rules, statistics, active wakelock accounting and user isolation; retire old workers/listeners explicitly.
- A statistics read, deserialization or app-cache write failure must never clear authoritative provider statistics. Keep the last data and report/retry the failed read.
- For the current OP13 hot-reload work, announce any required reboot and wait for the user to agree and reboot manually. Do not issue device reboot commands.
- Device delivery must verify requested/published/observed rule revisions, empty regex groups in a nonempty rule database, and actual allowed/blocked event deltas for the tested Android users. Record any missing first-unlock, cold-start, legacy or multi-user validation instead of treating installation as completed acceptance.

<!-- OPENSPEC:START -->
# OpenSpec Instructions

These instructions are for AI assistants working in this project.

Always open `@/openspec/AGENTS.md` when the request:
- Mentions planning or proposals (words like proposal, spec, change, plan)
- Introduces new capabilities, breaking changes, architecture shifts, or big performance/security work
- Sounds ambiguous and you need the authoritative spec before coding

Use `@/openspec/AGENTS.md` to learn:
- How to create and apply change proposals
- Spec format and conventions
- Project structure and guidelines

Keep this managed block so 'openspec update' can refresh the instructions.

<!-- OPENSPEC:END -->
