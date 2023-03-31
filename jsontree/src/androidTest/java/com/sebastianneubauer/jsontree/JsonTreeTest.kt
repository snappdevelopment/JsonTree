package com.sebastianneubauer.jsontree

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

internal class JsonTreeTest {

    @JvmField
    @Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun setJson(
        json: String,
        initialState: TreeState = TreeState.FIRST_ITEM_EXPANDED,
    ) {
        composeTestRule.setContent {
            JsonTree(
                json = json,
                initialState = initialState
            )
        }
    }

    @Test
    fun initial_state_is_first_item_expanded() {
        setJson(nestedJson)

        composeTestRule.onNodeWithText("\"topLevelObject\": { 2 items },").assertIsDisplayed()
        composeTestRule.onNodeWithText("\"topLevelArray\": [ 2 items ],").assertIsDisplayed()
        composeTestRule.onNodeWithText("\"emptyObject\": { 0 items }").assertIsDisplayed()

        composeTestRule.onNodeWithText("\"string\": \"stringValue\",").assertIsNotDisplayed()
        composeTestRule.onNodeWithText("\"hello\",").assertIsNotDisplayed()
    }

    @Test
    fun initial_state_is_collapsed() {
        setJson(nestedJson, initialState = TreeState.COLLAPSED)

        composeTestRule.onNodeWithText("{ 3 items }").assertIsDisplayed()
        composeTestRule.onNodeWithText("\"topLevelObject\"", substring = true).assertIsNotDisplayed()
    }

    @Test
    fun initial_state_is_expanded() {
        setJson(nestedJson, initialState = TreeState.EXPANDED)

        // every collapsable is expanded
        composeTestRule.onNodeWithText("\"topLevelObject\": {").assertIsDisplayed()
        composeTestRule.onNodeWithText("\"string\": \"stringValue\",").assertIsDisplayed()

        composeTestRule.onNodeWithText("\"nestedObject\": {").assertIsDisplayed()
        composeTestRule.onNodeWithText("\"int\": 42,").assertIsDisplayed()

        composeTestRule.onNodeWithText("\"nestedArray\": [").assertIsDisplayed()
        composeTestRule.onNodeWithText("\"nestedArrayValue\",").assertIsDisplayed()
        composeTestRule.onNodeWithText("\"nestedArrayValue\"").assertIsDisplayed()

        composeTestRule.onNodeWithText("\"arrayOfObjects\": [").assertIsDisplayed()
        composeTestRule.onNodeWithText("\"anotherString\": \"anotherStringValue\"").assertIsDisplayed()
        composeTestRule.onNodeWithText("\"anotherInt\": 52").assertIsDisplayed()

        composeTestRule.onNodeWithText("\"topLevelArray\": [").assertIsDisplayed()
        composeTestRule.onNodeWithText("\"hello\",").assertIsDisplayed()
        composeTestRule.onNodeWithText("\"emptyObject\": {").assertIsDisplayed()
    }

    @Test
    fun click_on_collapsed_object_or_array_expands_it() {
        setJson(nestedJson)

        composeTestRule.onNodeWithText("\"topLevelObject\": { 2 items },").performClick()
        composeTestRule.onNodeWithText("\"topLevelObject\": {").assertIsDisplayed()
        composeTestRule.onNodeWithText("\"string\": \"stringValue\",").assertIsDisplayed()
        composeTestRule.onNodeWithText("\"nestedObject\": { 3 items }").assertIsDisplayed()

        composeTestRule.onNodeWithText("\"topLevelArray\": [ 2 items ],").performClick()
        composeTestRule.onNodeWithText("\"topLevelArray\": [").assertIsDisplayed()
        composeTestRule.onNodeWithText("\"hello\",").assertIsDisplayed()
        composeTestRule.onNodeWithText("\"world\"").assertIsDisplayed()
    }

    @Test
    fun click_on_expanded_object_or_array_collapses_it() {
        setJson(nestedJson, initialState = TreeState.EXPANDED)

        composeTestRule.onNodeWithText("\"arrayOfObjects\": [").performClick()
        composeTestRule.onNodeWithText("\"arrayOfObjects\": [ 2 items ]").assertIsDisplayed()

        composeTestRule.onNodeWithText("\"nestedObject\": {").performClick()
        composeTestRule.onNodeWithText("\"nestedObject\": { 3 items }").assertIsDisplayed()

        composeTestRule.onNodeWithText("\"topLevelObject\": {").performClick()
        composeTestRule.onNodeWithText("\"topLevelObject\": { 2 items },").assertIsDisplayed()
    }

    @Test
    fun array_of_arrays_is_rendered_correctly() {
        setJson(arrayOfArraysJson)

        composeTestRule.onNodeWithText("\"array\": [ 2 items ]").assertIsDisplayed()
        composeTestRule.onNodeWithText("\"array\": [ 2 items ]").performClick()
        composeTestRule.onNodeWithText("\"array\": [").assertIsDisplayed()
        composeTestRule.onNodeWithText("[ 1 items ],").assertIsDisplayed()
        composeTestRule.onNodeWithText("[ 2 items ]").assertIsDisplayed()

        composeTestRule.onNodeWithText("[ 1 items ],").performClick()
        composeTestRule.onNodeWithText("[ 2 items ]").performClick()

        composeTestRule.onNodeWithText("\"stringValue\"").assertIsDisplayed()
        composeTestRule.onNodeWithText("],").assertIsDisplayed()
        composeTestRule.onNodeWithText("42,").assertIsDisplayed()
        composeTestRule.onNodeWithText("52").assertIsDisplayed()
    }

    @Test
    fun root_array_is_rendered_correctly() {
        setJson(rootArrayJson)

        composeTestRule.onNodeWithText("[").assertIsDisplayed()
        composeTestRule.onNodeWithText("\"stringValue\"").assertIsDisplayed()
        composeTestRule.onNodeWithText("]").assertIsDisplayed()
    }

    @Test
    fun root_string_is_rendered_correctly() {
        setJson(rootStringJson)

        composeTestRule.onNodeWithText("\"stringValue\"").assertIsDisplayed()
    }

    @Test
    fun empty_object_is_rendered_correctly() {
        setJson(EMPTY_OBJECT_JSON)

        composeTestRule.onNodeWithText("{").assertIsDisplayed()
        composeTestRule.onNodeWithText("}").assertIsDisplayed()

        composeTestRule.onNodeWithText("{").performClick()
        composeTestRule.onNodeWithText("{ 0 items }").assertIsDisplayed()
    }

    @Test
    fun invalid_json_shows_an_error() {
        composeTestRule.setContent {
            Box {
                var errorMessage: String? by remember { mutableStateOf(null) }

                JsonTree(
                    modifier = Modifier.testTag("jsonTree"),
                    json = INVALID_JSON,
                    onError = { throwable -> errorMessage = throwable.localizedMessage }
                )

                errorMessage?.let {
                    Text(
                        modifier = Modifier.testTag("errorText"),
                        text = it
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("jsonTree").assertDoesNotExist()
        composeTestRule.onNodeWithTag("errorText").assertIsDisplayed()
    }
}
