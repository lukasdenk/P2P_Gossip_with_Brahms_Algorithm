package api

class APIModule(val port: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as APIModule

        if (port != other.port) return false

        return true
    }

    override fun hashCode(): Int {
        return port
    }
}