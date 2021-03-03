import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class REPLTest {
    private val seed = mutableMapOf(Pair("a", "hello"), Pair("b", "goodbye"))
    @Test
    fun `should read a value by key`() {
        val repl = REPLDataImpl(seed)
        assertEquals("hello", repl.read("a"))
    }

    @Test
    fun `should write a value`() {
        val repl = REPLDataImpl()
        repl.write(Pair("a", "hello"))

        assertEquals("hello", repl.read("a"))
    }

    @Test
    fun `delete should remove pair`() {
        val repl = REPLDataImpl(seed)
        repl.delete("a")

        assertFalse { repl.store.containsKey("a") }
    }

    @Test
    fun `should handle transaction commit`() {
        val repl = REPLDataImpl(seed)
        repl.start()
        repl.write(Pair("a", "goodbye"))
        repl.write(Pair("c", "ciao"))
        repl.commit()

        assertEquals("goodbye", repl.read("a"))
        assertEquals("goodbye", repl.read("b"))
        assertEquals("ciao", repl.read("c"))
    }

    @Test
    fun `should handle nested transactions`() {
        val repl = REPLDataImpl(seed)
        repl.start()
        repl.write(Pair("a", "goodbye"))
        repl.write(Pair("c", "ciao"))
        repl.start()

        assertEquals("goodbye", repl.read("a"))
        assertEquals("goodbye", repl.read("b"))
        assertEquals("ciao", repl.read("c"))

        repl.write(Pair("a", "hello-transaction"))
        repl.write(Pair("b", "hello-as-well"))
        repl.write(Pair("c", "ciao!"))
        repl.commit()
        repl.commit()

        assertEquals("hello-transaction", repl.read("a"))
        assertEquals("hello-as-well", repl.read("b"))
        assertEquals("ciao!", repl.read("c"))
    }

    @Test
    fun `should commit one transaction at a time`() {
        val repl = REPLDataImpl()
        repl.write(Pair("a", "hello"))
        repl.start()
        repl.write(Pair("a", "goodbye"))
        repl.write(Pair("c", "ciao"))
        repl.start()

        assertEquals("goodbye", repl.read("a"))
        assertEquals("ciao", repl.read("c"))

        repl.write(Pair("a", "hello-again"))
        repl.write(Pair("b", "hello-as-well"))
        repl.write(Pair("c", "ciao!"))
        repl.commit()

        repl.write(Pair("a", "once-more"))

        repl.abort()

        assertEquals("hello", repl.read("a"))
        assertEquals(null, repl.read("b"))
        assertEquals(null, repl.read("c"))
    }

    @Test
    fun `should abort transaction and resume previous transaction`() {
        val repl = REPLDataImpl(seed)
        repl.start()
        repl.write(Pair("a", "goodbye"))
        repl.write(Pair("c", "ciao"))

        repl.start()
        repl.write(Pair("a", "hello"))
        repl.write(Pair("b", "hello-as-well"))
        repl.write(Pair("c", "ciao!"))
        repl.abort()

        assertEquals("goodbye", repl.read("a"))
        assertEquals("goodbye", repl.read("b"))
        assertEquals("ciao", repl.read("c"))
    }
}