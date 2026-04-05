package com.sebastianneubauer.jsontree.diff

/**
 * The diff calculation succeeded. See [info] for more details on the diff.
 */
public data class JsonTreeDiffSuccess(val info: JsonTreeDiffInfo)

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
public data class JsonTreeDiffError(val error: DiffError)

public sealed interface DiffError {
    public val throwable: Throwable
    /**
     * Describes an error during parsing of the original Json.
     */
    public class OriginalJsonError(override val throwable: Throwable): DiffError

    /**
     * Describes an error during parsing of the revised Json.
     */
    public class RevisedJsonError(override val throwable: Throwable): DiffError
}