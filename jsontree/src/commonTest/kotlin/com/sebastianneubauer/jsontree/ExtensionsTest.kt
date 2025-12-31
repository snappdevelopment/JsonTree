package com.sebastianneubauer.jsontree

import com.sebastianneubauer.jsontree.JsonTreeElement.Collapsable.Array
import com.sebastianneubauer.jsontree.JsonTreeElement.Collapsable.Object
import com.sebastianneubauer.jsontree.util.Expansion
import com.sebastianneubauer.jsontree.util.collapse
import com.sebastianneubauer.jsontree.util.expand
import com.sebastianneubauer.jsontree.util.toList
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

public class ExtensionsTest {

    @Test
    public fun expand_object_with_expansion_none_should_not_expand_children() {
        val result = ExpandTestData.testObject.expand(Expansion.None)

        assertEquals(
            actual = result,
            expected = ExpandTestData.testObject.copy(state = TreeState.EXPANDED)
        )
    }

    @Test
    public fun expand_object_with_expansion_singleOnly_should_expand_only_single_children() {
        val result = ExpandTestData.testObject.expand(Expansion.SingleOnly)

        assertEquals(
            actual = result,
            expected = ExpandTestData.testObject.copy(
                state = TreeState.EXPANDED,
                children = mapOf("array1" to ExpandTestData.array1.copy(state = TreeState.EXPANDED))
            )
        )
    }

    @Test
    public fun expand_object_with_expansion_all_should_expand_all_children() {
        val result = ExpandTestData.testObject.expand(Expansion.All)

        assertEquals(
            actual = result,
            expected = ExpandTestData.testObject.copy(
                state = TreeState.EXPANDED,
                children = mapOf(
                    "array1" to ExpandTestData.array1.copy(
                        state = TreeState.EXPANDED,
                        children = mapOf(
                            "0" to ExpandTestData.primitive1,
                            "1" to ExpandTestData.object1.copy(
                                state = TreeState.EXPANDED
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    public fun collapse_object_should_collapse_all_children() {
        val result = CollapseTestData.testObject.collapse()

        assertEquals(
            actual = result,
            expected = CollapseTestData.testObject.copy(
                state = TreeState.COLLAPSED,
                children = mapOf(
                    "array1" to CollapseTestData.array1.copy(
                        state = TreeState.COLLAPSED,
                        children = mapOf(
                            "0" to CollapseTestData.primitive1,
                            "1" to CollapseTestData.object1.copy(
                                state = TreeState.COLLAPSED
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    public fun collapsed_object_should_be_the_only_element_in_list() {
        val result = ExpandTestData.testObject.toList()

        assertEquals(
            actual = result,
            expected = listOf(ExpandTestData.testObject)
        )
    }

    @Test
    public fun expanded_object_with_collapsed_children_should_only_have_direct_children_in_list() {
        val result = ExpandTestData.testObject.copy(state = TreeState.EXPANDED).toList()

        assertEquals(
            actual = result,
            expected = listOf(
                ExpandTestData.testObject.copy(state = TreeState.EXPANDED),
                ExpandTestData.array1,
                ExpandTestData.testObject.endBracket
            )
        )
    }

    @Test
    public fun expanded_object_with_expanded_children_should_have_all_children_in_list() {
        val result = CollapseTestData.testObject.toList()

        assertEquals(
            actual = result,
            expected = listOf(
                CollapseTestData.testObject,
                CollapseTestData.array1,
                CollapseTestData.primitive1,
                CollapseTestData.object1,
                CollapseTestData.primitive2,
                CollapseTestData.object1.endBracket,
                CollapseTestData.array1.endBracket,
                CollapseTestData.testObject.endBracket
            )
        )
    }

    private object ExpandTestData {
        val primitive1 = JsonTreeElement.Primitive(
            id = "primitive1",
            level = 2,
            isLastItem = false,
            key = "0",
            value = JsonPrimitive("value1"),
            parentType = JsonTreeElement.ParentType.ARRAY
        )

        val primitive2 = JsonTreeElement.Primitive(
            id = "primitive2",
            level = 3,
            isLastItem = true,
            key = "primitive2",
            value = JsonPrimitive("value2"),
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val object1 = Object(
            id = "object1",
            level = 2,
            state = TreeState.COLLAPSED,
            children = mapOf(
                "primitive2" to primitive2
            ),
            isLastItem = true,
            key = "1",
            parentType = JsonTreeElement.ParentType.ARRAY
        )

        val array1 = Array(
            id = "array1",
            level = 1,
            state = TreeState.COLLAPSED,
            children = mapOf(
                "0" to primitive1,
                "1" to object1
            ),
            isLastItem = true,
            key = "array1",
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val testObject = Object(
            id = "testObject",
            level = 0,
            state = TreeState.COLLAPSED,
            children = mapOf("array1" to array1),
            isLastItem = true,
            key = null,
            parentType = JsonTreeElement.ParentType.NONE
        )
    }

    private object CollapseTestData {
        val primitive1 = JsonTreeElement.Primitive(
            id = "primitive1",
            level = 2,
            isLastItem = false,
            key = "0",
            value = JsonPrimitive("value1"),
            parentType = JsonTreeElement.ParentType.ARRAY
        )

        val primitive2 = JsonTreeElement.Primitive(
            id = "primitive2",
            level = 3,
            isLastItem = true,
            key = "primitive2",
            value = JsonPrimitive("value2"),
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val object1 = Object(
            id = "object1",
            level = 2,
            state = TreeState.EXPANDED,
            children = mapOf(
                "primitive2" to primitive2
            ),
            isLastItem = true,
            key = "1",
            parentType = JsonTreeElement.ParentType.ARRAY
        )

        val array1 = Array(
            id = "array1",
            level = 1,
            state = TreeState.EXPANDED,
            children = mapOf(
                "0" to primitive1,
                "1" to object1
            ),
            isLastItem = true,
            key = "array1",
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val testObject = Object(
            id = "testObject",
            level = 0,
            state = TreeState.EXPANDED,
            children = mapOf("array1" to array1),
            isLastItem = true,
            key = null,
            parentType = JsonTreeElement.ParentType.NONE
        )
    }
}
