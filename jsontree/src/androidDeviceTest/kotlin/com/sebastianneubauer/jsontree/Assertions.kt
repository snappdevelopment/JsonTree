@file:OptIn(ExperimentalTestApi::class)

package com.sebastianneubauer.jsontree

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText

internal fun ComposeUiTest.assertRootArrayIsDisplayed() {
    onNodeWithText("[").assertIsDisplayed()
    onNodeWithText("\"stringValue\"").assertIsDisplayed()
    onNodeWithText("]").assertIsDisplayed()
}

internal fun ComposeUiTest.assertRootStringIsDisplayed() {
    onNodeWithText("\"stringValue\"").assertIsDisplayed()
}

internal fun ComposeUiTest.assertEmptyObjectIsDisplayed() {
    onNodeWithText("{").assertIsDisplayed()
    onNodeWithText("}").assertIsDisplayed()
}
