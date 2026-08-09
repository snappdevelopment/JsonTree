package com.sebastianneubauer.jsontree

import com.sebastianneubauer.jsontree.JsonTreeElement.Collapsable.Array
import com.sebastianneubauer.jsontree.JsonTreeElement.Collapsable.Object
import com.sebastianneubauer.jsontree.JsonTreeElement.Primitive.Type
import com.sebastianneubauer.jsontree.util.Expansion
import com.sebastianneubauer.jsontree.util.collapse
import com.sebastianneubauer.jsontree.util.expand
import com.sebastianneubauer.jsontree.util.toList
import com.sebastianneubauer.jsontree.util.toRenderString
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

public class JsonTreeElementExtensionsTest {

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

    @Test
    public fun toRenderString_object_with_key_and_object_parent_should_render_with_key() {
        val element = Object(
            id = "",
            level = 0,
            state = TreeState.COLLAPSED,
            children = emptyMap(),
            isLastItem = true,
            key = "myKey",
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val result = element.toRenderString()

        assertEquals(
            actual = result,
            expected = "\"myKey\": {"
        )
    }

    @Test
    public fun toRenderString_object_with_key_and_array_parent_should_render_without_key() {
        val element = Object(
            id = "",
            level = 0,
            state = TreeState.COLLAPSED,
            children = emptyMap(),
            isLastItem = true,
            key = "0",
            parentType = JsonTreeElement.ParentType.ARRAY
        )

        val result = element.toRenderString()

        assertEquals(
            actual = result,
            expected = "{"
        )
    }

    @Test
    public fun toRenderString_object_without_key_should_render_without_key() {
        val element = Object(
            id = "",
            level = 0,
            state = TreeState.COLLAPSED,
            children = emptyMap(),
            isLastItem = true,
            key = null,
            parentType = JsonTreeElement.ParentType.NONE
        )

        val result = element.toRenderString()

        assertEquals(
            actual = result,
            expected = "{"
        )
    }

    @Test
    public fun toRenderString_array_with_key_and_object_parent_should_render_with_key() {
        val element = Array(
            id = "",
            level = 0,
            state = TreeState.COLLAPSED,
            children = emptyMap(),
            isLastItem = true,
            key = "myArray",
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val result = element.toRenderString()

        assertEquals(
            actual = result,
            expected = "\"myArray\": ["
        )
    }

    @Test
    public fun toRenderString_array_with_key_and_array_parent_should_render_without_key() {
        val element = Array(
            id = "",
            level = 0,
            state = TreeState.COLLAPSED,
            children = emptyMap(),
            isLastItem = true,
            key = "0",
            parentType = JsonTreeElement.ParentType.ARRAY
        )

        val result = element.toRenderString()

        assertEquals(
            actual = result,
            expected = "["
        )
    }

    @Test
    public fun toRenderString_array_without_key_should_render_without_key() {
        val element = Array(
            id = "",
            level = 0,
            state = TreeState.COLLAPSED,
            children = emptyMap(),
            isLastItem = true,
            key = null,
            parentType = JsonTreeElement.ParentType.NONE
        )

        val result = element.toRenderString()

        assertEquals(
            actual = result,
            expected = "["
        )
    }

    @Test
    public fun toRenderString_primitive_with_key_and_object_parent_not_last_should_render_with_key_and_comma() {
        val element = JsonTreeElement.Primitive(
            id = "",
            level = 0,
            isLastItem = false,
            key = "myProp",
            value = "test",
            type = Type.STRING,
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val result = element.toRenderString()

        assertEquals(
            actual = result,
            expected = "\"myProp\": \"test\","
        )
    }

    @Test
    public fun toRenderString_primitive_with_key_and_object_parent_last_should_render_with_key_and_without_comma() {
        val element = JsonTreeElement.Primitive(
            id = "",
            level = 0,
            isLastItem = true,
            key = "myProp",
            value = "test",
            type = Type.STRING,
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val result = element.toRenderString()

        assertEquals(
            actual = result,
            expected = "\"myProp\": \"test\""
        )
    }

    @Test
    public fun toRenderString_primitive_with_key_and_array_parent_not_last_should_render_without_key_and_with_comma() {
        val element = JsonTreeElement.Primitive(
            id = "",
            level = 0,
            isLastItem = false,
            key = "0",
            value = "42",
            type = Type.NUMBER,
            parentType = JsonTreeElement.ParentType.ARRAY
        )

        val result = element.toRenderString()

        assertEquals(
            actual = result,
            expected = "42,"
        )
    }

    @Test
    public fun toRenderString_primitive_with_key_and_array_parent_last_should_render_without_key_and_without_comma() {
        val element = JsonTreeElement.Primitive(
            id = "",
            level = 0,
            isLastItem = true,
            key = "0",
            value = "42",
            type = Type.NUMBER,
            parentType = JsonTreeElement.ParentType.ARRAY
        )

        val result = element.toRenderString()

        assertEquals(
            actual = result,
            expected = "42"
        )
    }

    @Test
    public fun toRenderString_primitive_without_key_not_last_should_render_without_key_and_with_comma() {
        val element = JsonTreeElement.Primitive(
            id = "",
            level = 0,
            isLastItem = false,
            key = null,
            value = "true",
            type = Type.BOOLEAN,
            parentType = JsonTreeElement.ParentType.NONE
        )

        val result = element.toRenderString()

        assertEquals(
            actual = result,
            expected = "true,"
        )
    }

    @Test
    public fun toRenderString_primitive_without_key_last_should_render_without_key_and_without_comma() {
        val element = JsonTreeElement.Primitive(
            id = "",
            level = 0,
            isLastItem = true,
            key = null,
            value = "false",
            type = Type.BOOLEAN,
            parentType = JsonTreeElement.ParentType.NONE
        )

        val result = element.toRenderString()

        assertEquals(
            actual = result,
            expected = "false"
        )
    }

    @Test
    public fun toRenderString_end_bracket_array_not_last_should_render_with_comma() {
        val element = JsonTreeElement.EndBracket(
            id = "",
            level = 0,
            isLastItem = false,
            type = JsonTreeElement.EndBracket.Type.ARRAY
        )

        val result = element.toRenderString()

        assertEquals(
            actual = result,
            expected = "],"
        )
    }

    @Test
    public fun toRenderString_end_bracket_array_last_should_render_without_comma() {
        val element = JsonTreeElement.EndBracket(
            id = "",
            level = 0,
            isLastItem = true,
            type = JsonTreeElement.EndBracket.Type.ARRAY
        )

        val result = element.toRenderString()

        assertEquals(
            actual = result,
            expected = "]"
        )
    }

    @Test
    public fun toRenderString_end_bracket_object_not_last_should_render_with_comma() {
        val element = JsonTreeElement.EndBracket(
            id = "",
            level = 0,
            isLastItem = false,
            type = JsonTreeElement.EndBracket.Type.OBJECT
        )

        val result = element.toRenderString()

        assertEquals(
            actual = result,
            expected = "},"
        )
    }

    @Test
    public fun toRenderString_end_bracket_object_last_should_render_without_comma() {
        val element = JsonTreeElement.EndBracket(
            id = "",
            level = 0,
            isLastItem = true,
            type = JsonTreeElement.EndBracket.Type.OBJECT
        )

        val result = element.toRenderString()

        assertEquals(
            actual = result,
            expected = "}"
        )
    }

    private object ExpandTestData {
        val primitive1 = JsonTreeElement.Primitive(
            id = "primitive1",
            level = 2,
            isLastItem = false,
            key = "0",
            value = "value1",
            type = Type.STRING,
            parentType = JsonTreeElement.ParentType.ARRAY
        )

        val primitive2 = JsonTreeElement.Primitive(
            id = "primitive2",
            level = 3,
            isLastItem = true,
            key = "primitive2",
            value = "value2",
            type = Type.STRING,
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
            value = "value1",
            type = Type.STRING,
            parentType = JsonTreeElement.ParentType.ARRAY
        )

        val primitive2 = JsonTreeElement.Primitive(
            id = "primitive2",
            level = 3,
            isLastItem = true,
            key = "primitive2",
            value = "value2",
            type = Type.STRING,
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
