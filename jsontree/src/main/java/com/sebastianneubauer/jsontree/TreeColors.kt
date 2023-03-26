package com.sebastianneubauer.jsontree

import androidx.compose.ui.graphics.Color

public data class TreeColors(
    val keyColor: Color,
    val stringValueColor: Color,
    val numberValueColor: Color,
    val booleanValueColor: Color,
    val nullValueColor: Color,
    val symbolColor: Color,
    val iconColor: Color,
)

public val defaultLightColors: TreeColors = TreeColors(
    keyColor = Color(0xFF1F9E8F),
    stringValueColor = Color(0xFFE9613F),
    numberValueColor = Color(0xFFF7964A),
    booleanValueColor = Color(0xFFE9BB4D),
    nullValueColor = Color(0xFFE9BB4D),
    symbolColor = Color(0xFF1D4555),
    iconColor = Color(0xFF1D4555),
)

public val defaultDarkColors: TreeColors = TreeColors(
    keyColor = Color(0xFF73c8a9),
    stringValueColor = Color(0xFFbd5532),
    numberValueColor = Color(0xFFe1b866),
    booleanValueColor = Color(0xFFdee1b6),
    nullValueColor = Color(0xFFdee1b6),
    symbolColor = Color(0xFF798199),
    iconColor = Color(0xFF798199),
)
