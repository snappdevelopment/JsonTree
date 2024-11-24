package com.sebastianneubauer.jsontree

import kotlin.test.Test
import kotlin.test.assertEquals
import com.sebastianneubauer.jsontree.JsonTreeElement.Collapsable.Array
import com.sebastianneubauer.jsontree.JsonTreeElement.Collapsable.Object
import kotlinx.serialization.json.JsonPrimitive

public class ExtensionsTest {

    @Test
    public fun `expand object with expansion none should not expand children`() {
        val result = ExpandTestData.testObject.expand(Expansion.None)

        assertEquals(
            actual = result,
            expected = ExpandTestData.testObject.copy(state = TreeState.EXPANDED)
        )
    }

    @Test
    public fun `expand object with expansion singleOnly should expand only single children`() {
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
    public fun `expand object with expansion all should expand all children`() {
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
    public fun `collapse object should collapse all children`() {
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
    public fun `collapsed object should be the only element in list`() {
        val result = ExpandTestData.testObject.toList()

        assertEquals(
            actual = result,
            expected = listOf(ExpandTestData.testObject)
        )
    }

    @Test
    public fun `expanded object with collapsed children should only have direct children in list`() {
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
    public fun `expanded object with expanded children should have all children in list`() {
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