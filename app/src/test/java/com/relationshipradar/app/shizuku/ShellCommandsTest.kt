package com.relationshipradar.app.shizuku

import com.relationshipradar.app.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Trust-boundary invariants for the elevated process. If one of these fails, stop and think. */
class ShellCommandsTest {
    private val pkg = BuildConfig.APPLICATION_ID

    @Test fun `every command names only our own package`() {
        for (c in ShellCommands.Command.entries) {
            val joined = c.argv.joinToString(" ")
            assertTrue("${c.name} must reference $pkg", joined.contains(pkg))
            // No shell metacharacters: argv is exec'd directly, never through sh -c.
            assertTrue("${c.name} has shell metachars", c.argv.none { a -> a.any { it in ";|&`$<>\n" } })
        }
    }

    @Test fun `no command can grant a permission we did not declare`() {
        val declared = setOf("READ_CALL_LOG", "READ_SMS", "READ_CONTACTS", "POST_NOTIFICATIONS", "READ_CALENDAR")
        ShellCommands.Command.entries.filter { it.argv.firstOrNull() == "pm" }.forEach { c ->
            val perm = c.argv.last().substringAfterLast('.')
            assertTrue("${c.name} grants undeclared $perm", perm in declared)
        }
    }

    @Test fun `ids are unique and round-trip`() {
        val ids = ShellCommands.Command.entries.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        ShellCommands.Command.entries.forEach { assertEquals(it, ShellCommands.Command.byId(it.id)) }
        ShellCommands.Query.entries.forEach { assertEquals(it, ShellCommands.Query.byId(it.id)) }
    }

    @Test fun `unknown ids resolve to nothing`() {
        assertNull(ShellCommands.Command.byId(0)); assertNull(ShellCommands.Command.byId(999)); assertNull(ShellCommands.Query.byId(-1))
    }

    @Test fun `hardening bundle never grants permissions`() =
        assertTrue(ShellCommands.hardening.none { it.argv.firstOrNull() == "pm" })
}
