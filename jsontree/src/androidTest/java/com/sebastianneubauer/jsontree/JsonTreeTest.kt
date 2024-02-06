package com.sebastianneubauer.jsontree

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
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
                initialState = initialState,
                onLoading = {}
            )
        }
    }

    @Test
    fun initial_state_is_first_item_expanded() {
        setJson(nestedJson)

        composeTestRule.onNodeWithText("\"topLevelObject\": { 2 items },").assertIsDisplayed()
        composeTestRule.onNodeWithText("\"topLevelArray\": [ 2 items ],").assertIsDisplayed()
        composeTestRule.onNodeWithText("\"emptyObject\": { 0 items }").assertIsDisplayed()

        composeTestRule.onNodeWithText("\"string\": \"stringValue\",").assertDoesNotExist()
        composeTestRule.onNodeWithText("\"hello\",").assertDoesNotExist()
    }

    @Test
    fun initial_state_is_collapsed() {
        setJson(nestedJson, initialState = TreeState.COLLAPSED)

        composeTestRule.onNodeWithText("{ 3 items }").assertIsDisplayed()
        composeTestRule.onNodeWithText("\"topLevelObject\"", substring = true).assertDoesNotExist()
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
        composeTestRule.onNodeWithText("[ 1 item ],").assertIsDisplayed()
        composeTestRule.onNodeWithText("[ 2 items ]").assertIsDisplayed()

        composeTestRule.onNodeWithText("[ 1 item ],").performClick()
        composeTestRule.onNodeWithText("[ 2 items ]").performClick()

        composeTestRule.onNodeWithText("\"stringValue\"").assertIsDisplayed()
        composeTestRule.onNodeWithText("],").assertIsDisplayed()
        composeTestRule.onNodeWithText("42,").assertIsDisplayed()
        composeTestRule.onNodeWithText("52").assertIsDisplayed()
    }

    @Test
    fun root_array_is_rendered_correctly() {
        setJson(rootArrayJson)

        composeTestRule.assertRootArrayIsDisplayed()
    }

    @Test
    fun root_string_is_rendered_correctly() {
        setJson(rootStringJson)

        composeTestRule.assertRootStringIsDisplayed()
    }

    @Test
    fun empty_object_is_rendered_correctly() {
        setJson(EMPTY_OBJECT_JSON)

        composeTestRule.assertEmptyObjectIsDisplayed()

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
                    onError = { throwable -> errorMessage = throwable.localizedMessage },
                    onLoading = {}
                )

                errorMessage?.let {
                    Text(
                        modifier = Modifier.testTag("errorText"),
                        text = it
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("errorText").assertIsDisplayed()
    }

    @Test
    fun changing_json_is_handled_correctly() {
        composeTestRule.setContent {
            var jsonString: String by remember { mutableStateOf(rootStringJson) }

            Column {
                JsonTree(json = jsonString, onLoading = {})

                Button(
                    modifier = Modifier.testTag("button"),
                    onClick = { jsonString = rootArrayJson }
                ) {}
            }
        }

        composeTestRule.assertRootStringIsDisplayed()

        composeTestRule.onNodeWithTag("button").performClick()

        composeTestRule.assertRootArrayIsDisplayed()
    }

    @Test
    fun changing_json_while_collapsed_is_handled_correctly() {
        composeTestRule.setContent {
            var jsonString: String by remember { mutableStateOf(nestedJson) }

            Column {
                JsonTree(
                    json = jsonString,
                    initialState = TreeState.COLLAPSED,
                    onLoading = {}
                )

                Button(
                    modifier = Modifier.testTag("button"),
                    onClick = { jsonString = rootArrayJson }
                ) {}
            }
        }

        composeTestRule.onNodeWithText("{ 3 items }").assertIsDisplayed()

        composeTestRule.onNodeWithTag("button").performClick()

        composeTestRule.onNodeWithText("[ 1 item ]").assertIsDisplayed()
    }

    @Test
    fun changing_json_from_invalid_to_valid_is_handled_correctly() {
        composeTestRule.setContent {
            var jsonString: String by remember { mutableStateOf(INVALID_JSON) }

            Column {
                JsonTree(
                    modifier = Modifier.testTag("jsonTree"),
                    json = jsonString,
                    onLoading = {}
                )

                Button(
                    modifier = Modifier.testTag("button"),
                    onClick = { jsonString = rootStringJson }
                ) {}
            }
        }

        composeTestRule.onNodeWithTag("button").performClick()
        composeTestRule.assertRootStringIsDisplayed()
    }

    @Test
    fun changing_json_from_valid_to_invalid_is_handled_correctly() {
        composeTestRule.setContent {
            var jsonString: String by remember { mutableStateOf(rootStringJson) }
            var errorMessage: String? by remember { mutableStateOf(null) }

            Column {
                JsonTree(
                    modifier = Modifier.testTag("jsonTree"),
                    json = jsonString,
                    onError = { throwable -> errorMessage = throwable.localizedMessage },
                    onLoading = {}
                )

                Button(
                    modifier = Modifier.testTag("button"),
                    onClick = { jsonString = INVALID_JSON }
                ) {}

                errorMessage?.let {
                    Text(
                        modifier = Modifier.testTag("errorText"),
                        text = it
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("jsonTree").assertIsDisplayed()
        composeTestRule.onNodeWithTag("errorText").assertDoesNotExist()

        composeTestRule.onNodeWithTag("button").performClick()

        composeTestRule.onNodeWithTag("errorText").assertIsDisplayed()
    }

    @Test
    fun changing_initial_state_is_handled_correctly() {
        composeTestRule.setContent {
            var initalState: TreeState by remember { mutableStateOf(TreeState.EXPANDED) }

            Column {
                JsonTree(
                    json = arrayOfArraysJson,
                    initialState = initalState,
                    onLoading = {}
                )

                Button(
                    modifier = Modifier.testTag("button"),
                    onClick = {
                        initalState = when (initalState) {
                            TreeState.EXPANDED -> TreeState.COLLAPSED
                            TreeState.COLLAPSED -> TreeState.FIRST_ITEM_EXPANDED
                            TreeState.FIRST_ITEM_EXPANDED -> TreeState.EXPANDED
                        }
                    }
                ) {}
            }
        }

        composeTestRule.onNodeWithText("\"array\": [").assertIsDisplayed()
        composeTestRule.onNodeWithText("\"stringValue\"").assertIsDisplayed()
        composeTestRule.onNodeWithText("42,").assertIsDisplayed()
        composeTestRule.onNodeWithText("52").assertIsDisplayed()

        composeTestRule.onNodeWithTag("button").performClick()

        composeTestRule.onNodeWithText("{ 1 item }").assertIsDisplayed()

        composeTestRule.onNodeWithTag("button").performClick()

        composeTestRule.onNodeWithText("\"array\": [ 2 items ]").assertIsDisplayed()
    }
}
