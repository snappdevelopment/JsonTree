@file:OptIn(ExperimentalTestApi::class)

package com.sebastianneubauer.jsontree.ui

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
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilExactlyOneExists
import com.sebastianneubauer.jsontree.EMPTY_OBJECT_JSON
import com.sebastianneubauer.jsontree.INVALID_JSON
import com.sebastianneubauer.jsontree.JsonTree
import com.sebastianneubauer.jsontree.TreeState
import com.sebastianneubauer.jsontree.arrayOfArraysJson
import com.sebastianneubauer.jsontree.nestedJson2
import com.sebastianneubauer.jsontree.rootArrayJson
import com.sebastianneubauer.jsontree.rootStringJson
import kotlinx.coroutines.test.TestResult
import kotlin.test.Test

public class JsonTreeTest {

    private fun ComposeUiTest.setJson(
        json: String,
        initialState: TreeState = TreeState.FIRST_ITEM_EXPANDED,
    ) {
        setContent {
            JsonTree(
                json = json,
                initialState = initialState,
                onLoading = {},
            )
        }
    }

    @Test
    public fun initial_state_is_first_item_expanded(): TestResult = runComposeUiTest {
        setJson(nestedJson2)

        waitForIdle()
        onNodeWithText("\"topLevelObject\": { 2 items },").assertIsDisplayed()
        onNodeWithText("\"topLevelArray\": [ 2 items ],").assertIsDisplayed()
        onNodeWithText("\"emptyObject\": { 0 items }").assertIsDisplayed()

        onNodeWithText("\"string\": \"stringValue\",").assertDoesNotExist()
        onNodeWithText("\"hello\",").assertDoesNotExist()
    }

    @Test
    public fun initial_state_is_collapsed(): TestResult = runComposeUiTest {
        setJson(nestedJson2, initialState = TreeState.COLLAPSED)

        waitUntilExactlyOneExists(hasText("{ 3 items }"))

        onNodeWithText("{ 3 items }").assertIsDisplayed()
        onNodeWithText("\"topLevelObject\"", substring = true).assertDoesNotExist()
    }

    @Test
    public fun initial_state_is_expanded(): TestResult = runComposeUiTest {
        setJson(nestedJson2, initialState = TreeState.EXPANDED)

        // fix flakiness by waiting until first item appears
        waitUntilExactlyOneExists(hasText("\"topLevelObject\": {"))

        // every collapsable is expanded
        onNodeWithText("\"topLevelObject\": {").assertIsDisplayed()
        onNodeWithText("\"string\": \"stringValue\",").assertIsDisplayed()

        onNodeWithText("\"nestedObject\": {").assertIsDisplayed()
        onNodeWithText("\"int\": 42,").assertIsDisplayed()

        onNodeWithText("\"nestedArray\": [").assertIsDisplayed()
        onNodeWithText("\"nestedArrayValue\",").assertIsDisplayed()
        onNodeWithText("\"nestedArrayValue\"").assertIsDisplayed()

        onNodeWithText("\"arrayOfObjects\": [").assertIsDisplayed()
        onNodeWithText("\"anotherString\": \"anotherStringValue\"").assertIsDisplayed()
        onNodeWithText("\"anotherInt\": 52").assertIsDisplayed()

        onNodeWithText("\"topLevelArray\": [").assertIsDisplayed()
        onNodeWithText("\"hello\",").assertIsDisplayed()
        onNodeWithText("\"emptyObject\": {").assertIsDisplayed()
    }

    @Test
    public fun click_on_collapsed_object_or_array_expands_it(): TestResult = runComposeUiTest {
        setJson(nestedJson2)
        // fix flakiness by waiting until first item appears
        waitUntilExactlyOneExists(hasText("\"topLevelObject\": { 2 items },"))

        onNodeWithText("\"topLevelObject\": { 2 items },").performClick()
        waitUntilExactlyOneExists(hasText("\"topLevelObject\": {"))
        onNodeWithText("\"topLevelObject\": {").assertIsDisplayed()
        onNodeWithText("\"string\": \"stringValue\",").assertIsDisplayed()
        onNodeWithText("\"nestedObject\": { 3 items }").assertIsDisplayed()

        onNodeWithText("\"topLevelArray\": [ 2 items ],").performClick()
        waitUntilExactlyOneExists(hasText("\"topLevelArray\": ["))
        onNodeWithText("\"topLevelArray\": [").assertIsDisplayed()
        onNodeWithText("\"hello\",").assertIsDisplayed()
        onNodeWithText("\"world\"").assertIsDisplayed()
    }

    @Test
    public fun click_on_expanded_object_or_array_collapses_it(): TestResult = runComposeUiTest {
        setJson(nestedJson2, initialState = TreeState.EXPANDED)

        waitForIdle()

        onNodeWithText("\"arrayOfObjects\": [").performClick()
        waitUntilExactlyOneExists(hasText("\"arrayOfObjects\": [ 2 items ]"))
        onNodeWithText("\"arrayOfObjects\": [ 2 items ]").assertIsDisplayed()

        onNodeWithText("\"nestedObject\": {").performClick()
        waitUntilExactlyOneExists(hasText("\"nestedObject\": { 3 items }"))
        onNodeWithText("\"nestedObject\": { 3 items }").assertIsDisplayed()

        onNodeWithText("\"topLevelObject\": {").performClick()
        waitUntilExactlyOneExists(hasText("\"topLevelObject\": { 2 items },"))
        onNodeWithText("\"topLevelObject\": { 2 items },").assertIsDisplayed()
    }

    @Test
    public fun array_of_arrays_is_rendered_correctly(): TestResult = runComposeUiTest {
        setJson(arrayOfArraysJson)
        // fix flakiness by waiting until first item appears
        waitUntilExactlyOneExists(hasText("\"array\": [ 2 items ]"))

        onNodeWithText("\"array\": [ 2 items ]").assertIsDisplayed()
        onNodeWithText("\"array\": [ 2 items ]").performClick()

        waitUntilExactlyOneExists(hasText("\"array\": ["))
        onNodeWithText("\"array\": [").assertIsDisplayed()
        onNodeWithText("[ 1 item ],").assertIsDisplayed()
        onNodeWithText("[ 2 items ]").assertIsDisplayed()

        onNodeWithText("[ 1 item ],").performClick()
        waitUntilExactlyOneExists(hasText("\"stringValue\""))
        onNodeWithText("\"stringValue\"").assertIsDisplayed()
        onNodeWithText("],").assertIsDisplayed()

        onNodeWithText("[ 2 items ]").performClick()
        waitUntilExactlyOneExists(hasText("42,"))
        onNodeWithText("42,").assertIsDisplayed()
        onNodeWithText("52").assertIsDisplayed()
    }

    @Test
    public fun root_array_is_rendered_correctly(): TestResult = runComposeUiTest {
        setJson(rootArrayJson)

        assertRootArrayIsDisplayed()
    }

    @Test
    public fun root_string_is_rendered_correctly(): TestResult = runComposeUiTest {
        setJson(rootStringJson)

        assertRootStringIsDisplayed()
    }

    @Test
    public fun empty_object_is_rendered_correctly(): TestResult = runComposeUiTest {
        setJson(EMPTY_OBJECT_JSON)
        waitForIdle()
        assertEmptyObjectIsDisplayed()

        onNodeWithText("{").performClick()
        waitUntilExactlyOneExists(hasText("{ 0 items }"))
        onNodeWithText("{ 0 items }").assertIsDisplayed()
    }

    @Test
    public fun invalid_json_shows_an_error(): TestResult = runComposeUiTest {
        setContent {
            Box {
                var errorMessage: String? by remember { mutableStateOf(null) }

                JsonTree(
                    modifier = Modifier.testTag("jsonTree"),
                    json = INVALID_JSON,
                    onError = { throwable -> errorMessage = throwable.message },
                    onLoading = {},
                )

                errorMessage?.let {
                    Text(
                        modifier = Modifier.testTag("errorText"),
                        text = it
                    )
                }
            }
        }

        onNodeWithTag("errorText").assertIsDisplayed()
    }

    @Test
    public fun changing_json_is_handled_correctly(): TestResult = runComposeUiTest {
        setContent {
            var jsonString: String by remember { mutableStateOf(rootStringJson) }

            Column {
                JsonTree(json = jsonString, onLoading = {})

                Button(
                    modifier = Modifier.testTag("button"),
                    onClick = { jsonString = rootArrayJson }
                ) {}
            }
        }

        assertRootStringIsDisplayed()

        onNodeWithTag("button").performClick()

        assertRootArrayIsDisplayed()
    }

    @Test
    public fun changing_json_while_collapsed_is_handled_correctly(): TestResult = runComposeUiTest {
        setContent {
            var jsonString: String by remember { mutableStateOf(nestedJson2) }

            Column {
                JsonTree(
                    json = jsonString,
                    initialState = TreeState.COLLAPSED,
                    onLoading = {},
                )

                Button(
                    modifier = Modifier.testTag("button"),
                    onClick = { jsonString = rootArrayJson }
                ) {}
            }
        }

        onNodeWithText("{ 3 items }").assertIsDisplayed()

        onNodeWithTag("button").performClick()

        onNodeWithText("[ 1 item ]").assertIsDisplayed()
    }

    @Test
    public fun changing_json_from_invalid_to_valid_is_handled_correctly(): TestResult = runComposeUiTest {
        setContent {
            var jsonString: String by remember { mutableStateOf(INVALID_JSON) }

            Column {
                JsonTree(
                    modifier = Modifier.testTag("jsonTree"),
                    json = jsonString,
                    onLoading = {},
                )

                Button(
                    modifier = Modifier.testTag("button"),
                    onClick = { jsonString = rootStringJson }
                ) {}
            }
        }

        onNodeWithTag("button").performClick()
        assertRootStringIsDisplayed()
    }

    @Test
    public fun changing_json_from_valid_to_invalid_is_handled_correctly(): TestResult = runComposeUiTest {
        setContent {
            var jsonString: String by remember { mutableStateOf(rootStringJson) }
            var errorMessage: String? by remember { mutableStateOf(null) }

            Column {
                JsonTree(
                    modifier = Modifier.testTag("jsonTree"),
                    json = jsonString,
                    onError = { throwable -> errorMessage = throwable.message },
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

        // fix flakiness by waiting until first item appears
        waitUntilExactlyOneExists(hasTestTag("jsonTree"))

        onNodeWithTag("jsonTree").assertIsDisplayed()
        onNodeWithTag("errorText").assertDoesNotExist()

        onNodeWithTag("button").performClick()

        onNodeWithTag("errorText").assertIsDisplayed()
    }

    @Test
    public fun changing_initial_state_is_handled_correctly(): TestResult = runComposeUiTest {
        setContent {
            var initalState: TreeState by remember { mutableStateOf(TreeState.EXPANDED) }

            Column {
                JsonTree(json = arrayOfArraysJson, initialState = initalState, onLoading = {})

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

        onNodeWithText("\"array\": [").assertIsDisplayed()
        onNodeWithText("\"stringValue\"").assertIsDisplayed()
        onNodeWithText("42,").assertIsDisplayed()
        onNodeWithText("52").assertIsDisplayed()

        onNodeWithTag("button").performClick()

        onNodeWithText("{ 1 item }").assertIsDisplayed()

        onNodeWithTag("button").performClick()

        onNodeWithText("\"array\": [ 2 items ]").assertIsDisplayed()
    }

    @Test
    public fun showing_and_hiding_indices_is_handled_correctly(): TestResult = runComposeUiTest {
        setContent {
            var showIndices: Boolean by remember { mutableStateOf(true) }

            Column {
                JsonTree(
                    json = arrayOfArraysJson,
                    onLoading = {},
                    showIndices = showIndices,
                    initialState = TreeState.EXPANDED
                )

                Button(
                    modifier = Modifier.testTag("button"),
                    onClick = { showIndices = !showIndices }
                ) {}
            }
        }

        onNodeWithText("0: [").assertIsDisplayed()
        onNodeWithText("0: \"stringValue\"").assertIsDisplayed()
        onNodeWithText("1: [").assertIsDisplayed()
        onNodeWithText("0: 42,").assertIsDisplayed()
        onNodeWithText("1: 52").assertIsDisplayed()

        onNodeWithTag("button").performClick()

        onAllNodesWithText("[").assertCountEquals(2)
        onNodeWithText("\"stringValue\"").assertIsDisplayed()
        onNodeWithText("42,").assertIsDisplayed()
        onNodeWithText("52").assertIsDisplayed()
    }

    @Test
    public fun showing_and_hiding_item_count_is_handled_correctly(): TestResult = runComposeUiTest {
        setContent {
            var showItemCount: Boolean by remember { mutableStateOf(true) }

            Column {
                JsonTree(
                    json = rootArrayJson,
                    onLoading = {},
                    showItemCount = showItemCount,
                    initialState = TreeState.COLLAPSED
                )

                Button(
                    modifier = Modifier.testTag("button"),
                    onClick = { showItemCount = !showItemCount }
                ) {}
            }
        }

        onNodeWithText("[ 1 item ]").assertIsDisplayed()

        onNodeWithTag("button").performClick()

        onNodeWithText("[ ... ]").assertIsDisplayed()

        onNodeWithTag("button").performClick()

        onNodeWithText("[ 1 item ]").assertIsDisplayed()
    }

    @Test
    public fun expanding_single_children_is_handled_correctly(): TestResult = runComposeUiTest {
        setContent {
            JsonTree(
                json = arrayOfArraysJson,
                initialState = TreeState.COLLAPSED,
                onLoading = {},
                expandSingleChildren = true
            )
        }
        waitUntilExactlyOneExists(hasText("{ 1 item }"))
        onNodeWithText("{ 1 item }").performClick()
        waitUntilExactlyOneExists(hasText("\"array\": ["))
        // shows nested array
        onNodeWithText("\"array\": [").assertIsDisplayed()
        // shows collapsed contents of nested array
        onNodeWithText("[ 1 item ],").assertIsDisplayed()
        onNodeWithText("[ 2 items ]").assertIsDisplayed()

        // collapse root
        onNodeWithText("{").performClick()
        waitUntilExactlyOneExists(hasText("{ 1 item }"))
        onNodeWithText("{ 1 item }").assertIsDisplayed()
    }

    @Test
    public fun not_expanding_single_children_is_handled_correctly(): TestResult = runComposeUiTest {
        setContent {
            JsonTree(
                json = arrayOfArraysJson,
                initialState = TreeState.COLLAPSED,
                onLoading = {},
                expandSingleChildren = false
            )
        }
        waitUntilExactlyOneExists(hasText("{ 1 item }"))
        onNodeWithText("{ 1 item }").performClick()

        waitUntilExactlyOneExists(hasText("\"array\": [ 2 items ]"))
        onNodeWithText("\"array\": [ 2 items ]").assertIsDisplayed()
        onNodeWithText("\"array\": [ 2 items ]").performClick()

        waitUntilExactlyOneExists(hasText("[ 1 item ],"))
        onNodeWithText("[ 1 item ],").assertIsDisplayed()
        onNodeWithText("[ 2 items ]").assertIsDisplayed()

        // collapse root
        onNodeWithText("{").performClick()
        waitUntilExactlyOneExists(hasText("{ 1 item }"))
        onNodeWithText("{ 1 item }").assertIsDisplayed()
    }
}
