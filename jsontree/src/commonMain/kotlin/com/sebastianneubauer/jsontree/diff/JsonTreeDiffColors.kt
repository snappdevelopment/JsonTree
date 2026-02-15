package com.sebastianneubauer.jsontree.diff

import androidx.compose.ui.graphics.Color

/**
 * The color palette for the json diff.
 *
 * @param keyColor The color for json keys.
 * @param stringValueColor The color for json strings.
 * @param numberValueColor The color for json numbers.
 * @param booleanValueColor The color for json booleans.
 * @param nullValueColor The color for json nulls.
 * @param symbolColor The color for all symbols like brackets, colons and commas.
 * @param deletionHighlightColor The color for highlighting deletions inside a line.
 * @param deletionBackgroundColor The background color for lines that contain deletion diffs.
 * @param insertionHighlightColor TThe color for highlighting insertions inside a line.
 * @param insertionBackgroundColor The background color for lines that contain insertion diffs.
 */
public data class JsonTreeDiffColors(
    val keyColor: Color,
    val stringValueColor: Color,
    val numberValueColor: Color,
    val booleanValueColor: Color,
    val nullValueColor: Color,
    val symbolColor: Color,
    val deletionHighlightColor: Color,
    val deletionBackgroundColor: Color,
    val insertionHighlightColor: Color,
    val insertionBackgroundColor: Color,
)

/**
 * The default light palette for the json diff.
 */
public val defaultLightDiffColors: JsonTreeDiffColors = JsonTreeDiffColors(
    keyColor = Color(0xFF1F9E8F),
    stringValueColor = Color(0xFFE9613F),
    numberValueColor = Color(0xFFF7964A),
    booleanValueColor = Color(0xFFE9BB4D),
    nullValueColor = Color(0xFFE9BB4D),
    symbolColor = Color(0xFF1D4555),
    deletionHighlightColor = Color(0xFFFECECA),
    deletionBackgroundColor = Color(0xFFFFEBE9),
    insertionHighlightColor = Color(0xFFACEEBB),
    insertionBackgroundColor = Color(0xFFDBFBE1),
)

/**
 * The default dark palette for the json diff.
 */
public val defaultDarkDiffColors: JsonTreeDiffColors = JsonTreeDiffColors(
    keyColor = Color(0xFF73c8a9),
    stringValueColor = Color(0xFFbd5532),
    numberValueColor = Color(0xFFe1b866),
    booleanValueColor = Color(0xFFdee1b6),
    nullValueColor = Color(0xFFdee1b6),
    symbolColor = Color(0xFF798199),
    deletionHighlightColor = Color(0xFF7C3C3D),
    deletionBackgroundColor = Color(0xFF352D33),
    insertionHighlightColor = Color(0xFF345E3D),
    insertionBackgroundColor = Color(0xFF263834),
)
