package com.sebastianneubauer.jsontree.diff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sebastianneubauer.jsontree.CollapsableType
import com.sebastianneubauer.jsontree.JsonTreeElement
import com.sebastianneubauer.jsontree.diff.JsonTreeDifferState.JsonDiffElement
import com.sebastianneubauer.jsontree.diff.DiffError.OriginalJsonError
import com.sebastianneubauer.jsontree.diff.DiffError.RevisedJsonError
import kotlinx.coroutines.Dispatchers

/**
 * Renders a side-by-side diff of two JSON strings with syntax highlighting.
 *
 * @param originalJson The original JSON data as a string.
 * @param revisedJson The revised JSON data as a string which is compared with [originalJson].
 * @param onLoading A Composable which is show while the diff is being calculated.
 * @param onError A Composable which is shown if an error occurs. Receives an [JsonTreeDiffError] object with more information.
 * @param onSuccess A callback which is called when the diff calculation succeeded. Receives an [JsonTreeDiffSuccess] object with more information.
 * @param modifier The Modifier which is applied on the side-by-side diff. Not applied on the [onLoading] and [onError] slots.
 * @param showInlineDiffs If true, the diff shows partial changes within the text of a line.
 * @param contentPadding The content padding which is applied on the LazyColumn of the side-by-side diff.
 * @param colors The color palette the diff uses. [defaultLightDiffColors], [defaultDarkDiffColors] or a custom instance of [JsonTreeDiffColors].
 * @param textStyle The style which is used for all texts in the diff.
 */
@Composable
public fun JsonTreeDiff(
    originalJson: String,
    revisedJson: String,
    onLoading: @Composable () -> Unit,
    onError: @Composable (JsonTreeDiffError) -> Unit,
    onSuccess: (JsonTreeDiffSuccess) -> Unit= {},
    modifier: Modifier = Modifier,
    showInlineDiffs: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    colors: JsonTreeDiffColors = defaultLightDiffColors,
    textStyle: TextStyle = LocalTextStyle.current
) {
    // resets the internal state to avoid rendering an outdated state and calling onSuccess, until the LaunchedEffect is called
    val jsonTreeDiffer = remember(originalJson, revisedJson, showInlineDiffs) {
        JsonTreeDiffer(
            defaultDispatcher = Dispatchers.Default,
            mainDispatcher = Dispatchers.Main
        )
    }

    LaunchedEffect(jsonTreeDiffer) {
        jsonTreeDiffer.diff(
            original = originalJson,
            revised = revisedJson,
            showInlineDiffs = showInlineDiffs
        )
    }

    when(val state = jsonTreeDiffer.state.value) {
        is JsonTreeDifferState.Loading -> onLoading()
        is JsonTreeDifferState.Ready -> {
            onSuccess(JsonTreeDiffSuccess(state.diffInfo))

            Box(modifier = modifier) {
                SideBySideDiff(
                    state = state,
                    colors = colors,
                    textStyle = textStyle,
                    contentPadding = contentPadding
                )
            }
        }
        is JsonTreeDifferState.Error.OriginalJsonError -> onError(JsonTreeDiffError(OriginalJsonError(state.throwable)))
        is JsonTreeDifferState.Error.RevisedJsonError -> onError(JsonTreeDiffError(RevisedJsonError(state.throwable)))
    }
}

@Composable
private fun SideBySideDiff(
    state: JsonTreeDifferState.Ready,
    colors: JsonTreeDiffColors,
    textStyle: TextStyle,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.background(color = colors.regularBackgroundColor),
        contentPadding = contentPadding
    ) {
        itemsIndexed(
            items = state.diffElements,
            key = { index, _ -> index }
        ) { index, (originalDiffElement, revisedDiffElement) ->
            val originalJsonTreeElement = when(originalDiffElement) {
                is JsonDiffElement.Change -> originalDiffElement.jsonTreeElement
                is JsonDiffElement.Deletion -> originalDiffElement.jsonTreeElement!!
                is JsonDiffElement.Equal -> originalDiffElement.jsonTreeElement
                is JsonDiffElement.Insertion -> null
            }
            val originalText = rememberText(
                jsonTreeElement = originalJsonTreeElement,
                diffIndices = if(originalDiffElement is JsonDiffElement.Change) {
                    originalDiffElement.inlineDiffIndices
                } else null,
                colors = colors,
                highlightColor = colors.deletionHighlightColor,
            )

            val revisedJsonTreeElement = when(revisedDiffElement) {
                is JsonDiffElement.Change -> revisedDiffElement.jsonTreeElement
                is JsonDiffElement.Deletion -> null
                is JsonDiffElement.Equal -> revisedDiffElement.jsonTreeElement
                is JsonDiffElement.Insertion -> revisedDiffElement.jsonTreeElement!!
            }
            val revisedText = rememberText(
                jsonTreeElement = revisedJsonTreeElement,
                diffIndices = if(revisedDiffElement is JsonDiffElement.Change) {
                    revisedDiffElement.inlineDiffIndices
                } else null,
                colors = colors,
                highlightColor = colors.insertionHighlightColor
            )

            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .fillMaxWidth()
            ) {
                DiffText(
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxHeight(),
                    backgroundColor = when(originalDiffElement) {
                        is JsonDiffElement.Change -> colors.deletionBackgroundColor
                        is JsonDiffElement.Deletion -> colors.deletionBackgroundColor
                        is JsonDiffElement.Equal -> colors.regularBackgroundColor
                        is JsonDiffElement.Insertion -> colors.changeBackgroundColor
                    },
                    backgroundFillColor = colors.changeBackgroundColor,
                    indent = if(originalJsonTreeElement != null && index > 0) {
                        20.dp * originalJsonTreeElement.level
                    } else {
                        0.dp
                    },
                    textStyle = textStyle,
                    text = originalText,
                )

                DiffText(
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxHeight(),
                    backgroundColor = when(revisedDiffElement) {
                        is JsonDiffElement.Change -> colors.insertionBackgroundColor
                        is JsonDiffElement.Deletion -> colors.changeBackgroundColor
                        is JsonDiffElement.Equal -> colors.regularBackgroundColor
                        is JsonDiffElement.Insertion -> colors.insertionBackgroundColor
                    },
                    backgroundFillColor = colors.changeBackgroundColor,
                    indent = if(revisedJsonTreeElement != null && index > 0) {
                        20.dp * revisedJsonTreeElement.level
                    } else {
                        0.dp
                    },
                    textStyle = textStyle,
                    text = revisedText
                )
            }
        }
    }
}

@Composable
private fun DiffText(
    modifier: Modifier,
    backgroundColor: Color,
    backgroundFillColor: Color,
    indent: Dp,
    textStyle: TextStyle,
    text: AnnotatedString,
) {
    Column(modifier = modifier) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = backgroundColor)
                .padding(start = indent),
            style = textStyle,
            text = text,
        )

        // If the change on the other side spans multiple lines,
        // fill the rest of this side with a background color
        Box(
            modifier = Modifier
                .weight(1F)
                .fillMaxWidth()
                .background(color = backgroundFillColor)
        )
    }
}

@Composable
private fun rememberText(
    jsonTreeElement: JsonTreeElement?,
    diffIndices: List<Pair<Int, Int>>?,
    colors: JsonTreeDiffColors,
    highlightColor: Color,
): AnnotatedString {
    return when(jsonTreeElement) {
        is JsonTreeElement.Collapsable.Array -> rememberCollapsableDiffText(
            type = CollapsableType.ARRAY,
            key = jsonTreeElement.key,
            isLastItem = jsonTreeElement.isLastItem,
            parentType = jsonTreeElement.parentType,
            colors = colors,
            highlightColor = highlightColor,
            diffIndices = diffIndices,
        )
        is JsonTreeElement.Collapsable.Object -> rememberCollapsableDiffText(
            type = CollapsableType.OBJECT,
            key = jsonTreeElement.key,
            isLastItem = jsonTreeElement.isLastItem,
            parentType = jsonTreeElement.parentType,
            colors = colors,
            highlightColor = highlightColor,
            diffIndices = diffIndices,
        )
        is JsonTreeElement.Primitive -> rememberPrimitiveDiffText(
            key = jsonTreeElement.key,
            value = jsonTreeElement.value,
            isLastItem = jsonTreeElement.isLastItem,
            parentType = jsonTreeElement.parentType,
            colors = colors,
            highlightColor = highlightColor,
            diffIndices = diffIndices,
        )
        is JsonTreeElement.EndBracket -> rememberEndBracketDiffText(
            type = jsonTreeElement.type,
            isLastItem = jsonTreeElement.isLastItem,
            colors = colors
        )
        null -> AnnotatedString("")
    }
}