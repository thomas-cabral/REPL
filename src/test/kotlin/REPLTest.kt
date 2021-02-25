import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class REPLTest {
    private val seed = mutableMapOf(Pair("a", "hello"), Pair("b", "goodbye"))
    @Test
    fun `should read a value by key`() {
        val repl = REPLImpl(seed)
        assertEquals("hello", repl.read("a"))
    }

    @Test
    fun `should write a value`() {
        val repl = REPLImpl()
        repl.write(Pair("a", "hello"))

        assertEquals("hello", repl.read("a"))
    }

    @Test
    fun `delete should remove pair`() {
        val repl = REPLImpl(seed)
        repl.delete("a")

        assertFalse { repl.store.containsKey("a") }
    }

    @Test
    fun `should handle transaction commit`() {
        val repl = REPLImpl(seed)
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
        val repl = REPLImpl(seed)
        repl.start()
        repl.write(Pair("a", "goodbye"))
        repl.write(Pair("c", "ciao"))
        repl.start()

        assertEquals("goodbye", repl.read("a"))
        assertEquals("goodbye", repl.read("b"))
        assertEquals("ciao", repl.read("c"))

        repl.write(Pair("a", "hello"))
        repl.write(Pair("b", "hello as well"))
        repl.write(Pair("c", "ciao!"))
        repl.commit()

        assertEquals("hello", repl.read("a"))
        assertEquals("hello as well", repl.read("b"))
        assertEquals("ciao!", repl.read("c"))
    }

    @Test
    fun `should abort transaction and resume previous transaction`() {
        val repl = REPLImpl(seed)
        repl.start()
        repl.write(Pair("a", "goodbye"))
        repl.write(Pair("c", "ciao"))

        repl.start()
        repl.write(Pair("a", "hello"))
        repl.write(Pair("b", "hello as well"))
        repl.write(Pair("c", "ciao!"))
        repl.abort()

        assertEquals("goodbye", repl.read("a"))
        assertEquals("goodbye", repl.read("b"))
        assertEquals("ciao", repl.read("c"))
    }
}