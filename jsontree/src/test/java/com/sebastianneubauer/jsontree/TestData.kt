package com.sebastianneubauer.jsontree

internal val nestedJson = """
    {
        "array": [
            [
                {
                    "string": "aString"
                }
            ],
            [
                42,
                52
            ]
        ]
    }
""".trimIndent()

internal val rootArrayJson = """
    [
        "stringValue"
    ]
""".trimIndent()

internal val rootStringJson = """
    "stringValue"
""".trimIndent()

internal const val EMPTY_OBJECT_JSON = "{}"

internal const val INVALID_JSON = ""
