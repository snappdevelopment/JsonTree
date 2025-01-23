package com.sebastianneubauer.jsontree

import com.sebastianneubauer.jsontree.JsonTreeParserTest.Companion.underTest
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

public class JsonSearchResultTest {

    @Test
    public fun testBuildJsonQueryNestedJson(): TestResult = runTest {
        val underTest = underTest(nestedJson, TreeState.EXPANDED)
        val res = listOf(
            "", // root object
            "array",
            "array[0]",
            "array[0][0]",
            "array[0][0].string",
            "array[1]",
            "array[1][0]",
            "array[1][1]",
        )
        (underTest.state.value as JsonTreeParserState.Ready).list.filter { it !is JsonTreeElement.EndBracket }
            .forEachIndexed { i, item ->
                assertEquals(res[i], item.buildJsonQuery())
            }
    }

    @Test
    public fun testBuildJsonQueryNestedJson2(): TestResult = runTest {
        val underTest = underTest(nestedJson2, TreeState.EXPANDED)
        val res = listOf(
            "", // root object
            "topLevelObject",
            "topLevelObject.string",
            "topLevelObject.nestedObject",
            "topLevelObject.nestedObject.int",
            "topLevelObject.nestedObject.nestedArray",
            "topLevelObject.nestedObject.nestedArray[0]",
            "topLevelObject.nestedObject.nestedArray[1]",
            "topLevelObject.nestedObject.arrayOfObjects",
            "topLevelObject.nestedObject.arrayOfObjects[0]",
            "topLevelObject.nestedObject.arrayOfObjects[0].anotherString",
            "topLevelObject.nestedObject.arrayOfObjects[1]",
            "topLevelObject.nestedObject.arrayOfObjects[1].anotherInt",
            "topLevelArray",
            "topLevelArray[0]",
            "topLevelArray[1]",
            "emptyObject",
        )
        (underTest.state.value as JsonTreeParserState.Ready).list.filter { it !is JsonTreeElement.EndBracket }
            .forEachIndexed { i, item ->
                assertEquals(res[i], item.buildJsonQuery())
            }
    }

    @Test
    public fun testBuildJsonQueryArrayOfArray(): TestResult = runTest {
        val underTest = underTest(arrayOfArraysJson, TreeState.EXPANDED)
        val res = listOf(
            "", // root object
            "array",
            "array[0]",
            "array[0][0]",
            "array[1]",
            "array[1][0]",
            "array[1][1]"
        )
        (underTest.state.value as JsonTreeParserState.Ready).list.filter { it !is JsonTreeElement.EndBracket }
            .forEachIndexed { i, item ->
                assertEquals(res[i], item.buildJsonQuery())
            }
    }
}
