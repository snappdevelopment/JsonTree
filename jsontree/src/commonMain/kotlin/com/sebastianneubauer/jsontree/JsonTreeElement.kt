package com.sebastianneubauer.jsontree

internal sealed interface JsonTreeElement {
    val id: String
    val level: Int
    val isLastItem: Boolean

    enum class ParentType { NONE, ARRAY, OBJECT }

    data class Primitive(
        override val id: String,
        override val level: Int,
        override val isLastItem: Boolean,
        val key: String?,
        val type: Type,
        val value: String,
        val parentType: ParentType,
    ) : JsonTreeElement {
        enum class Type { STRING, BOOLEAN, NUMBER, OTHER }
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
        ) : Collapsable

        data class Array(
            override val id: String,
            override val level: Int,
            override val state: TreeState,
            override val children: Map<String, JsonTreeElement>,
            override val isLastItem: Boolean,
            val key: String?,
            val parentType: ParentType,
        ) : Collapsable
    }

    data class EndBracket(
        override val id: String,
        override val level: Int,
        override val isLastItem: Boolean,
        val type: Type
    ) : JsonTreeElement {
        enum class Type { ARRAY, OBJECT }
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
