package com.sebastianneubauer.jsontree

import androidx.compose.runtime.State
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
import com.sebastianneubauer.jsontree.util.Expansion
import com.sebastianneubauer.jsontree.util.collapse
import com.sebastianneubauer.jsontree.util.expand
import com.sebastianneubauer.jsontree.util.toList
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

internal class JsonTreeParser(
    private val json: String,
    private val defaultDispatcher: CoroutineDispatcher,
    private val mainDispatcher: CoroutineDispatcher,
) {
    private var parserState = mutableStateOf<JsonTreeParserState>(Loading)
    val state: State<JsonTreeParserState> = parserState

    suspend fun init(initialState: TreeState) = withContext(defaultDispatcher) {
        val parsingState = runCatching {
            Parsed(Json.parseToJsonElement(json))
        }.getOrElse { throwable ->
            Error(throwable)
        }

        val state = when (parsingState) {
            is Parsed -> {
                Ready(
                    list = parsingState.jsonElement
                        .toJsonTreeElement(
                            idGenerator = AtomicLongWrapper(),
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

        withContext(mainDispatcher) {
            parserState.value = state
        }
    }

    suspend fun expandAllItems(): List<JsonTreeElement> = withContext(defaultDispatcher) {
        val state = parserState.value
        check(state is Ready)

        // take root element and expand everything
        val expandedList = state.list.first().expand(expansion = Expansion.All).toList()

        withContext(mainDispatcher) {
            parserState.value = state.copy(expandedList)
        }
        expandedList
    }

    suspend fun expandOrCollapseItem(
        item: JsonTreeElement,
        expandSingleChildren: Boolean
    ) = withContext(defaultDispatcher) {
        val state = parserState.value
        check(state is Ready)

        val newList = when (item) {
            is Primitive -> error("Primitive can't be clicked")
            is EndBracket -> error("EndBracket can't be clicked")
            is Array -> {
                when (item.state) {
                    TreeState.COLLAPSED -> state.list.expandItem(item, expandSingleChildren)
                    TreeState.EXPANDED,
                    TreeState.FIRST_ITEM_EXPANDED -> state.list.collapseItem(item)
                }
            }
            is Object -> {
                when (item.state) {
                    TreeState.COLLAPSED -> state.list.expandItem(item, expandSingleChildren)
                    TreeState.EXPANDED,
                    TreeState.FIRST_ITEM_EXPANDED -> state.list.collapseItem(item)
                }
            }
        }

        withContext(mainDispatcher) {
            parserState.value = state.copy(newList)
        }
    }

    private fun List<JsonTreeElement>.collapseItem(
        item: JsonTreeElement.Collapsable
    ): List<JsonTreeElement> {
        val newItem = item.collapse()
        var itemIndex = -1
        var endBracketIndex = -1

        for (i in indices) {
            val element = get(i)
            if (itemIndex == -1 && element.id == item.id) {
                itemIndex = i
            } else if (itemIndex != -1 && element.id == item.endBracket.id) {
                endBracketIndex = i
                break
            }
        }

        return buildList(size - (endBracketIndex - itemIndex)) {
            addAll(this@collapseItem.subList(0, itemIndex))
            add(newItem)
            addAll(this@collapseItem.subList(endBracketIndex + 1, this@collapseItem.size))
        }
    }

    private fun List<JsonTreeElement>.expandItem(
        item: JsonTreeElement.Collapsable,
        expandSingleChildren: Boolean
    ): List<JsonTreeElement> {
        val newItems = item
            .expand(expansion = if (expandSingleChildren) Expansion.SingleOnly else Expansion.None)
            .toList()

        val itemIndex = indexOfFirst { it.id == item.id }

        return buildList(size - 1 + newItems.size) {
            addAll(this@expandItem.subList(0, itemIndex))
            addAll(newItems)
            addAll(this@expandItem.subList(itemIndex + 1, this@expandItem.size))
        }
    }

    private fun JsonElement.toJsonTreeElement(
        idGenerator: AtomicLongWrapper,
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
                val childElements = jsonArray.withIndex().associate { (index, item) ->
                    index.toString() to item.toJsonTreeElement(
                        idGenerator = idGenerator,
                        state = if (state == TreeState.FIRST_ITEM_EXPANDED) TreeState.COLLAPSED else state,
                        level = level + 1,
                        key = index.toString(),
                        isLastItem = index == (jsonArray.size - 1),
                        parentType = ParentType.ARRAY,
                    )
                }

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
                    it.key to it.value.toJsonTreeElement(
                        idGenerator = idGenerator,
                        state = if (state == TreeState.FIRST_ITEM_EXPANDED) TreeState.COLLAPSED else state,
                        level = level + 1,
                        key = it.key,
                        isLastItem = it == jsonObject.entries.last(),
                        parentType = ParentType.OBJECT
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
}

internal class AtomicLongWrapper {
    private val atomicLong = atomic(0L)
    fun incrementAndGet(): Long {
        return atomicLong.incrementAndGet()
    }
}
