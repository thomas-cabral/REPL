import kotlin.system.exitProcess

fun main() {
    val store = REPLDataImpl()
    var running = true
    println("welcome to REPL")

    while (running) {
        val input = readLine()
        val response = ReplUI(store).validateRespond(input)
        println(response.second)
        running = response.first != Commands.QUIT.name
    }

    exitProcess(0)
}

class ReplUI(private val store: REPLData) {
    private fun runCommand(command: String?, key: String?, value: String?) : String {
        return when (command) {
            Commands.READ.name -> readValue(key)
            Commands.WRITE.name -> writeValue(key, value)
            Commands.DELETE.name -> deleteValue(key)
            Commands.START.name -> startTransaction()
            Commands.COMMIT.name -> commitTransaction()
            Commands.ABORT.name -> abortTransaction()
            Commands.QUIT.name -> "Exiting..."
            else -> "Unknown Command"
        }
    }

    private fun readValue(key: String?): String {
        return if (key != null) {
            val value = store.read(key)
            if (value.isNullOrEmpty()) {
                "Key not found: $key"
            } else {
                value.toString()
            }
        } else {
            "Key is Null"
        }
    }

    private fun writeValue(key: String?, value: String?): String {
        if (key != null && value != null) {
            val pair: Pair<String, String> = Pair(key, value)
            store.write(pair)
            return "Wrote $pair"
        }
        return "Invalid input $key: $value"
    }

    private fun deleteValue(key: String?): String {
        if (key != null) {
            store.delete(key)
            return "Deleted $key"
        }

        return "Please enter a key"
    }

    private fun startTransaction(): String {
        store.start()
        return "Started Transaction"
    }

    private fun commitTransaction(): String {
        store.commit()
        return "Committed Transaction"
    }

    private fun abortTransaction(): String{
        store.abort()
        return "Aborted Transaction"
    }

    fun validateRespond(input: String?) : Pair<String?, String> {
        val splitInput = input?.split(" ")
        val command = splitInput?.getOrNull(0)?.toUpperCase()
        val key = splitInput?.getOrNull(1)
        val value = splitInput?.getOrNull(2)
        if (input.isNullOrEmpty() || !Commands.values().map { it.name }.contains(command)) {
            return Pair("", "Invalid Command $input")
        }

        return Pair(command, runCommand(command, key, value))
    }
}