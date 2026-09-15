package com.relationshipradar.app.shizuku

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import com.relationshipradar.app.BuildConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import rikka.shizuku.Shizuku
import kotlin.coroutines.resume

/**
 * The app side of the Shizuku boundary. Everything elevated goes through [run]/[query]
 * with an allow-listed ID. A RootBridge could implement the same shape later.
 */
object ShizukuBridge {
    const val REQUEST_CODE = 4242

    enum class Status { NOT_INSTALLED, NOT_RUNNING, PERMISSION_NEEDED, READY }

    fun status(): Status = try {
        if (!Shizuku.pingBinder()) Status.NOT_RUNNING
        else if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) Status.READY
        else Status.PERMISSION_NEEDED
    } catch (_: Throwable) {
        Status.NOT_INSTALLED
    }

    fun requestPermission() {
        if (status() == Status.PERMISSION_NEEDED) Shizuku.requestPermission(REQUEST_CODE)
    }

    /** Registers once per process; the callback fires on the main thread. */
    fun onPermissionResult(callback: (granted: Boolean) -> Unit) {
        Shizuku.addRequestPermissionResultListener { code, result ->
            if (code == REQUEST_CODE) callback(result == PackageManager.PERMISSION_GRANTED)
        }
    }

    private val args = Shizuku.UserServiceArgs(ComponentName(BuildConfig.APPLICATION_ID, ShellService::class.java.name))
        .daemon(false)
        .processNameSuffix("shell")
        .debuggable(BuildConfig.DEBUG)
        .version(BuildConfig.VERSION_CODE)
        .tag("radar-shell")

    @Volatile private var service: IShellService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = if (binder != null && binder.pingBinder()) IShellService.Stub.asInterface(binder) else null
            pending.forEach { it.resume(service) }
            pending.clear()
        }
        override fun onServiceDisconnected(name: ComponentName?) { service = null }
    }
    private val pending = mutableListOf<kotlinx.coroutines.CancellableContinuation<IShellService?>>()

    private suspend fun connect(): IShellService? {
        if (status() != Status.READY) return null
        service?.let { return it }
        return withTimeoutOrNull(8_000) {
            suspendCancellableCoroutine { cont ->
                synchronized(pending) { pending += cont }
                try { Shizuku.bindUserService(args, connection) } catch (t: Throwable) { cont.resume(null) }
            }
        }
    }

    sealed class Outcome(val text: String) {
        class Ok(text: String) : Outcome(text)
        class Err(text: String) : Outcome(text)
    }

    suspend fun run(cmd: ShellCommands.Command): Outcome {
        val s = connect() ?: return Outcome.Err("Shizuku not connected")
        val r = try { s.run(cmd.id) } catch (t: Throwable) { "err:${t.message}" }
        return if (r.startsWith("ok:")) Outcome.Ok(r.removePrefix("ok:")) else Outcome.Err(r.removePrefix("err:"))
    }

    suspend fun query(q: ShellCommands.Query): Outcome {
        val s = connect() ?: return Outcome.Err("Shizuku not connected")
        val r = try { s.query(q.id) } catch (t: Throwable) { "err:${t.message}" }
        return if (r.startsWith("ok:")) Outcome.Ok(r.removePrefix("ok:")) else Outcome.Err(r.removePrefix("err:"))
    }

    fun disconnect() {
        try { Shizuku.unbindUserService(args, connection, true) } catch (_: Throwable) {}
        service = null
    }

}
