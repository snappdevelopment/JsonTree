package com.sebastianneubauer.jsontree

import androidx.compose.runtime.mutableStateOf
import com.sebastianneubauer.jsontree.JsonTree.CollapsableElement.ArrayElement
import com.sebastianneubauer.jsontree.JsonTree.CollapsableElement.ObjectElement
import com.sebastianneubauer.jsontree.JsonTree.EndBracket
import com.sebastianneubauer.jsontree.JsonTree.NullElement
import com.sebastianneubauer.jsontree.JsonTree.PrimitiveElement
import com.sebastianneubauer.jsontree.ParserState.Loading
import com.sebastianneubauer.jsontree.ParserState.Parsing.Error
import com.sebastianneubauer.jsontree.ParserState.Parsing.Parsed
import com.sebastianneubauer.jsontree.ParserState.Ready
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.util.UUID

internal class JsonViewModel(
    private val json: String,
) {
    private var parserState = mutableStateOf<ParserState>(Loading)
    val state = parserState

    fun initJsonParser(initialState: TreeState) {
        val parsingState = runCatching {
            Parsed(Json.parseToJsonElement(json))
        }.getOrElse { throwable ->
            Error(throwable)
        }

        parserState.value = when (parsingState) {
            is Parsed -> {
                Ready(
                    list = parsingState.jsonElement.toJsonTree(
                        state = initialState,
                        level = 0,
                        key = null,
                        isLastItem = true
                    ).toRenderList()
                )
            }
            is Error -> parsingState
        }
    }

    private fun JsonTree.toRenderList(): List<JsonTree> {
        val list = mutableListOf<JsonTree>()

        fun addToList(jsonTree: JsonTree) {
            when (jsonTree) {
                is EndBracket -> error("EndBracket in initial list creation")
                is PrimitiveElement -> list.add(jsonTree)
                is NullElement -> list.add(jsonTree)
                is ArrayElement -> {
                    list.add(jsonTree)
                    if (jsonTree.state != TreeState.COLLAPSED) {
                        jsonTree.children.forEach {
                            addToList(it.value)
                        }
                        list.add(jsonTree.endBracket)
                    }
                }
                is ObjectElement -> {
                    list.add(jsonTree)
                    if (jsonTree.state != TreeState.COLLAPSED) {
                        jsonTree.children.forEach {
                            addToList(it.value)
                        }
                        list.add(jsonTree.endBracket)
                    }
                }
            }
        }

        addToList(this)
        return list
    }

    fun expandOrCollapseItem(item: JsonTree) {
        val state = parserState.value
        check(state is Ready)

        val newList = when (item) {
            is PrimitiveElement -> error("PrimitiveElement can't be clicked")
            is NullElement -> error("NullElement can't be clicked")
            is EndBracket -> error("EndBracket can't be clicked")
            is ArrayElement -> {
                when (item.state) {
                    TreeState.COLLAPSED -> state.list.expandItem(item)
                    TreeState.EXPANDED,
                    TreeState.FIRST_ITEM_EXPANDED -> state.list.collapseItem(item)
                }
            }
            is ObjectElement -> {
                when (item.state) {
                    TreeState.COLLAPSED -> state.list.expandItem(item)
                    TreeState.EXPANDED,
                    TreeState.FIRST_ITEM_EXPANDED -> state.list.collapseItem(item)
                }
            }
        }

        parserState.value = state.copy(newList)
    }

    private fun List<JsonTree>.collapseItem(item: JsonTree.CollapsableElement): List<JsonTree> {
        return toMutableList().apply {
            val newItem = when (item) {
                is ObjectElement -> item.copy(state = TreeState.COLLAPSED)
                is ArrayElement -> item.copy(state = TreeState.COLLAPSED)
            }
            val itemIndex = indexOf(item)
            val endBracketIndex = indexOf(item.endBracket)
            val itemsToRemove = slice(itemIndex..endBracketIndex)
            removeAll(itemsToRemove)
            add(itemIndex, newItem)
        }
    }

    private fun List<JsonTree>.expandItem(item: JsonTree.CollapsableElement): List<JsonTree> {
        return toMutableList().apply {
            val newItem = when (item) {
                is ObjectElement -> item.copy(state = TreeState.EXPANDED)
                is ArrayElement -> item.copy(state = TreeState.EXPANDED)
            }
            val itemIndex = indexOf(item)
            removeAt(itemIndex)
            addAll(itemIndex, listOf(newItem) + item.children.values + item.endBracket)
        }
    }
}

internal sealed interface ParserState {
    object Loading : ParserState
    data class Ready(val list: List<JsonTree>) : ParserState

    sealed interface Parsing : ParserState {
        data class Parsed(val jsonElement: JsonElement) : Parsing
        data class Error(val throwable: Throwable) : Parsing
    }
}

internal fun JsonElement.toJsonTree(
    state: TreeState,
    level: Int,
    key: String?,
    isLastItem: Boolean,
): JsonTree {
    return when (this) {
        is JsonPrimitive -> {
            PrimitiveElement(
                id = UUID.randomUUID().toString(),
                level = level,
                key = key,
                value = this,
                isLastItem = isLastItem,
            )
        }
        is JsonNull -> {
            NullElement(
                id = UUID.randomUUID().toString(),
                level = level,
                key = key,
                value = this,
                isLastItem = isLastItem,
            )
        }
        is JsonArray -> {
            val childElements = jsonArray
                .mapIndexed { index, item -> Pair(index.toString(), item) }
                .toMap()
                .mapValues {
                    it.value.toJsonTree(
                        state = TreeState.COLLAPSED,
                        level = level + 1,
                        key = it.key,
                        isLastItem = it.key == (jsonArray.size - 1).toString()
                    )
                }

            ArrayElement(
                id = UUID.randomUUID().toString(),
                level = level,
                state = state,
                key = key,
                children = childElements,
                isLastItem = isLastItem,
            )
        }
        is JsonObject -> {
            val childElements = jsonObject.entries
                .associate { it.toPair() }
                .mapValues {
                    it.value.toJsonTree(
                        state = TreeState.COLLAPSED,
                        level = level + 1,
                        key = it.key,
                        isLastItem = it == jsonObject.entries.last()
                    )
                }

            ObjectElement(
                id = UUID.randomUUID().toString(),
                level = level,
                state = state,
                key = key,
                children = childElements,
                isLastItem = isLastItem,
            )
        }
    }
}

internal sealed interface JsonTree {
    val id: String
    val level: Int
    val isLastItem: Boolean

    data class PrimitiveElement(
        override val id: String,
        override val level: Int,
        override val isLastItem: Boolean,
        val key: String?,
        val value: JsonPrimitive,
    ) : JsonTree

    data class NullElement(
        override val id: String,
        override val level: Int,
        override val isLastItem: Boolean,
        val key: String?,
        val value: JsonPrimitive,
    ) : JsonTree

    sealed interface CollapsableElement : JsonTree {
        val state: TreeState
        val children: Map<String, JsonTree>

        data class ObjectElement(
            override val id: String,
            override val level: Int,
            override val state: TreeState,
            override val children: Map<String, JsonTree>,
            override val isLastItem: Boolean,
            val key: String?,
        ) : CollapsableElement

        data class ArrayElement(
            override val id: String,
            override val level: Int,
            override val state: TreeState,
            override val children: Map<String, JsonTree>,
            override val isLastItem: Boolean,
            val key: String?,
        ) : CollapsableElement
    }

    data class EndBracket(
        override val id: String,
        override val level: Int,
        override val isLastItem: Boolean,
        val type: Type
    ) : JsonTree {
        enum class Type { ARRAY, OBJECT }
    }
}

private val JsonTree.CollapsableElement.endBracket: EndBracket
    get() = EndBracket(
        id = "$id-b",
        level = level,
        isLastItem = isLastItem,
        type = when (this) {
            is ObjectElement -> JsonTree.EndBracket.Type.OBJECT
            is ArrayElement -> JsonTree.EndBracket.Type.ARRAY
        }
    )
