package com.sebastianneubauer.jsontree.util

import com.sebastianneubauer.jsontree.JsonTreeElement
import com.sebastianneubauer.jsontree.JsonTreeElement.Collapsable.Array
import com.sebastianneubauer.jsontree.JsonTreeElement.Collapsable.Object
import com.sebastianneubauer.jsontree.JsonTreeElement.EndBracket
import com.sebastianneubauer.jsontree.JsonTreeElement.Primitive
import com.sebastianneubauer.jsontree.TreeState
import com.sebastianneubauer.jsontree.endBracket

internal enum class Expansion {
    /**
     * No children are expanded.
     */
    None,

    /**
     * All children are expanded.
     */
    All,

    /**
     * Only children without siblings are expanded.
     */
    SingleOnly
}

/**
 * Expands a JsonTreeElement and its children depending on which [expansion] is chosen.
 *
 * `Expansion.None` -> Children will not be expanded.
 *
 * `Expansion.All` -> Children will be expanded recursively.
 *
 * `Expansion.SingleOnly` -> Only children without siblings will be expanded.
 */
internal fun JsonTreeElement.expand(
    expansion: Expansion,
): JsonTreeElement {
    return when (this) {
        is Array -> this.copy(
            state = TreeState.EXPANDED,
            children = when (expansion) {
                Expansion.None -> children
                Expansion.All -> children.expandChildren(singleChildrenOnly = false)
                Expansion.SingleOnly -> children.expandChildren(singleChildrenOnly = true)
            }
        )

        is Object -> this.copy(
            state = TreeState.EXPANDED,
            children = when (expansion) {
                Expansion.None -> children
                Expansion.All -> children.expandChildren(singleChildrenOnly = false)
                Expansion.SingleOnly -> children.expandChildren(singleChildrenOnly = true)
            }
        )

        is Primitive,
        is EndBracket -> this
    }
}

private fun Map<String, JsonTreeElement>.expandChildren(
    singleChildrenOnly: Boolean
): Map<String, JsonTreeElement> {
    if (singleChildrenOnly && this.size > 1) {
        return this
    }

    var hasChanges = false
    val result = LinkedHashMap<String, JsonTreeElement>(size)

    for ((key, child) in this) {
        val newChild = when (child) {
            is Primitive, is EndBracket -> child
            is Array -> {
                if (child.state == TreeState.COLLAPSED) {
                    hasChanges = true
                    child.copy(
                        state = TreeState.EXPANDED,
                        children = child.children.expandChildren(singleChildrenOnly)
                    )
                } else {
                    child
                }
            }
            is Object -> {
                if (child.state == TreeState.COLLAPSED) {
                    hasChanges = true
                    child.copy(
                        state = TreeState.EXPANDED,
                        children = child.children.expandChildren(singleChildrenOnly)
                    )
                } else {
                    child
                }
            }
        }
        result[key] = newChild
    }

    return if (hasChanges) result else this
}

/**
 * Collapses a JsonTreeElement and all its children.
 */
internal fun JsonTreeElement.collapse(): JsonTreeElement {
    return when (this) {
        is Array -> this.copy(
            state = TreeState.COLLAPSED,
            children = children.collapseChildren()
        )

        is Object -> this.copy(
            state = TreeState.COLLAPSED,
            children = children.collapseChildren()
        )

        is Primitive,
        is EndBracket -> this
    }
}

private fun Map<String, JsonTreeElement>.collapseChildren(): Map<String, JsonTreeElement> {
    var hasChanges = false
    val result = LinkedHashMap<String, JsonTreeElement>(size)

    for ((key, child) in this) {
        val newChild = when (child) {
            is Primitive, is EndBracket -> child
            is Array -> {
                if (child.state != TreeState.COLLAPSED) {
                    hasChanges = true
                    child.copy(
                        state = TreeState.COLLAPSED,
                        children = child.children.collapseChildren()
                    )
                } else {
                    child
                }
            }
            is Object -> {
                if (child.state != TreeState.COLLAPSED) {
                    hasChanges = true
                    child.copy(
                        state = TreeState.COLLAPSED,
                        children = child.children.collapseChildren()
                    )
                } else {
                    child
                }
            }
        }
        result[key] = newChild
    }

    return if (hasChanges) result else this
}

/**
 * Converts a JsonTreeElement into a list which can be rendered.
 */
internal fun JsonTreeElement.toList(): List<JsonTreeElement> {
    return buildList {
        addTreeElement(this@toList)
    }
}

private fun MutableList<JsonTreeElement>.addTreeElement(element: JsonTreeElement) {
    when (element) {
        is EndBracket, is Primitive -> add(element)
        is Array -> {
            add(element)
            if (element.state != TreeState.COLLAPSED) {
                for (child in element.children.values) {
                    addTreeElement(child)
                }
                add(element.endBracket)
            }
        }
        is Object -> {
            add(element)
            if (element.state != TreeState.COLLAPSED) {
                for (child in element.children.values) {
                    addTreeElement(child)
                }
                add(element.endBracket)
            }
        }
    }
}
