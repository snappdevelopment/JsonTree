package com.sebastianneubauer.jsontree

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import io.github.petertrr.diffutils.text.DiffLineNormalizer
import io.github.petertrr.diffutils.text.DiffRow
import io.github.petertrr.diffutils.text.DiffRowGenerator
import io.github.petertrr.diffutils.text.DiffTagGenerator
import jsontree.jsontree.generated.resources.Res
import jsontree.jsontree.generated.resources.jsontree_arrow_right
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable.start
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.vectorResource

@Composable
public fun JsonTreeDiff(
    originalJson: String,
    revisedJson: String,
    onLoading: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    colors: TreeColors = defaultLightColors,
    icon: ImageVector = vectorResource(Res.drawable.jsontree_arrow_right),
    iconSize: Dp = 20.dp,
    textStyle: TextStyle = LocalTextStyle.current,
    showIndices: Boolean = false,
    showItemCount: Boolean = true,
    lazyListState: LazyListState = rememberLazyListState(),
    onError: (Throwable) -> Unit = {}
) {

    val originalJsonParser = remember(originalJson) {
        JsonTreeParser(
            json = originalJson,
            defaultDispatcher = Dispatchers.Default,
            mainDispatcher = Dispatchers.Main
        )
    }

    val revisedJsonParser = remember(revisedJson) {
        JsonTreeParser(
            json = originalJson,
            defaultDispatcher = Dispatchers.Default,
            mainDispatcher = Dispatchers.Main
        )
    }

    LaunchedEffect(originalJsonParser) {
        originalJsonParser.init(TreeState.EXPANDED)
    }

    LaunchedEffect(revisedJsonParser) {
        revisedJsonParser.init(TreeState.EXPANDED)
    }

    val originalJsonParserState = originalJsonParser.state.value
    val revisedJsonParserState = revisedJsonParser.state.value

    when {
        originalJsonParserState is JsonTreeParserState.Ready
                && revisedJsonParserState is JsonTreeParserState.Ready -> {

        }
        originalJsonParserState is JsonTreeParserState.Loading
                || revisedJsonParserState is JsonTreeParserState.Loading -> {
                    onLoading()
                }
        originalJsonParserState is JsonTreeParserState.Parsing.Error
                || revisedJsonParserState is JsonTreeParserState.Parsing.Error -> {
                    val originalError = (originalJsonParserState as? JsonTreeParserState.Parsing.Error)?.throwable
                    val revisedError = (revisedJsonParserState as? JsonTreeParserState.Parsing.Error)?.throwable
                    onError(originalError ?: revisedError!!)
                }
        else -> error("Unexpected states $originalJsonParserState, $revisedJsonParserState")
    }

//    when (val state = revisedJsonParser.state.value) {
//        is JsonTreeParserState.Ready -> {
//            Box(modifier = modifier) {
//                JsonTreeList(
//                    state = state,
//                    contentPadding = contentPadding,
//                    colors = colors,
//                    icon = icon,
//                    iconSize = iconSize,
//                    textStyle = textStyle,
//                    showIndices = showIndices,
//                    showItemCount = showItemCount,
//                    searchResult = SearchState.SearchResult(
//                        query = null,
//                        occurrences = emptyMap(),
//                        selectedOccurrence = null,
//                        totalResults = 0,
//                        selectedResultIndex = null
//                    ),
//                    lazyListState = lazyListState,
//                    onClick = {} //noop
//                )
//
//            }
//        }
//        is JsonTreeParserState.Loading -> onLoading()
//        is JsonTreeParserState.Parsing.Error -> onError(state.throwable)
//        is JsonTreeParserState.Parsing.Parsed -> error("Unexpected state $state")
//    }
}

@Composable
public fun SideBySideDiff() {
    val original = """
        {
            "topLevelObject": {
                "string": "stringValue",
                "nestedObject": {
                    "int": 42,
                    "nestedArray": [
                        "nestedArrayValue",
                        "nestedArrayValue"
                    ],
                    "arrayOfObjects": [
                        {
                            "anotherString": "anotherStringValue"
                        },
                        {
                            "anotherInt": 52
                        }
                    ]
                }
            },
            "topLevelArray": [
                "hello",
                "world"
            ],
            "emptyObject": {
    
            }
        }
    """.trimIndent()

    val revised = """
        {
            "topLevelObject": {
                "string": "stringValue",
                "nestedObject": {
                    "nestedArray": [
                        "nestedArrayValue",
                        "nestedArrayValue"
                    ],
                    "rrayOfa": [
                        {
                            "anotherString": "anotherStringValue"
                        },
                        {
                            "anotherInt": 52
                        },
                        {
                            "anotherFloat": 5.0
                        }
                    ]
                }
            },
            "topLevelArray": [
                "hello",
                "world"
            ],
            "emptyObject": {
    
            }
        }
    """.trimIndent()

    val diffRows = DiffRowGenerator(
        showInlineDiffs = true,
        newTag = object : DiffTagGenerator {
            override fun generateClose(tag: DiffRow.Tag): String {
                return "</b>"//if(tag == DiffRow.Tag.CHANGE) "</b>" else ""
            }

            override fun generateOpen(tag: DiffRow.Tag): String {
                return "<b>"//if(tag == DiffRow.Tag.CHANGE) "<b>" else ""
            }
        },
        oldTag = object : DiffTagGenerator {
            override fun generateClose(tag: DiffRow.Tag): String {
                return "</b>"//if(tag == DiffRow.Tag.CHANGE) "</b>" else ""
            }

            override fun generateOpen(tag: DiffRow.Tag): String {
                return "<b>"//if(tag == DiffRow.Tag.CHANGE) "<b>" else ""
            }
        },
    ).generateDiffRows(original.lines(), revised.lines())

    println(diffRows)

    val originalListState = rememberLazyListState()
    val revisedListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    SyncScrolling(
        originalListState = originalListState,
        revisedListState = revisedListState,
        coroutineScope = coroutineScope
    )

    Row(modifier = Modifier.fillMaxWidth()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = originalListState
        ) {
            items(diffRows) { diffRow ->
                val text = when(diffRow.tag) {
                    DiffRow.Tag.INSERT -> ""
                    DiffRow.Tag.DELETE -> diffRow.oldLine
                    DiffRow.Tag.CHANGE -> diffRow.oldLine
                    DiffRow.Tag.EQUAL -> diffRow.oldLine
                }
                val strippedText = text.replace("<b>", "").replace("</b>", "")
                val annotatedText = buildAnnotatedString {
                    append(strippedText)
                    if(diffRow.tag == DiffRow.Tag.CHANGE) {
                        val indices = text.findBoldTagIndicesStripped()
                        indices.forEach { (start, end) ->
                            addStyle(
                                style = SpanStyle(background = Color.Blue),
                                start = start,
                                end = end
                            )
                        }
                    }
                }

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = when(diffRow.tag) {
                                DiffRow.Tag.INSERT -> Color.Transparent
                                DiffRow.Tag.DELETE -> Color.Red
                                DiffRow.Tag.CHANGE -> Color.Red
                                DiffRow.Tag.EQUAL -> Color.Transparent
                            }
                        ),
                    text = annotatedText
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            state = revisedListState
        ) {
            items(diffRows) { diffRow ->
                val text = when(diffRow.tag) {
                    DiffRow.Tag.INSERT -> diffRow.newLine
                    DiffRow.Tag.DELETE -> ""
                    DiffRow.Tag.CHANGE -> diffRow.newLine
                    DiffRow.Tag.EQUAL -> diffRow.newLine
                }
                val strippedText = text.replace("<b>", "").replace("</b>", "")
                val annotatedText = buildAnnotatedString {
                    append(strippedText)
                    if(diffRow.tag == DiffRow.Tag.CHANGE) {
                        val indices = text.findBoldTagIndicesStripped()
                        indices.forEach { (start, end) ->
                            addStyle(
                                style = SpanStyle(background = Color.Blue),
                                start = start,
                                end = end
                            )
                        }
                    }
                }
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = when(diffRow.tag) {
                                DiffRow.Tag.INSERT -> Color.Green
                                DiffRow.Tag.DELETE -> Color.Transparent
                                DiffRow.Tag.CHANGE -> Color.Green
                                DiffRow.Tag.EQUAL -> Color.Transparent
                            }
                        ),
                    text = annotatedText
                )
            }
        }
    }
}

internal fun String.findBoldTagIndicesStripped(): List<Pair<Int, Int>> {
    val result = mutableListOf<Pair<Int, Int>>()
    var strippedIndex = 0
    var currentIndex = 0

    while (currentIndex < length) {
        val openTagIndex = indexOf("<b>", currentIndex)
        if (openTagIndex == -1) break

        // Add non-bold text length to stripped index
        strippedIndex += (openTagIndex - currentIndex)

        val contentStart = openTagIndex + 3
        val closeTagIndex = indexOf("</b>", contentStart)

        if (closeTagIndex == -1) break

        val contentLength = closeTagIndex - contentStart
        result.add(strippedIndex to (strippedIndex + contentLength))

        strippedIndex += contentLength
        currentIndex = closeTagIndex + 4
    }

    return result
}

@Composable
private fun SyncScrolling(
    originalListState: LazyListState,
    revisedListState: LazyListState,
    coroutineScope: CoroutineScope
) {
    fun syncScroll(leading: LazyListState, following: LazyListState) {
        coroutineScope.launch {
            val scrollPosition = leading.firstVisibleItemScrollOffset
            following.scrollToItem(
                index = leading.firstVisibleItemIndex,
                scrollOffset = scrollPosition
            )
        }
    }

    // Observe scroll changes in the LEFT column
    LaunchedEffect(originalListState.firstVisibleItemIndex, originalListState.firstVisibleItemScrollOffset) {
        syncScroll(originalListState, revisedListState)
    }

    // Observe scroll changes in the RIGHT column
    LaunchedEffect(revisedListState.firstVisibleItemIndex, revisedListState.firstVisibleItemScrollOffset) {
        syncScroll(revisedListState, originalListState)
    }
}
// TODO: Evtl. JsonDiffElement data class erstellen welches das JsonTreeElement und den ChangeType hat. Dann beiden JsonTreeElement Listen darauf mappen um die Infos zusammen zu haben