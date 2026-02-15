package com.sebastianneubauer.jsontree.diff

public interface JsonTreeDiffError {
    public val throwable: Throwable
    /**
     * Describes an error during parsing of the original Json.
     */
    public class OriginalJsonError(override val throwable: Throwable): JsonTreeDiffError

    /**
     * Describes an error during parsing of the revised Json.
     */
    public class RevisedJsonError(override val throwable: Throwable): JsonTreeDiffError
}