package com.sebastianneubauer.jsontree

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.util.UUID

// private fun main() {
//    jsonTree2(json = nestedJson)
// }

// @Composable
public fun jsonTree2(
//    modifier: Modifier = Modifier,
    json: String,
    initialState: TreeState = TreeState.FIRST_ITEM_EXPANDED,
//    colors: TreeColors = defaultLightColors,
//    icon: ImageVector = ImageVector.vectorResource(R.drawable.jsontree_arrow_right),
//    iconSize: Dp = 20.dp,
//    textStyle: TextStyle = LocalTextStyle.current,
    onError: (Throwable) -> Unit = {}
) {
    val jsonElement: JsonElement? = // remember(json) {
        runCatching {
            Json.parseToJsonElement(json)
        }.getOrElse { throwable ->
            onError(throwable)
            null
        }
    // }

    jsonElement?.let {
        val jsonTree = it.toJsonTree(
            state = initialState,
            level = 0,
            key = null
        )
        JsonViewModel(jsonTree)
    }
}

private class JsonViewModel(
    private val jsonTree: JsonTree
) {
    private var renderList = mutableListOf<JsonTree>()

    init {
        renderList = jsonTree.toRenderList()
        println("Render list:")
        renderList.forEach { println(it) }

        renderList = expandOrCollapseItemWithId(id = renderList[1].id)
        renderList = expandOrCollapseItemWithId(id = renderList[3].id)
        println()
        println("Expand item Render list:")
        renderList.forEach { println(it) }

        renderList = expandOrCollapseItemWithId(id = renderList[1].id)
        println()
        println("Collapse Render list:")
        renderList.forEach { println(it) }

        renderList = expandOrCollapseItemWithId(id = renderList[1].id)
        println()
        println("exp Render list:")
        renderList.forEach { println(it) }
    }

    private fun JsonTree.toRenderList(): MutableList<JsonTree> {
        val list = mutableListOf<JsonTree>()

        fun addToList(jsonTree: JsonTree) {
            when (jsonTree) {
                is JsonTree.EndBracket -> error("EndBracket in initial list creation")
                is JsonTree.PrimitiveElement -> list.add(jsonTree)
                is JsonTree.NullElement -> list.add(jsonTree)
                is JsonTree.ArrayElement -> {
                    list.add(jsonTree)
                    if (jsonTree.state != TreeState.COLLAPSED) {
                        jsonTree.children.forEach {
                            addToList(it.value)
                        }
                        list.add(jsonTree.endBracket)
                    }
                }
                is JsonTree.ObjectElement -> {
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

    fun expandOrCollapseItemWithId(id: String): MutableList<JsonTree> {
        val item = renderList.first { it.id == id }

        return when (item) {
            is JsonTree.PrimitiveElement -> error("PrimitiveElement can't be clicked")
            is JsonTree.NullElement -> error("NullElement can't be clicked")
            is JsonTree.EndBracket -> error("EndBracket can't be clicked")
            is JsonTree.ArrayElement -> {
                when (item.state) {
                    TreeState.EXPANDED -> {
                        renderList.toMutableList().apply {
                            val index = indexOf(item)
                            remove(item)
                            // children will still have the previous state
                            add(index, item.copy(state = TreeState.COLLAPSED))
                            val childIds = item.children.values.flatMap { it.getIds() }.toSet()
                            val endBracketId = item.endBracket.id
                            removeAll { it.id in (childIds + endBracketId) }
                        }
                    }
                    TreeState.COLLAPSED -> {
                        renderList.toMutableList().apply {
                            val index = indexOf(item)
                            remove(item)
                            add(index, item.copy(state = TreeState.EXPANDED))
                            addAll(index + 1, item.children.values + item.endBracket)
                        }
                    }
                    TreeState.FIRST_ITEM_EXPANDED -> {
                        renderList.toMutableList().apply {
                            val index = indexOf(item)
                            remove(item)
                            // children will still have the previous state
                            add(index, item.copy(state = TreeState.COLLAPSED))
                            val childIds = item.children.values.flatMap { it.getIds() }.toSet()
                            val endBracketId = item.endBracket.id
                            removeAll { it.id in (childIds + endBracketId) }
                        }
                    }
                }
            }
            is JsonTree.ObjectElement -> {
                when (item.state) {
                    TreeState.EXPANDED -> {
                        renderList.toMutableList().apply {
                            val index = indexOf(item)
                            remove(item)
                            // children will still have the previous state
                            add(index, item.copy(state = TreeState.COLLAPSED))
                            val childIds = item.children.values.flatMap { it.getIds() }.toSet()
                            val endBracketId = item.endBracket.id
                            removeAll { it.id in (childIds + endBracketId) }
                        }
                    }
                    TreeState.COLLAPSED -> {
                        renderList.toMutableList().apply {
                            val index = indexOf(item)
                            remove(item)
                            add(index, item.copy(state = TreeState.EXPANDED))
                            addAll(index + 1, item.children.values + item.endBracket)
                        }
                    }
                    TreeState.FIRST_ITEM_EXPANDED -> {
                        renderList.toMutableList().apply {
                            val index = indexOf(item)
                            remove(item)
                            // children will still have the previous state
                            add(index, item.copy(state = TreeState.COLLAPSED))
                            val childIds = item.children.values.flatMap { it.getIds() }.toSet()
                            val endBracketId = item.endBracket.id
                            removeAll { it.id in (childIds + endBracketId) }
                        }
                    }
                }
            }
        }
    }

    private fun JsonTree.getIds(): List<String> {
        val list = mutableListOf<String>()

        fun getChildIds(jsonTree: JsonTree) {
            when (jsonTree) {
                is JsonTree.PrimitiveElement -> list.add(jsonTree.id)
                is JsonTree.NullElement -> list.add(jsonTree.id)
                is JsonTree.EndBracket -> list.add(jsonTree.id)
                is JsonTree.ArrayElement -> {
                    list.add(jsonTree.id)
                    jsonTree.children.forEach {
                        getChildIds(it.value)
                    }
                    list.add(jsonTree.endBracket.id)
                }
                is JsonTree.ObjectElement -> {
                    list.add(jsonTree.id)
                    jsonTree.children.forEach {
                        getChildIds(it.value)
                    }
                    list.add(jsonTree.endBracket.id)
                }
            }
        }

        getChildIds(this)
        return list
    }
}

private fun JsonElement.toJsonTree(
    state: TreeState,
    level: Int,
    key: String?
): JsonTree {
    return when (this) {
        is JsonPrimitive -> {
            JsonTree.PrimitiveElement(
                id = UUID.randomUUID().toString(),
                level = level,
                key = key,
                value = this
            )
        }
        is JsonNull -> {
            JsonTree.NullElement(
                id = UUID.randomUUID().toString(),
                level = level,
                key = key,
                value = this
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
                        key = it.key
                    )
                }

            JsonTree.ArrayElement(
                id = UUID.randomUUID().toString(),
                level = level,
                state = state,
                key = key,
                children = childElements
            )
        }
        is JsonObject -> {
            val childElements = jsonObject.entries
                .associate { it.toPair() }
                .mapValues {
                    it.value.toJsonTree(
                        state = TreeState.COLLAPSED,
                        level = level + 1,
                        key = it.key
                    )
                }

            JsonTree.ObjectElement(
                id = UUID.randomUUID().toString(),
                level = level,
                state = state,
                key = key,
                children = childElements
            )
        }
    }
}

private sealed interface JsonTree {
    val id: String
    val level: Int

    data class PrimitiveElement(
        override val id: String,
        override val level: Int,
        val key: String?,
        val value: JsonPrimitive,
    ) : JsonTree

    data class NullElement(
        override val id: String,
        override val level: Int,
        val key: String?,
        val value: JsonPrimitive,
    ) : JsonTree

    data class ObjectElement(
        override val id: String,
        override val level: Int,
        val state: TreeState,
        val key: String?,
        val children: Map<String, JsonTree>,
    ) : JsonTree

    data class ArrayElement(
        override val id: String,
        override val level: Int,
        val state: TreeState,
        val key: String?,
        val children: Map<String, JsonTree>,
    ) : JsonTree

    data class EndBracket(
        override val id: String,
        override val level: Int,
        val type: Type
    ) : JsonTree {
        enum class Type { ARRAY, OBJECT }
    }
}

private val JsonTree.ObjectElement.endBracket: JsonTree.EndBracket
    get() = JsonTree.EndBracket(
        id = "$id-b",
        level = level,
        type = JsonTree.EndBracket.Type.OBJECT
    )

private val JsonTree.ArrayElement.endBracket: JsonTree.EndBracket
    get() = JsonTree.EndBracket(
        id = "$id-b",
        level = level,
        type = JsonTree.EndBracket.Type.ARRAY
    )

internal val nestedJson = """
    {
    	"topLevelObject": {
    		"string": "stringValue",
            "nestedObject": {
    	        "int": 42,
                "nestedArray": [
                    "nestedArrayValue",
                    "nestedArrayValue"
                ],
                "arrayOfObjects": [
                    {
                        "anotherString": "anotherStringValue"
                    },
                    {
                        "anotherInt": 52
                    }
                ]
            }
    	},
    	"topLevelArray": [
    		"hello",
    		"world"
    	],
    	"emptyObject": {

        }
    }
""".trimIndent()
