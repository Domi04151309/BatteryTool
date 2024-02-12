package io.github.domi04151309.batterytool.helpers

class PseudoHashSet : Set<String> {
    override val size: Int
        get() = 0

    override fun isEmpty(): Boolean = true

    override fun iterator(): Iterator<String> = setOf<String>().iterator()

    override fun containsAll(elements: Collection<String>): Boolean = true

    override fun contains(element: String): Boolean = true
}
