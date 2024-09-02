package com.sebastianneubauer.jsontree

import com.sebastianneubauer.jsontree.JsonTreeParserState.Parsing.Error
import com.sebastianneubauer.jsontree.JsonTreeParserState.Ready
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

public class JsonTreeParserTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun underTest(json: String, initialState: TreeState): JsonTreeParser {
        return JsonTreeParser(
            json = json,
            defaultDispatcher = UnconfinedTestDispatcher(),
            mainDispatcher = UnconfinedTestDispatcher()
        ).also { it.init(initialState) }
    }

    @Test
    public fun `invalid json - shows error state`(): TestResult = runTest {
        val underTest = underTest(INVALID_JSON, TreeState.COLLAPSED)
        // produces Error state with any throwable
        assertTrue(underTest.state.value is Error)
    }

    @Test
    public fun `empty json - collapsed - shows collapsed empty json`(): TestResult = runTest {
        val underTest = underTest(EMPTY_OBJECT_JSON, TreeState.COLLAPSED)
        assertEquals(
            Ready(
                list = listOf(
                    JsonTreeElement.Collapsable.Object(
                        id = "1",
                        level = 0,
                        state = TreeState.COLLAPSED,
                        children = emptyMap(),
                        isLastItem = true,
                        key = null,
                        parentType = JsonTreeElement.ParentType.NONE,
                    )
                )
            ),
            underTest.state.value
        )
    }

    @Test
    public fun `empty json - first item expanded - shows expanded empty json`(): TestResult = runTest {
        val underTest = underTest(EMPTY_OBJECT_JSON, TreeState.FIRST_ITEM_EXPANDED)
        assertEquals(
            Ready(
                list = listOf(
                    JsonTreeElement.Collapsable.Object(
                        id = "1",
                        level = 0,
                        state = TreeState.FIRST_ITEM_EXPANDED,
                        children = emptyMap(),
                        isLastItem = true,
                        key = null,
                        parentType = JsonTreeElement.ParentType.NONE,
                    ),
                    JsonTreeElement.EndBracket(
                        id = "1-b",
                        level = 0,
                        isLastItem = true,
                        type = JsonTreeElement.EndBracket.Type.OBJECT
                    )
                )
            ),
            underTest.state.value
        )
    }

    @Test
    public fun `empty json - expanded - shows expanded empty json`(): TestResult = runTest {
        val underTest = underTest(EMPTY_OBJECT_JSON, TreeState.EXPANDED)
        assertEquals(
            Ready(
                list = listOf(
                    JsonTreeElement.Collapsable.Object(
                        id = "1",
                        level = 0,
                        state = TreeState.EXPANDED,
                        children = emptyMap(),
                        isLastItem = true,
                        key = null,
                        parentType = JsonTreeElement.ParentType.NONE,
                    ),
                    JsonTreeElement.EndBracket(
                        id = "1-b",
                        level = 0,
                        isLastItem = true,
                        type = JsonTreeElement.EndBracket.Type.OBJECT
                    )
                )
            ),
            underTest.state.value
        )
    }

    @Test
    public fun `root string json - collapsed - shows root string`(): TestResult = runTest {
        val underTest = underTest(rootStringJson, TreeState.COLLAPSED)
        assertEquals(
            Ready(
                list = listOf(
                    JsonTreeElement.Primitive(
                        id = "1",
                        level = 0,
                        isLastItem = true,
                        key = null,
                        value = JsonPrimitive("stringValue"),
                        parentType = JsonTreeElement.ParentType.NONE
                    )
                )
            ),
            underTest.state.value
        )
    }

    @Test
    public fun `root string json - expanded - shows root string`(): TestResult = runTest {
        val underTest = underTest(rootStringJson, TreeState.EXPANDED)
        assertEquals(
            Ready(
                list = listOf(
                    JsonTreeElement.Primitive(
                        id = "1",
                        level = 0,
                        isLastItem = true,
                        key = null,
                        value = JsonPrimitive("stringValue"),
                        parentType = JsonTreeElement.ParentType.NONE
                    )
                )
            ),
            underTest.state.value
        )
    }

    @Test
    public fun `root string json - first item expanded - shows root string`(): TestResult = runTest {
        val underTest = underTest(rootStringJson, TreeState.FIRST_ITEM_EXPANDED)
        assertEquals(
            Ready(
                list = listOf(
                    JsonTreeElement.Primitive(
                        id = "1",
                        level = 0,
                        isLastItem = true,
                        key = null,
                        value = JsonPrimitive("stringValue"),
                        parentType = JsonTreeElement.ParentType.NONE
                    )
                )
            ),
            underTest.state.value
        )
    }

    @Test
    public fun `root array json - collapsed - shows collapsed array`(): TestResult = runTest {
        val underTest = underTest(rootArrayJson, TreeState.COLLAPSED)
        assertEquals(
            Ready(
                list = listOf(
                    JsonTreeElement.Collapsable.Array(
                        id = "2",
                        level = 0,
                        isLastItem = true,
                        key = null,
                        state = TreeState.COLLAPSED,
                        parentType = JsonTreeElement.ParentType.NONE,
                        children = mapOf(
                            "0" to JsonTreeElement.Primitive(
                                id = "1",
                                level = 1,
                                isLastItem = true,
                                key = "0",
                                value = JsonPrimitive("stringValue"),
                                parentType = JsonTreeElement.ParentType.ARRAY,
                            )
                        )
                    )
                )
            ),
            underTest.state.value
        )
    }

    @Test
    public fun `root array json - first item expanded - shows expanded array`(): TestResult = runTest {
        val underTest = underTest(rootArrayJson, TreeState.FIRST_ITEM_EXPANDED)
        assertEquals(
            Ready(
                list = listOf(
                    JsonTreeElement.Collapsable.Array(
                        id = "2",
                        level = 0,
                        isLastItem = true,
                        key = null,
                        state = TreeState.FIRST_ITEM_EXPANDED,
                        parentType = JsonTreeElement.ParentType.NONE,
                        children = mapOf(
                            "0" to JsonTreeElement.Primitive(
                                id = "1",
                                level = 1,
                                isLastItem = true,
                                key = "0",
                                value = JsonPrimitive("stringValue"),
                                parentType = JsonTreeElement.ParentType.ARRAY,
                            )
                        )
                    ),
                    JsonTreeElement.Primitive(
                        id = "1",
                        level = 1,
                        isLastItem = true,
                        key = "0",
                        value = JsonPrimitive("stringValue"),
                        parentType = JsonTreeElement.ParentType.ARRAY,
                    ),
                    JsonTreeElement.EndBracket(
                        id = "2-b",
                        level = 0,
                        isLastItem = true,
                        type = JsonTreeElement.EndBracket.Type.ARRAY,
                    )
                )
            ),
            underTest.state.value
        )
    }

    @Test
    public fun `root array json - expanded - shows expanded array`(): TestResult = runTest {
        val underTest = underTest(rootArrayJson, TreeState.EXPANDED)
        assertEquals(
            Ready(
                list = listOf(
                    JsonTreeElement.Collapsable.Array(
                        id = "2",
                        level = 0,
                        isLastItem = true,
                        key = null,
                        state = TreeState.EXPANDED,
                        parentType = JsonTreeElement.ParentType.NONE,
                        children = mapOf(
                            "0" to JsonTreeElement.Primitive(
                                id = "1",
                                level = 1,
                                isLastItem = true,
                                key = "0",
                                value = JsonPrimitive("stringValue"),
                                parentType = JsonTreeElement.ParentType.ARRAY,
                            )
                        )
                    ),
                    JsonTreeElement.Primitive(
                        id = "1",
                        level = 1,
                        isLastItem = true,
                        key = "0",
                        value = JsonPrimitive("stringValue"),
                        parentType = JsonTreeElement.ParentType.ARRAY,
                    ),
                    JsonTreeElement.EndBracket(
                        id = "2-b",
                        level = 0,
                        isLastItem = true,
                        type = JsonTreeElement.EndBracket.Type.ARRAY,
                    )
                )
            ),
            underTest.state.value
        )
    }

    @Test
    public fun `nested json - collapsed - shows collapsed json`(): TestResult = runTest {
        val underTest = underTest(nestedJson, TreeState.COLLAPSED)
        assertEquals(
            Ready(list = listOf(jsonTreeElement())),
            underTest.state.value
        )
    }

    @Test
    public fun `nested json - first item expanded - shows first item expanded`(): TestResult = runTest {
        val underTest = underTest(nestedJson, TreeState.FIRST_ITEM_EXPANDED)
        assertEquals(
            Ready(
                list = listOf(
                    jsonTreeElement(
                        state = TreeState.FIRST_ITEM_EXPANDED,
                        childrenState = TreeState.COLLAPSED
                    ),
                    array(),
                    JsonTreeElement.EndBracket(
                        id = "8-b",
                        level = 0,
                        isLastItem = true,
                        type = JsonTreeElement.EndBracket.Type.OBJECT
                    )
                )
            ),
            underTest.state.value
        )
    }

    @Test
    public fun `nested json - expanded - shows expanded json`(): TestResult = runTest {
        val underTest = underTest(nestedJson, TreeState.EXPANDED)
        assertEquals(
            Ready(
                list = listOf(
                    jsonTreeElement(state = TreeState.EXPANDED, childrenState = TreeState.EXPANDED),
                    array(state = TreeState.EXPANDED, childrenState = TreeState.EXPANDED),
                    nestedArray1(state = TreeState.EXPANDED, childrenState = TreeState.EXPANDED),
                    objectElement(state = TreeState.EXPANDED),
                    stringPrimitive,
                    JsonTreeElement.EndBracket(
                        id = "2-b",
                        level = 3,
                        isLastItem = true,
                        type = JsonTreeElement.EndBracket.Type.OBJECT
                    ),
                    JsonTreeElement.EndBracket(
                        id = "3-b",
                        level = 2,
                        isLastItem = false,
                        type = JsonTreeElement.EndBracket.Type.ARRAY
                    ),
                    nestedArray2(state = TreeState.EXPANDED),
                    numberPrimitive1,
                    numberPrimitive2,
                    JsonTreeElement.EndBracket(
                        id = "6-b",
                        level = 2,
                        isLastItem = true,
                        type = JsonTreeElement.EndBracket.Type.ARRAY
                    ),
                    JsonTreeElement.EndBracket(
                        id = "7-b",
                        level = 1,
                        isLastItem = true,
                        type = JsonTreeElement.EndBracket.Type.ARRAY
                    ),
                    JsonTreeElement.EndBracket(
                        id = "8-b",
                        level = 0,
                        isLastItem = true,
                        type = JsonTreeElement.EndBracket.Type.OBJECT
                    ),
                )
            ),
            underTest.state.value
        )
    }

    @Test
    public fun `nested json - expandSingleChildren is false - expands and collapses correctly`(): TestResult =
        runTest {
            val underTest = underTest(nestedJson, TreeState.COLLAPSED)
            assertEquals(
                Ready(list = listOf(jsonTreeElement())),
                underTest.state.value
            )

            // expand root
            underTest.expandOrCollapseItem(jsonTreeElement(), expandSingleChildren = false)

            assertEquals(
                Ready(
                    list = listOf(
                        jsonTreeElement(
                            state = TreeState.EXPANDED,
                            childrenState = TreeState.COLLAPSED
                        ),
                        array(),
                        JsonTreeElement.EndBracket(
                            id = "8-b",
                            level = 0,
                            isLastItem = true,
                            type = JsonTreeElement.EndBracket.Type.OBJECT
                        ),
                    )
                ),
                underTest.state.value
            )

            // expand array
            underTest.expandOrCollapseItem(item = array(), expandSingleChildren = false)

            assertEquals(
                Ready(
                    list = listOf(
                        jsonTreeElement(
                            state = TreeState.EXPANDED,
                            childrenState = TreeState.COLLAPSED
                        ),
                        array(state = TreeState.EXPANDED, childrenState = TreeState.COLLAPSED),
                        nestedArray1(),
                        nestedArray2(),
                        JsonTreeElement.EndBracket(
                            id = "7-b",
                            level = 1,
                            isLastItem = true,
                            type = JsonTreeElement.EndBracket.Type.ARRAY
                        ),
                        JsonTreeElement.EndBracket(
                            id = "8-b",
                            level = 0,
                            isLastItem = true,
                            type = JsonTreeElement.EndBracket.Type.OBJECT
                        ),
                    )
                ),
                underTest.state.value
            )

            // expand nestedArray2
            underTest.expandOrCollapseItem(item = nestedArray2(), expandSingleChildren = false)

            assertEquals(
                Ready(
                    list = listOf(
                        jsonTreeElement(
                            state = TreeState.EXPANDED,
                            childrenState = TreeState.COLLAPSED
                        ),
                        array(state = TreeState.EXPANDED, childrenState = TreeState.COLLAPSED),
                        nestedArray1(),
                        nestedArray2(state = TreeState.EXPANDED),
                        numberPrimitive1,
                        numberPrimitive2,
                        JsonTreeElement.EndBracket(
                            id = "6-b",
                            level = 2,
                            isLastItem = true,
                            type = JsonTreeElement.EndBracket.Type.ARRAY
                        ),
                        JsonTreeElement.EndBracket(
                            id = "7-b",
                            level = 1,
                            isLastItem = true,
                            type = JsonTreeElement.EndBracket.Type.ARRAY
                        ),
                        JsonTreeElement.EndBracket(
                            id = "8-b",
                            level = 0,
                            isLastItem = true,
                            type = JsonTreeElement.EndBracket.Type.OBJECT
                        ),
                    )
                ),
                underTest.state.value
            )

            // collapse array
            underTest.expandOrCollapseItem(
                item = array(state = TreeState.EXPANDED, childrenState = TreeState.COLLAPSED),
                expandSingleChildren = false
            )

            assertEquals(
                Ready(
                    list = listOf(
                        jsonTreeElement(
                            state = TreeState.EXPANDED,
                            childrenState = TreeState.COLLAPSED
                        ),
                        array(),
                        JsonTreeElement.EndBracket(
                            id = "8-b",
                            level = 0,
                            isLastItem = true,
                            type = JsonTreeElement.EndBracket.Type.OBJECT
                        ),
                    )
                ),
                underTest.state.value
            )

            // collapse root
            underTest.expandOrCollapseItem(
                item = jsonTreeElement(
                    state = TreeState.EXPANDED,
                    childrenState = TreeState.COLLAPSED
                ),
                expandSingleChildren = false
            )

            assertEquals(
                Ready(list = listOf(jsonTreeElement())),
                underTest.state.value
            )
        }

    @Test
    public fun `nested json - expandSingleChildren is true - expands and collapses correctly`(): TestResult = runTest {
        val underTest = underTest(nestedJson, TreeState.COLLAPSED)
        assertEquals(
            Ready(list = listOf(jsonTreeElement())),
            underTest.state.value
        )

        // expand root
        underTest.expandOrCollapseItem(jsonTreeElement(), expandSingleChildren = true)

        assertEquals(
            Ready(
                list = listOf(
                    jsonTreeElement(
                        state = TreeState.EXPANDED,
                        childrenState = TreeState.EXPANDED,
                        grandChildrenState = TreeState.COLLAPSED
                    ),
                    array(state = TreeState.EXPANDED),
                    nestedArray1(),
                    nestedArray2(),
                    JsonTreeElement.EndBracket(
                        id = "7-b",
                        level = 1,
                        isLastItem = true,
                        type = JsonTreeElement.EndBracket.Type.ARRAY
                    ),
                    JsonTreeElement.EndBracket(
                        id = "8-b",
                        level = 0,
                        isLastItem = true,
                        type = JsonTreeElement.EndBracket.Type.OBJECT
                    ),
                )
            ),
            underTest.state.value
        )

        // expand nestedArray1, expands nested object as well
        underTest.expandOrCollapseItem(item = nestedArray1(), expandSingleChildren = true)

        assertEquals(
            Ready(
                list = listOf(
                    jsonTreeElement(
                        state = TreeState.EXPANDED,
                        childrenState = TreeState.EXPANDED,
                        grandChildrenState = TreeState.COLLAPSED
                    ),
                    array(state = TreeState.EXPANDED),
                    nestedArray1(state = TreeState.EXPANDED, childrenState = TreeState.EXPANDED),
                    objectElement(state = TreeState.EXPANDED),
                    stringPrimitive,
                    JsonTreeElement.EndBracket(
                        id = "2-b",
                        level = 3,
                        isLastItem = true,
                        type = JsonTreeElement.EndBracket.Type.OBJECT
                    ),
                    JsonTreeElement.EndBracket(
                        id = "3-b",
                        level = 2,
                        isLastItem = false,
                        type = JsonTreeElement.EndBracket.Type.ARRAY
                    ),
                    nestedArray2(),
                    JsonTreeElement.EndBracket(
                        id = "7-b",
                        level = 1,
                        isLastItem = true,
                        type = JsonTreeElement.EndBracket.Type.ARRAY
                    ),
                    JsonTreeElement.EndBracket(
                        id = "8-b",
                        level = 0,
                        isLastItem = true,
                        type = JsonTreeElement.EndBracket.Type.OBJECT
                    ),
                )
            ),
            underTest.state.value
        )

        // expand nestedArray2
        underTest.expandOrCollapseItem(item = nestedArray2(), expandSingleChildren = true)

        assertEquals(
            Ready(
                list = listOf(
                    jsonTreeElement(
                        state = TreeState.EXPANDED,
                        childrenState = TreeState.EXPANDED,
                        grandChildrenState = TreeState.COLLAPSED
                    ),
                    array(state = TreeState.EXPANDED),
                    nestedArray1(state = TreeState.EXPANDED, childrenState = TreeState.EXPANDED),
                    objectElement(state = TreeState.EXPANDED),
                    stringPrimitive,
                    JsonTreeElement.EndBracket(
                        id = "2-b",
                        level = 3,
                        isLastItem = true,
                        type = JsonTreeElement.EndBracket.Type.OBJECT
                    ),
                    JsonTreeElement.EndBracket(
                        id = "3-b",
                        level = 2,
                        isLastItem = false,
                        type = JsonTreeElement.EndBracket.Type.ARRAY
                    ),
                    nestedArray2(state = TreeState.EXPANDED),
                    numberPrimitive1,
                    numberPrimitive2,
                    JsonTreeElement.EndBracket(
                        id = "6-b",
                        level = 2,
                        isLastItem = true,
                        type = JsonTreeElement.EndBracket.Type.ARRAY
                    ),
                    JsonTreeElement.EndBracket(
                        id = "7-b",
                        level = 1,
                        isLastItem = true,
                        type = JsonTreeElement.EndBracket.Type.ARRAY
                    ),
                    JsonTreeElement.EndBracket(
                        id = "8-b",
                        level = 0,
                        isLastItem = true,
                        type = JsonTreeElement.EndBracket.Type.OBJECT
                    ),
                )
            ),
            underTest.state.value
        )

        // collapse array
        underTest.expandOrCollapseItem(
            item = array(state = TreeState.EXPANDED),
            expandSingleChildren = true
        )

        assertEquals(
            Ready(
                list = listOf(
                    jsonTreeElement(
                        state = TreeState.EXPANDED,
                        childrenState = TreeState.EXPANDED,
                        grandChildrenState = TreeState.COLLAPSED
                    ),
                    array(),
                    JsonTreeElement.EndBracket(
                        id = "8-b",
                        level = 0,
                        isLastItem = true,
                        type = JsonTreeElement.EndBracket.Type.OBJECT
                    ),
                )
            ),
            underTest.state.value
        )

        // collapse root
        underTest.expandOrCollapseItem(
            item = jsonTreeElement(
                state = TreeState.EXPANDED,
                childrenState = TreeState.COLLAPSED,
                grandChildrenState = TreeState.COLLAPSED
            ),
            expandSingleChildren = true
        )

        assertEquals(
            Ready(list = listOf(jsonTreeElement())),
            underTest.state.value
        )
    }

    private val stringPrimitive = JsonTreeElement.Primitive(
        id = "1",
        level = 4,
        isLastItem = true,
        key = "string",
        value = JsonPrimitive("aString"),
        parentType = JsonTreeElement.ParentType.OBJECT,
    )

    private val numberPrimitive1 = JsonTreeElement.Primitive(
        id = "4",
        level = 3,
        isLastItem = false,
        key = "0",
        value = JsonPrimitive(42),
        parentType = JsonTreeElement.ParentType.ARRAY
    )

    private val numberPrimitive2 = JsonTreeElement.Primitive(
        id = "5",
        level = 3,
        isLastItem = true,
        key = "1",
        value = JsonPrimitive(52),
        parentType = JsonTreeElement.ParentType.ARRAY
    )

    private fun objectElement(
        state: TreeState = TreeState.COLLAPSED,
    ) = JsonTreeElement.Collapsable.Object(
        id = "2",
        level = 3,
        state = state,
        isLastItem = true,
        key = "0",
        parentType = JsonTreeElement.ParentType.ARRAY,
        children = mapOf(
            "string" to stringPrimitive
        ),
    )

    private fun nestedArray1(
        state: TreeState = TreeState.COLLAPSED,
        childrenState: TreeState = TreeState.COLLAPSED,
    ) = JsonTreeElement.Collapsable.Array(
        id = "3",
        level = 2,
        isLastItem = false,
        key = "0",
        state = state,
        parentType = JsonTreeElement.ParentType.ARRAY,
        children = mapOf(
            "0" to objectElement(state = childrenState)
        ),
    )

    private fun nestedArray2(
        state: TreeState = TreeState.COLLAPSED,
    ) = JsonTreeElement.Collapsable.Array(
        id = "6",
        level = 2,
        state = state,
        isLastItem = true,
        key = "1",
        parentType = JsonTreeElement.ParentType.ARRAY,
        children = mapOf(
            "0" to numberPrimitive1,
            "1" to numberPrimitive2
        ),
    )

    private fun array(
        state: TreeState = TreeState.COLLAPSED,
        childrenState: TreeState = TreeState.COLLAPSED,
    ) = JsonTreeElement.Collapsable.Array(
        id = "7",
        level = 1,
        isLastItem = true,
        key = "array",
        state = state,
        parentType = JsonTreeElement.ParentType.OBJECT,
        children = mapOf(
            "0" to nestedArray1(state = childrenState, childrenState = childrenState),
            "1" to nestedArray2(state = childrenState)
        ),
    )

    private fun jsonTreeElement(
        state: TreeState = TreeState.COLLAPSED,
        childrenState: TreeState = TreeState.COLLAPSED,
        grandChildrenState: TreeState = childrenState,
    ) = JsonTreeElement.Collapsable.Object(
        id = "8",
        level = 0,
        state = state,
        isLastItem = true,
        key = null,
        parentType = JsonTreeElement.ParentType.NONE,
        children = mapOf(
            "array" to array(state = childrenState, childrenState = grandChildrenState)
        )
    )
}
