package io.github.domi04151309.batterytool.helpers

class PseudoHashSet : HashSet<String>() {
    override fun contains(element: String): Boolean = false
}