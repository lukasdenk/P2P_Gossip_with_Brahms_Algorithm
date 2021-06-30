class CommonUtils {
    companion object {
        fun <T> randomSubSet(collection: Collection<T>, n: Int): MutableSet<T> {
            return collection.toList().shuffled().subList(0, n).toMutableSet()
        }
    }
}