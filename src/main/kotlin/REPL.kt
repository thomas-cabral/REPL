interface REPL {
    fun read(key: String) : String?
    fun write(keyVal: Pair<String, String>)
    fun delete(key: String)
    fun start()
    fun commit()
    fun abort()
}

class REPLImpl(val store: MutableMap<String, String> = mutableMapOf()) : REPL {
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
        store.clear()
        pendingTransactions.last().forEach {
            store[it.key] = it.value
        }
        pendingTransactions.clear()
    }

    override fun abort() {
        pendingTransactions.removeLast()
    }

}