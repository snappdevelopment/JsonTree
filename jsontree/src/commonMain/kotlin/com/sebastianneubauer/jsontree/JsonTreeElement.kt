package com.sebastianneubauer.jsontree

import kotlinx.serialization.json.JsonPrimitive

internal sealed interface JsonTreeElement {
    val id: String
    val level: Int
    val isLastItem: Boolean

    fun hasMatch(searchKeyValue: String?): Boolean

    fun childrenHasMatch(searchKeyValue: String?): Boolean

    enum class ParentType { NONE, ARRAY, OBJECT }

    data class Primitive(
        override val id: String,
        override val level: Int,
        override val isLastItem: Boolean,
        val key: String?,
        val value: JsonPrimitive,
        val parentType: ParentType,
    ) : JsonTreeElement {

        override fun hasMatch(searchKeyValue: String?): Boolean {
            if (searchKeyValue.isNullOrEmpty()) return false
            return (key?.contains(searchKeyValue, ignoreCase = true) == true) ||
                childrenHasMatch(searchKeyValue)
        }

        override fun childrenHasMatch(searchKeyValue: String?): Boolean {
            if (searchKeyValue.isNullOrEmpty()) return false
            return value.content.contains(searchKeyValue, ignoreCase = true)
        }
    }

    sealed interface Collapsable : JsonTreeElement {
        val state: TreeState
        val children: Map<String, JsonTreeElement>

        data class Object(
            override val id: String,
            override val level: Int,
            override val state: TreeState,
            override val children: Map<String, JsonTreeElement>,
            override val isLastItem: Boolean,
            val key: String?,
            val parentType: ParentType,
        ) : Collapsable {

            override fun hasMatch(searchKeyValue: String?): Boolean {
                if (searchKeyValue.isNullOrEmpty()) return false
                return (key?.contains(searchKeyValue, ignoreCase = true) == true) ||
                    (state == TreeState.COLLAPSED && childrenHasMatch(searchKeyValue))
            }

            override fun childrenHasMatch(searchKeyValue: String?): Boolean {
                if (searchKeyValue.isNullOrEmpty()) return false
                return children.values.any { it.hasMatch(searchKeyValue) }
            }
        }

        data class Array(
            override val id: String,
            override val level: Int,
            override val state: TreeState,
            override val children: Map<String, JsonTreeElement>,
            override val isLastItem: Boolean,
            val key: String?,
            val parentType: ParentType,
        ) : Collapsable {

            override fun hasMatch(searchKeyValue: String?): Boolean {
                if (searchKeyValue.isNullOrEmpty()) return false
                return (key?.contains(searchKeyValue, ignoreCase = true) == true) ||
                    (state == TreeState.COLLAPSED && childrenHasMatch(searchKeyValue))
            }

            override fun childrenHasMatch(searchKeyValue: String?): Boolean {
                if (searchKeyValue.isNullOrEmpty()) return false
                return children.values.any { it.hasMatch(searchKeyValue) }
            }
        }
    }

    data class EndBracket(
        override val id: String,
        override val level: Int,
        override val isLastItem: Boolean,
        val type: Type
    ) : JsonTreeElement {
        enum class Type { ARRAY, OBJECT }

        override fun hasMatch(searchKeyValue: String?) = false
        override fun childrenHasMatch(searchKeyValue: String?) = false
    }
}

internal val JsonTreeElement.Collapsable.endBracket: JsonTreeElement.EndBracket
    get() = JsonTreeElement.EndBracket(
        id = "$id-b",
        level = level,
        isLastItem = isLastItem,
        type = when (this) {
            is JsonTreeElement.Collapsable.Object -> JsonTreeElement.EndBracket.Type.OBJECT
            is JsonTreeElement.Collapsable.Array -> JsonTreeElement.EndBracket.Type.ARRAY
        }
    )
