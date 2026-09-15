package com.relationshipradar.app.shizuku

import android.content.Context
import androidx.annotation.Keep
import kotlin.system.exitProcess

/**
 * Runs inside Shizuku's elevated process (shell uid). It has no access to our database,
 * no Context of ours, and accepts only allow-listed command IDs.
 */
@Keep
class ShellService() : IShellService.Stub() {

    @Keep
    @Suppress("unused")
    constructor(context: Context) : this()

    override fun destroy() = exitProcess(0)
    override fun exit() = destroy()

    override fun run(commandId: Int): String {
        val cmd = ShellCommands.Command.byId(commandId) ?: return "err:unknown command $commandId"
        return exec(cmd.argv)
    }

    override fun query(queryId: Int): String {
        val q = ShellCommands.Query.byId(queryId) ?: return "err:unknown query $queryId"
        return exec(q.argv)
    }

    private fun exec(argv: List<String>): String = try {
        val p = ProcessBuilder(argv).redirectErrorStream(true).start()
        val out = p.inputStream.bufferedReader().readText().trim()
        val code = p.waitFor()
        (if (code == 0) "ok:" else "err:") + out
    } catch (t: Throwable) {
        "err:${t.message}"
    }
}
