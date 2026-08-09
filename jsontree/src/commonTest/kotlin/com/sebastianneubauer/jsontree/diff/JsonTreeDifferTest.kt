package com.sebastianneubauer.jsontree.diff

import com.sebastianneubauer.jsontree.JsonTreeElement
import com.sebastianneubauer.jsontree.JsonTreeElement.Primitive.Type
import com.sebastianneubauer.jsontree.TreeState
import com.sebastianneubauer.jsontree.diff.JsonTreeDifferState.JsonDiffElement
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

public class JsonTreeDifferTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun underTest() = JsonTreeDiffer(
        defaultDispatcher = UnconfinedTestDispatcher(),
        mainDispatcher = UnconfinedTestDispatcher()
    )

    @Test
    public fun initial_state_is_loading(): TestResult = runTest {
        assertEquals(
            expected = JsonTreeDifferState.Loading,
            actual = underTest().state.value
        )
    }

    @Test
    public fun diff_with_identical_json_shows_all_equal_elements(): TestResult = runTest {
        val json = """{"name": "value"}"""
        val differ = underTest()

        differ.diff(
            original = json,
            revised = json,
            showInlineDiffs = false
        )

        val primitive = JsonTreeElement.Primitive(
            id = "1",
            level = 1,
            isLastItem = true,
            key = "name",
            value = "value",
            type = Type.STRING,
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        assertEquals(
            expected = JsonTreeDifferState.Ready(
                diffInfo = JsonTreeDiffInfo(
                    changeInfo = ChangeInfo(
                        insertions = 0,
                        deletions = 0
                    )
                ),
                diffElements = listOf(
                    Pair(
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.Collapsable.Object(
                                id = "2",
                                level = 0,
                                state = TreeState.EXPANDED,
                                children = mapOf("name" to primitive),
                                isLastItem = true,
                                key = null,
                                parentType = JsonTreeElement.ParentType.NONE
                            )
                        ),
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.Collapsable.Object(
                                id = "2",
                                level = 0,
                                state = TreeState.EXPANDED,
                                children = mapOf("name" to primitive),
                                isLastItem = true,
                                key = null,
                                parentType = JsonTreeElement.ParentType.NONE
                            )
                        )
                    ),
                    Pair(
                        JsonDiffElement.Equal(jsonTreeElement = primitive),
                        JsonDiffElement.Equal(jsonTreeElement = primitive)
                    ),
                    Pair(
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "2-b",
                                level = 0,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        ),
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "2-b",
                                level = 0,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        )
                    )
                )
            ),
            actual = differ.state.value
        )
    }

    @Test
    public fun diff_with_changed_value_shows_change_elements(): TestResult = runTest {
        val original = """{"name": "oldValue"}"""
        val revised = """{"name": "newValue"}"""
        val differ = underTest()

        differ.diff(
            original = original,
            revised = revised,
            showInlineDiffs = false
        )

        val originalPrimitive = JsonTreeElement.Primitive(
            id = "1",
            level = 1,
            isLastItem = true,
            key = "name",
            value = "oldValue",
            type = Type.STRING,
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val revisedPrimitive = JsonTreeElement.Primitive(
            id = "1",
            level = 1,
            isLastItem = true,
            key = "name",
            value = "newValue",
            type = Type.STRING,
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        assertEquals(
            expected =  JsonTreeDifferState.Ready(
                diffInfo = JsonTreeDiffInfo(
                    changeInfo = ChangeInfo(
                        insertions = 1,
                        deletions = 1
                    )
                ),
                diffElements = listOf(
                    Pair(
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.Collapsable.Object(
                                id = "2",
                                level = 0,
                                state = TreeState.EXPANDED,
                                children = mapOf("name" to originalPrimitive),
                                isLastItem = true,
                                key = null,
                                parentType = JsonTreeElement.ParentType.NONE
                            )
                        ),
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.Collapsable.Object(
                                id = "2",
                                level = 0,
                                state = TreeState.EXPANDED,
                                children = mapOf("name" to revisedPrimitive),
                                isLastItem = true,
                                key = null,
                                parentType = JsonTreeElement.ParentType.NONE
                            )
                        )
                    ),
                    Pair(
                        JsonDiffElement.Change(
                            jsonTreeElement = originalPrimitive,
                            inlineDiffIndices = emptyList()
                        ),
                        JsonDiffElement.Change(
                            jsonTreeElement = revisedPrimitive,
                            inlineDiffIndices = emptyList()
                        )
                    ),
                    Pair(
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "2-b",
                                level = 0,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        ),
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "2-b",
                                level = 0,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        )
                    )
                )
            ),
            actual = differ.state.value
        )
    }

    @Test
    public fun diff_with_added_field_shows_insertion(): TestResult = runTest {
        val original = """{"name": "value"}"""
        val revised = """{"age": 42, "name": "value"}"""
        val differ = underTest()

        differ.diff(
            original = original,
            revised = revised,
            showInlineDiffs = false
        )

        val originalPrimitive = JsonTreeElement.Primitive(
            id = "1",
            level = 1,
            isLastItem = true,
            key = "name",
            value = "value",
            type = Type.STRING,
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val revisedAgePrimitive = JsonTreeElement.Primitive(
            id = "1",
            level = 1,
            isLastItem = false,
            key = "age",
            value = "42",
            type = Type.NUMBER,
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val revisedNamePrimitive = JsonTreeElement.Primitive(
            id = "2",
            level = 1,
            isLastItem = true,
            key = "name",
            value = "value",
            type = Type.STRING,
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        assertEquals(
            expected = JsonTreeDifferState.Ready(
                diffInfo = JsonTreeDiffInfo(
                    changeInfo = ChangeInfo(
                        insertions = 1,
                        deletions = 0
                    )
                ),
                diffElements = listOf(
                    Pair(
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.Collapsable.Object(
                                id = "2",
                                level = 0,
                                state = TreeState.EXPANDED,
                                children = mapOf("name" to originalPrimitive),
                                isLastItem = true,
                                key = null,
                                parentType = JsonTreeElement.ParentType.NONE
                            )
                        ),
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.Collapsable.Object(
                                id = "3",
                                level = 0,
                                state = TreeState.EXPANDED,
                                children = mapOf(
                                    "age" to revisedAgePrimitive,
                                    "name" to revisedNamePrimitive
                                ),
                                isLastItem = true,
                                key = null,
                                parentType = JsonTreeElement.ParentType.NONE
                            )
                        )
                    ),
                    Pair(
                        JsonDiffElement.Insertion(jsonTreeElement = null),
                        JsonDiffElement.Insertion(jsonTreeElement = revisedAgePrimitive)
                    ),
                    Pair(
                        JsonDiffElement.Equal(jsonTreeElement = originalPrimitive),
                        JsonDiffElement.Equal(jsonTreeElement = revisedNamePrimitive)
                    ),
                    Pair(
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "2-b",
                                level = 0,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        ),
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "3-b",
                                level = 0,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        )
                    )
                )
            ),
            actual = differ.state.value
        )
    }

    @Test
    public fun diff_with_removed_field_shows_deletion(): TestResult = runTest {
        val original = """{"age": 42, "name": "value"}"""
        val revised = """{"name": "value"}"""
        val differ = underTest()

        differ.diff(
            original = original,
            revised = revised,
            showInlineDiffs = false
        )

        val originalAgePrimitive = JsonTreeElement.Primitive(
            id = "1",
            level = 1,
            isLastItem = false,
            key = "age",
            value = "42",
            type = Type.NUMBER,
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val originalNamePrimitive = JsonTreeElement.Primitive(
            id = "2",
            level = 1,
            isLastItem = true,
            key = "name",
            value = "value",
            type = Type.STRING,
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val revisedPrimitive = JsonTreeElement.Primitive(
            id = "1",
            level = 1,
            isLastItem = true,
            key = "name",
            value = "value",
            type = Type.STRING,
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        assertEquals(
            expected = JsonTreeDifferState.Ready(
                diffInfo = JsonTreeDiffInfo(
                    changeInfo = ChangeInfo(
                        insertions = 0,
                        deletions = 1
                    )
                ),
                diffElements = listOf(
                    Pair(
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.Collapsable.Object(
                                id = "3",
                                level = 0,
                                state = TreeState.EXPANDED,
                                children = mapOf(
                                    "age" to originalAgePrimitive,
                                    "name" to originalNamePrimitive
                                ),
                                isLastItem = true,
                                key = null,
                                parentType = JsonTreeElement.ParentType.NONE
                            )
                        ),
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.Collapsable.Object(
                                id = "2",
                                level = 0,
                                state = TreeState.EXPANDED,
                                children = mapOf("name" to revisedPrimitive),
                                isLastItem = true,
                                key = null,
                                parentType = JsonTreeElement.ParentType.NONE
                            )
                        )
                    ),
                    Pair(
                        JsonDiffElement.Deletion(jsonTreeElement = originalAgePrimitive),
                        JsonDiffElement.Deletion(jsonTreeElement = null)
                    ),
                    Pair(
                        JsonDiffElement.Equal(jsonTreeElement = originalNamePrimitive),
                        JsonDiffElement.Equal(jsonTreeElement = revisedPrimitive)
                    ),
                    Pair(
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "3-b",
                                level = 0,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        ),
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "2-b",
                                level = 0,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        )
                    )
                )
            ),
            actual = differ.state.value
        )
    }

    @Test
    public fun diff_with_inline_diffs_enabled_shows_inline_diff_indices(): TestResult = runTest {
        val original = """{"name": "oldValue"}"""
        val revised = """{"name": "newValue"}"""
        val differ = underTest()

        differ.diff(
            original = original,
            revised = revised,
            showInlineDiffs = true
        )

        val originalPrimitive = JsonTreeElement.Primitive(
            id = "1",
            level = 1,
            isLastItem = true,
            key = "name",
            value = "oldValue",
            type = Type.STRING,
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val revisedPrimitive = JsonTreeElement.Primitive(
            id = "1",
            level = 1,
            isLastItem = true,
            key = "name",
            value = "newValue",
            type = Type.STRING,
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        assertEquals(
            expected = JsonTreeDifferState.Ready(
                diffInfo = JsonTreeDiffInfo(
                    changeInfo = ChangeInfo(
                        insertions = 1,
                        deletions = 1
                    )
                ),
                diffElements = listOf(
                    Pair(
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.Collapsable.Object(
                                id = "2",
                                level = 0,
                                state = TreeState.EXPANDED,
                                children = mapOf("name" to originalPrimitive),
                                isLastItem = true,
                                key = null,
                                parentType = JsonTreeElement.ParentType.NONE
                            )
                        ),
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.Collapsable.Object(
                                id = "2",
                                level = 0,
                                state = TreeState.EXPANDED,
                                children = mapOf("name" to revisedPrimitive),
                                isLastItem = true,
                                key = null,
                                parentType = JsonTreeElement.ParentType.NONE
                            )
                        )
                    ),
                    Pair(
                        JsonDiffElement.Change(
                            jsonTreeElement = originalPrimitive,
                            inlineDiffIndices = listOf(Pair(9, 12))
                        ),
                        JsonDiffElement.Change(
                            jsonTreeElement = revisedPrimitive,
                            inlineDiffIndices = listOf(Pair(9, 12))
                        )
                    ),
                    Pair(
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "2-b",
                                level = 0,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        ),
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "2-b",
                                level = 0,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        )
                    )
                )
            ),
            actual = differ.state.value
        )
    }

    @Test
    public fun diff_with_invalid_original_json_shows_original_error(): TestResult = runTest {
        val differ = underTest()

        differ.diff(
            original = "",
            revised = """{"name": "value"}""",
            showInlineDiffs = false
        )

        val state = differ.state.value
        assertTrue(state is JsonTreeDifferState.Error.OriginalJsonError)
    }

    @Test
    public fun diff_with_invalid_revised_json_shows_revised_error(): TestResult = runTest {
        val differ = underTest()

        differ.diff(
            original = """{"name": "value"}""",
            revised = "",
            showInlineDiffs = false
        )

        val state = differ.state.value
        assertTrue(state is JsonTreeDifferState.Error.RevisedJsonError)
    }

    @Test
    public fun diff_with_nested_object_identical_shows_all_elements(): TestResult = runTest {
        val json = """{"user": {"name": "John"}}"""
        val differ = underTest()

        differ.diff(
            original = json,
            revised = json,
            showInlineDiffs = false
        )

        val userObjectPrimitive = JsonTreeElement.Primitive(
            id = "1",
            level = 2,
            isLastItem = true,
            key = "name",
            value = "John",
            type = Type.STRING,
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val userObject = JsonTreeElement.Collapsable.Object(
            id = "2",
            level = 1,
            state = TreeState.EXPANDED,
            children = mapOf("name" to userObjectPrimitive),
            isLastItem = true,
            key = "user",
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val rootObject = JsonTreeElement.Collapsable.Object(
            id = "3",
            level = 0,
            state = TreeState.EXPANDED,
            children = mapOf("user" to userObject),
            isLastItem = true,
            key = null,
            parentType = JsonTreeElement.ParentType.NONE
        )

        assertEquals(
            expected = JsonTreeDifferState.Ready(
                diffInfo = JsonTreeDiffInfo(
                    changeInfo = ChangeInfo(
                        insertions = 0,
                        deletions = 0
                    )
                ),
                diffElements = listOf(
                    Pair(
                        JsonDiffElement.Equal(jsonTreeElement = rootObject),
                        JsonDiffElement.Equal(jsonTreeElement = rootObject)
                    ),
                    Pair(
                        JsonDiffElement.Equal(jsonTreeElement = userObject),
                        JsonDiffElement.Equal(jsonTreeElement = userObject)
                    ),
                    Pair(
                        JsonDiffElement.Equal(jsonTreeElement = userObjectPrimitive),
                        JsonDiffElement.Equal(jsonTreeElement = userObjectPrimitive)
                    ),
                    Pair(
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "2-b",
                                level = 1,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        ),
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "2-b",
                                level = 1,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        )
                    ),
                    Pair(
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "3-b",
                                level = 0,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        ),
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "3-b",
                                level = 0,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        )
                    )
                )
            ),
            actual = differ.state.value
        )
    }

    @Test
    public fun diff_with_nested_object_value_change_shows_change(): TestResult = runTest {
        val original = """{"user": {"name": "John"}}"""
        val revised = """{"user": {"name": "Jane"}}"""
        val differ = underTest()

        differ.diff(
            original = original,
            revised = revised,
            showInlineDiffs = false
        )

        val originalUserPrimitive = JsonTreeElement.Primitive(
            id = "1",
            level = 2,
            isLastItem = true,
            key = "name",
            value = "John",
            type = Type.STRING,
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val revisedUserPrimitive = JsonTreeElement.Primitive(
            id = "1",
            level = 2,
            isLastItem = true,
            key = "name",
            value = "Jane",
            type = Type.STRING,
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val originalUserObject = JsonTreeElement.Collapsable.Object(
            id = "2",
            level = 1,
            state = TreeState.EXPANDED,
            children = mapOf("name" to originalUserPrimitive),
            isLastItem = true,
            key = "user",
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val revisedUserObject = JsonTreeElement.Collapsable.Object(
            id = "2",
            level = 1,
            state = TreeState.EXPANDED,
            children = mapOf("name" to revisedUserPrimitive),
            isLastItem = true,
            key = "user",
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val originalRootObject = JsonTreeElement.Collapsable.Object(
            id = "3",
            level = 0,
            state = TreeState.EXPANDED,
            children = mapOf("user" to originalUserObject),
            isLastItem = true,
            key = null,
            parentType = JsonTreeElement.ParentType.NONE
        )

        val revisedRootObject = JsonTreeElement.Collapsable.Object(
            id = "3",
            level = 0,
            state = TreeState.EXPANDED,
            children = mapOf("user" to revisedUserObject),
            isLastItem = true,
            key = null,
            parentType = JsonTreeElement.ParentType.NONE
        )

        assertEquals(
            expected = JsonTreeDifferState.Ready(
                diffInfo = JsonTreeDiffInfo(
                    changeInfo = ChangeInfo(
                        insertions = 1,
                        deletions = 1
                    )
                ),
                diffElements = listOf(
                    Pair(
                        JsonDiffElement.Equal(jsonTreeElement = originalRootObject),
                        JsonDiffElement.Equal(jsonTreeElement = revisedRootObject)
                    ),
                    Pair(
                        JsonDiffElement.Equal(jsonTreeElement = originalUserObject),
                        JsonDiffElement.Equal(jsonTreeElement = revisedUserObject)
                    ),
                    Pair(
                        JsonDiffElement.Change(
                            jsonTreeElement = originalUserPrimitive,
                            inlineDiffIndices = emptyList()
                        ),
                        JsonDiffElement.Change(
                            jsonTreeElement = revisedUserPrimitive,
                            inlineDiffIndices = emptyList()
                        )
                    ),
                    Pair(
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "2-b",
                                level = 1,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        ),
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "2-b",
                                level = 1,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        )
                    ),
                    Pair(
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "3-b",
                                level = 0,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        ),
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "3-b",
                                level = 0,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        )
                    )
                )
            ),
            actual = differ.state.value
        )
    }

    @Test
    public fun diff_with_deeply_nested_objects_preserves_structure(): TestResult = runTest {
        val json = """{"a": {"b": {"c": "value"}}}"""
        val differ = underTest()

        differ.diff(
            original = json,
            revised = json,
            showInlineDiffs = false
        )

        val cPrimitive = JsonTreeElement.Primitive(
            id = "1",
            level = 3,
            isLastItem = true,
            key = "c",
            value = "value",
            type = Type.STRING,
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val bObject = JsonTreeElement.Collapsable.Object(
            id = "2",
            level = 2,
            state = TreeState.EXPANDED,
            children = mapOf("c" to cPrimitive),
            isLastItem = true,
            key = "b",
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val aObject = JsonTreeElement.Collapsable.Object(
            id = "3",
            level = 1,
            state = TreeState.EXPANDED,
            children = mapOf("b" to bObject),
            isLastItem = true,
            key = "a",
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val rootObject = JsonTreeElement.Collapsable.Object(
            id = "4",
            level = 0,
            state = TreeState.EXPANDED,
            children = mapOf("a" to aObject),
            isLastItem = true,
            key = null,
            parentType = JsonTreeElement.ParentType.NONE
        )

        assertEquals(
            expected = JsonTreeDifferState.Ready(
                diffInfo = JsonTreeDiffInfo(
                    changeInfo = ChangeInfo(
                        insertions = 0,
                        deletions = 0
                    )
                ),
                diffElements = listOf(
                    Pair(
                        JsonDiffElement.Equal(jsonTreeElement = rootObject),
                        JsonDiffElement.Equal(jsonTreeElement = rootObject)
                    ),
                    Pair(
                        JsonDiffElement.Equal(jsonTreeElement = aObject),
                        JsonDiffElement.Equal(jsonTreeElement = aObject)
                    ),
                    Pair(
                        JsonDiffElement.Equal(jsonTreeElement = bObject),
                        JsonDiffElement.Equal(jsonTreeElement = bObject)
                    ),
                    Pair(
                        JsonDiffElement.Equal(jsonTreeElement = cPrimitive),
                        JsonDiffElement.Equal(jsonTreeElement = cPrimitive)
                    ),
                    Pair(
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "2-b",
                                level = 2,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        ),
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "2-b",
                                level = 2,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        )
                    ),
                    Pair(
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "3-b",
                                level = 1,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        ),
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "3-b",
                                level = 1,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        )
                    ),
                    Pair(
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "4-b",
                                level = 0,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        ),
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "4-b",
                                level = 0,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        )
                    )
                )
            ),
            actual = differ.state.value
        )
    }

    @Test
    public fun diff_with_nested_object_insertion_shows_insertion(): TestResult = runTest {
        val original = """{"user": {"name": "John"}}"""
        val revised = """{"user": {"age": 30, "name": "John"}}"""
        val differ = underTest()

        differ.diff(
            original = original,
            revised = revised,
            showInlineDiffs = false
        )

        val originalNamePrimitive = JsonTreeElement.Primitive(
            id = "1",
            level = 2,
            isLastItem = true,
            key = "name",
            value = "John",
            type = Type.STRING,
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val revisedAgePrimitive = JsonTreeElement.Primitive(
            id = "1",
            level = 2,
            isLastItem = false,
            key = "age",
            value = "30",
            type = Type.NUMBER,
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val revisedNamePrimitive = JsonTreeElement.Primitive(
            id = "2",
            level = 2,
            isLastItem = true,
            key = "name",
            value = "John",
            type = Type.STRING,
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val originalUserObject = JsonTreeElement.Collapsable.Object(
            id = "2",
            level = 1,
            state = TreeState.EXPANDED,
            children = mapOf("name" to originalNamePrimitive),
            isLastItem = true,
            key = "user",
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val revisedUserObject = JsonTreeElement.Collapsable.Object(
            id = "3",
            level = 1,
            state = TreeState.EXPANDED,
            children = mapOf(
                "age" to revisedAgePrimitive,
                "name" to revisedNamePrimitive
            ),
            isLastItem = true,
            key = "user",
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val originalRootObject = JsonTreeElement.Collapsable.Object(
            id = "3",
            level = 0,
            state = TreeState.EXPANDED,
            children = mapOf("user" to originalUserObject),
            isLastItem = true,
            key = null,
            parentType = JsonTreeElement.ParentType.NONE
        )

        val revisedRootObject = JsonTreeElement.Collapsable.Object(
            id = "4",
            level = 0,
            state = TreeState.EXPANDED,
            children = mapOf("user" to revisedUserObject),
            isLastItem = true,
            key = null,
            parentType = JsonTreeElement.ParentType.NONE
        )

        assertEquals(
            expected = JsonTreeDifferState.Ready(
                diffInfo = JsonTreeDiffInfo(
                    changeInfo = ChangeInfo(
                        insertions = 1,
                        deletions = 0
                    )
                ),
                diffElements = listOf(
                    Pair(
                        JsonDiffElement.Equal(jsonTreeElement = originalRootObject),
                        JsonDiffElement.Equal(jsonTreeElement = revisedRootObject)
                    ),
                    Pair(
                        JsonDiffElement.Equal(jsonTreeElement = originalUserObject),
                        JsonDiffElement.Equal(jsonTreeElement = revisedUserObject)
                    ),
                    Pair(
                        JsonDiffElement.Insertion(jsonTreeElement = null),
                        JsonDiffElement.Insertion(jsonTreeElement = revisedAgePrimitive)
                    ),
                    Pair(
                        JsonDiffElement.Equal(jsonTreeElement = originalNamePrimitive),
                        JsonDiffElement.Equal(jsonTreeElement = revisedNamePrimitive)
                    ),
                    Pair(
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "2-b",
                                level = 1,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        ),
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "3-b",
                                level = 1,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        )
                    ),
                    Pair(
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "3-b",
                                level = 0,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        ),
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "4-b",
                                level = 0,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        )
                    )
                )
            ),
            actual = differ.state.value
        )
    }

    @Test
    public fun diff_with_nested_object_deletion_shows_deletion(): TestResult = runTest {
        val original = """{"user": {"age": 30, "name": "John"}}"""
        val revised = """{"user": {"name": "John"}}"""
        val differ = underTest()

        differ.diff(
            original = original,
            revised = revised,
            showInlineDiffs = false
        )

        val originalAgePrimitive = JsonTreeElement.Primitive(
            id = "1",
            level = 2,
            isLastItem = false,
            key = "age",
            value = "30",
            type = Type.NUMBER,
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val originalNamePrimitive = JsonTreeElement.Primitive(
            id = "2",
            level = 2,
            isLastItem = true,
            key = "name",
            value = "John",
            type = Type.STRING,
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val revisedNamePrimitive = JsonTreeElement.Primitive(
            id = "1",
            level = 2,
            isLastItem = true,
            key = "name",
            value = "John",
            type = Type.STRING,
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val originalUserObject = JsonTreeElement.Collapsable.Object(
            id = "3",
            level = 1,
            state = TreeState.EXPANDED,
            children = mapOf(
                "age" to originalAgePrimitive,
                "name" to originalNamePrimitive
            ),
            isLastItem = true,
            key = "user",
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val revisedUserObject = JsonTreeElement.Collapsable.Object(
            id = "2",
            level = 1,
            state = TreeState.EXPANDED,
            children = mapOf("name" to revisedNamePrimitive),
            isLastItem = true,
            key = "user",
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val originalRootObject = JsonTreeElement.Collapsable.Object(
            id = "4",
            level = 0,
            state = TreeState.EXPANDED,
            children = mapOf("user" to originalUserObject),
            isLastItem = true,
            key = null,
            parentType = JsonTreeElement.ParentType.NONE
        )

        val revisedRootObject = JsonTreeElement.Collapsable.Object(
            id = "3",
            level = 0,
            state = TreeState.EXPANDED,
            children = mapOf("user" to revisedUserObject),
            isLastItem = true,
            key = null,
            parentType = JsonTreeElement.ParentType.NONE
        )

        assertEquals(
            expected = JsonTreeDifferState.Ready(
                diffInfo = JsonTreeDiffInfo(
                    changeInfo = ChangeInfo(
                        insertions = 0,
                        deletions = 1
                    )
                ),
                diffElements = listOf(
                    Pair(
                        JsonDiffElement.Equal(jsonTreeElement = originalRootObject),
                        JsonDiffElement.Equal(jsonTreeElement = revisedRootObject)
                    ),
                    Pair(
                        JsonDiffElement.Equal(jsonTreeElement = originalUserObject),
                        JsonDiffElement.Equal(jsonTreeElement = revisedUserObject)
                    ),
                    Pair(
                        JsonDiffElement.Deletion(jsonTreeElement = originalAgePrimitive),
                        JsonDiffElement.Deletion(jsonTreeElement = null)
                    ),
                    Pair(
                        JsonDiffElement.Equal(jsonTreeElement = originalNamePrimitive),
                        JsonDiffElement.Equal(jsonTreeElement = revisedNamePrimitive)
                    ),
                    Pair(
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "3-b",
                                level = 1,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        ),
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "2-b",
                                level = 1,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        )
                    ),
                    Pair(
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "4-b",
                                level = 0,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        ),
                        JsonDiffElement.Equal(
                            jsonTreeElement = JsonTreeElement.EndBracket(
                                id = "3-b",
                                level = 0,
                                isLastItem = true,
                                type = JsonTreeElement.EndBracket.Type.OBJECT
                            )
                        )
                    )
                )
            ),
            actual = differ.state.value
        )
    }
}