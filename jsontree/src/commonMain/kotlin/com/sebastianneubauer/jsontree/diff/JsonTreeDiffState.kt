package com.sebastianneubauer.jsontree.diff

/**
 * Describes the current state of the diff calculation.
 */
public sealed interface JsonTreeDiffState {
    /**
     * The diff is currently being calculated.
     */
    public data object Loading: JsonTreeDiffState

    /**
     * The diff calculation succeeded. See [info] for more details on the diff.
     */
    public data class Success(val info: JsonTreeDiffInfo): JsonTreeDiffState

    /**
     * The diff calculation failed with an error. See [error] for details.
     */
    public data class Error(val error: JsonTreeDiffError): JsonTreeDiffState
}

/**
 * Infos about the calculated diff.
 */
public data class JsonTreeDiffInfo(
    val changeInfo: ChangeInfo
)

public sealed interface ChangeInfo {
    /**
     * The given Json strings are identical.
     */
    public data object Identical: ChangeInfo

    /**
     * The given Json strings have differences.
     */
    public data class Changed(
        /**
         * The amount of inserted lines in the revised Json.
         */
        val insertions: Int,
        /**
         * The amount of deleted lines in the original Json.
         */
        val deletions: Int,
    ): ChangeInfo
}

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