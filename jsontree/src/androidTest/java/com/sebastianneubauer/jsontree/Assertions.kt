package com.sebastianneubauer.jsontree

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText

internal fun ComposeTestRule.assertRootArrayIsDisplayed() {
    onNodeWithText("[").assertIsDisplayed()
    onNodeWithText("\"stringValue\"").assertIsDisplayed()
    onNodeWithText("]").assertIsDisplayed()
}

internal fun ComposeTestRule.assertRootStringIsDisplayed() {
    onNodeWithText("\"stringValue\"").assertIsDisplayed()
}

internal fun ComposeTestRule.assertEmptyObjectIsDisplayed() {
    onNodeWithText("{").assertIsDisplayed()
    onNodeWithText("}").assertIsDisplayed()
}
