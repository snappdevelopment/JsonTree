package com.sebastianneubauer.jsontree.diff

/**
 * The diff calculation succeeded. See [info] for more details on the diff.
 */
public data class Success(val info: JsonTreeDiffInfo)

/**
 * Infos about the calculated diff.
 */
public data class JsonTreeDiffInfo(
    val changeInfo: ChangeInfo
)

public data class ChangeInfo(
    /**
     * The amount of inserted lines in the revised Json.
     */
    val insertions: Int,
    /**
     * The amount of deleted lines in the original Json.
     */
    val deletions: Int,
)

/**
 * The diff calculation failed with an error. See [error] for details.
 */
public data class Error(val error: JsonTreeDiffError)

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