package com.sebastianneubauer.jsontree

import kotlinx.serialization.json.JsonElement

internal sealed interface JsonTreeParserState {
    object Loading : JsonTreeParserState
    data class Ready(val list: List<JsonTreeElement>) : JsonTreeParserState

    sealed interface Parsing : JsonTreeParserState {
        data class Parsed(val jsonElement: JsonElement) : Parsing
        data class Error(val throwable: Throwable) : Parsing
    }
}
