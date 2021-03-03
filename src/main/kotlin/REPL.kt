interface REPLData {
    fun read(key: String) : String?
    fun write(keyVal: Pair<String, String>)
    fun delete(key: String)
    fun start()
    fun commit()
    fun abort()
}

enum class Commands {
    READ,
    WRITE,
    DELETE,
    START,
    COMMIT,
    ABORT,
    QUIT
}

class REPLDataImpl(val store: MutableMap<String, String> = mutableMapOf()) : REPLData {
    private val pendingTransactions = mutableListOf<MutableMap<String, String>>()

    private fun getActiveStore() : MutableMap<String, String> {
        if (pendingTransactions.isEmpty()) {
            return store
        }
        return pendingTransactions.last()
    }

    override fun read(key: String): String? {
        return getActiveStore()[key]
    }

    override fun write(keyVal: Pair<String, String>) {
        getActiveStore()[keyVal.first] = keyVal.second
    }

    override fun delete(key: String) {
        getActiveStore().remove(key)
    }

    override fun start() {
        pendingTransactions.add(getActiveStore().toMutableMap())
    }

    override fun commit() {
        val currentStore: MutableMap<String, String>
        val transactionListSize = pendingTransactions.size
        val nextTransactionIndex = pendingTransactions.lastIndex - 1
        currentStore = if (transactionListSize == 1) {
            store
        } else {
            pendingTransactions[nextTransactionIndex]
        }

        val currentTransaction =  pendingTransactions.last()

        currentStore.forEach {
            if (!currentTransaction.containsKey(it.key)) {
                currentStore.remove(it.key)
            }
        }

        currentTransaction.forEach {
            currentStore[it.key] = it.value
        }
        pendingTransactions.removeLast()
    }

    override fun abort() {
        if (pendingTransactions.isNotEmpty()) {
            pendingTransactions.removeLast()
        }
    }
}