package com.sebastianneubauer.jsontree

import androidx.compose.runtime.mutableStateOf
import com.sebastianneubauer.jsontree.JsonTreeElement.Collapsable.Array
import com.sebastianneubauer.jsontree.JsonTreeElement.Collapsable.Object
import com.sebastianneubauer.jsontree.JsonTreeElement.EndBracket
import com.sebastianneubauer.jsontree.JsonTreeElement.ParentType
import com.sebastianneubauer.jsontree.JsonTreeElement.Primitive
import com.sebastianneubauer.jsontree.JsonTreeParserState.Loading
import com.sebastianneubauer.jsontree.JsonTreeParserState.Parsing.Error
import com.sebastianneubauer.jsontree.JsonTreeParserState.Parsing.Parsed
import com.sebastianneubauer.jsontree.JsonTreeParserState.Ready
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.util.concurrent.atomic.AtomicLong

internal class JsonTreeParser(
    private val json: String,
) {
    private var parserState = mutableStateOf<JsonTreeParserState>(Loading)
    val state = parserState

    fun init(initialState: TreeState) {
        val parsingState = runCatching {
            Parsed(Json.parseToJsonElement(json))
        }.getOrElse { throwable ->
            Error(throwable)
        }

        parserState.value = when (parsingState) {
            is Parsed -> {
                Ready(
                    list = parsingState.jsonElement
                        .toJsonTree(
                            idGenerator = AtomicLong(),
                            state = initialState,
                            level = 0,
                            key = null,
                            isLastItem = true,
                            parentType = ParentType.NONE
                        ).toList()
                )
            }
            is Error -> parsingState
        }
    }

    fun expandOrCollapseItem(item: JsonTreeElement) {
        val state = parserState.value
        check(state is Ready)

        val newList = when (item) {
            is Primitive -> error("Primitive can't be clicked")
            is EndBracket -> error("EndBracket can't be clicked")
            is Array -> {
                when (item.state) {
                    TreeState.COLLAPSED -> state.list.expandItem(item)
                    TreeState.EXPANDED,
                    TreeState.FIRST_ITEM_EXPANDED -> state.list.collapseItem(item)
                }
            }
            is Object -> {
                when (item.state) {
                    TreeState.COLLAPSED -> state.list.expandItem(item)
                    TreeState.EXPANDED,
                    TreeState.FIRST_ITEM_EXPANDED -> state.list.collapseItem(item)
                }
            }
        }

        parserState.value = state.copy(newList)
    }

    private fun List<JsonTreeElement>.collapseItem(item: JsonTreeElement.Collapsable): List<JsonTreeElement> {
        return toMutableList().apply {
            val newItem = when (item) {
                is Object -> item.copy(state = TreeState.COLLAPSED, children = item.children.collapse())
                is Array -> item.copy(state = TreeState.COLLAPSED, children = item.children.collapse())
            }
            val itemIndex = indexOfFirst { it.id == item.id }
            val endBracketIndex = indexOfFirst { it.id == item.endBracket.id }
            subList(itemIndex, endBracketIndex + 1).clear()
            add(itemIndex, newItem)
        }
    }

    private fun Map<String, JsonTreeElement>.collapse(): Map<String, JsonTreeElement> {
        return mapValues {
            when (val child = it.value) {
                is Primitive -> child
                is EndBracket -> child
                is Array -> {
                    child.copy(
                        state = TreeState.COLLAPSED,
                        children = child.children.collapse()
                    )
                }
                is Object -> {
                    child.copy(
                        state = TreeState.COLLAPSED,
                        children = child.children.collapse()
                    )
                }
            }
        }
    }

    private fun List<JsonTreeElement>.expandItem(item: JsonTreeElement.Collapsable): List<JsonTreeElement> {
        return toMutableList().apply {
            val newItem = when (item) {
                is Object -> item.copy(state = TreeState.EXPANDED)
                is Array -> item.copy(state = TreeState.EXPANDED)
            }
            val itemIndex = indexOfFirst { it.id == item.id }
            removeAt(itemIndex)
            addAll(itemIndex, listOf(newItem) + item.children.values + item.endBracket)
        }
    }

    private fun JsonElement.toJsonTree(
        idGenerator: AtomicLong,
        state: TreeState,
        level: Int,
        key: String?,
        isLastItem: Boolean,
        parentType: ParentType,
    ): JsonTreeElement {
        return when (this) {
            is JsonPrimitive -> {
                Primitive(
                    id = idGenerator.incrementAndGet().toString(),
                    level = level,
                    key = key,
                    value = this,
                    isLastItem = isLastItem,
                    parentType = parentType,
                )
            }
            is JsonArray -> {
                val childElements = jsonArray.mapIndexed { index, item ->
                    Pair(
                        index.toString(),
                        item.toJsonTree(
                            idGenerator = idGenerator,
                            state = if (state == TreeState.FIRST_ITEM_EXPANDED) TreeState.COLLAPSED else state,
                            level = level + 1,
                            key = index.toString(),
                            isLastItem = index == (jsonArray.size - 1),
                            parentType = ParentType.ARRAY,
                        )
                    )
                }
                    .toMap()

                Array(
                    id = idGenerator.incrementAndGet().toString(),
                    level = level,
                    state = state,
                    key = key,
                    children = childElements,
                    isLastItem = isLastItem,
                    parentType = parentType,
                )
            }
            is JsonObject -> {
                val childElements = jsonObject.entries.associate {
                    Pair(
                        it.key,
                        it.value.toJsonTree(
                            idGenerator = idGenerator,
                            state = if (state == TreeState.FIRST_ITEM_EXPANDED) TreeState.COLLAPSED else state,
                            level = level + 1,
                            key = it.key,
                            isLastItem = it == jsonObject.entries.last(),
                            parentType = ParentType.OBJECT
                        )
                    )
                }

                Object(
                    id = idGenerator.incrementAndGet().toString(),
                    level = level,
                    state = state,
                    key = key,
                    children = childElements,
                    isLastItem = isLastItem,
                    parentType = parentType,
                )
            }
        }
    }

    private fun JsonTreeElement.toList(): List<JsonTreeElement> {
        val list = mutableListOf<JsonTreeElement>()

        fun addToList(jsonTree: JsonTreeElement) {
            when (jsonTree) {
                is EndBracket -> error("EndBracket in initial list creation")
                is Primitive -> list.add(jsonTree)
                is Array -> {
                    list.add(jsonTree)
                    if (jsonTree.state != TreeState.COLLAPSED) {
                        jsonTree.children.forEach {
                            addToList(it.value)
                        }
                        list.add(jsonTree.endBracket)
                    }
                }
                is Object -> {
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
}
