package com.sebastianneubauer.jsontree

internal val nestedJson = """
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
