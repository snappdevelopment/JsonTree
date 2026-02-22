package com.sebastianneubauer.jsontree.diff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sebastianneubauer.jsontree.CollapsableType
import com.sebastianneubauer.jsontree.JsonTreeElement
import com.sebastianneubauer.jsontree.diff.JsonTreeDifferState.JsonDiffElement
import com.sebastianneubauer.jsontree.diff.JsonTreeDiffError.OriginalJsonError
import com.sebastianneubauer.jsontree.diff.JsonTreeDiffError.RevisedJsonError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
public fun JsonTreeDiff(
    originalJson: String,
    revisedJson: String,
    onLoading: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    colors: JsonTreeDiffColors = defaultLightDiffColors,
    textStyle: TextStyle = LocalTextStyle.current,
    onError: (JsonTreeDiffError) -> Unit = {}
) {
    val jsonTreeDiffer = remember(originalJson, revisedJson) {
        JsonTreeDiffer(
            defaultDispatcher = Dispatchers.Default,
            mainDispatcher = Dispatchers.Main
        )
    }

    LaunchedEffect(jsonTreeDiffer) {
        jsonTreeDiffer.diff(originalJson, revisedJson)
    }

    when(val state = jsonTreeDiffer.state.collectAsState().value) {
        is JsonTreeDifferState.Loading -> onLoading()
        is JsonTreeDifferState.Ready -> Box(modifier = modifier) {
            SideBySideDiff(
                state = state,
                colors = colors,
                textStyle = textStyle,
            )
        }
        is JsonTreeDifferState.Error.OriginalJsonError -> onError(OriginalJsonError(state.throwable))
        is JsonTreeDifferState.Error.RevisedJsonError -> onError(RevisedJsonError(state.throwable))
    }
}

@Composable
private fun SideBySideDiff(
    state: JsonTreeDifferState.Ready,
    colors: JsonTreeDiffColors,
    textStyle: TextStyle,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
    ) {
        itemsIndexed(state.diffElements) { index, (originalDiffElement, revisedDiffElement) ->
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
                        is JsonDiffElement.Equal -> Color.Transparent
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
                        is JsonDiffElement.Equal -> Color.Transparent
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