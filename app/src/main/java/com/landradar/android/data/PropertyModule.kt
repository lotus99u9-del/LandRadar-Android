package com.landradar.android.data

import java.util.concurrent.CancellationException

/**
 * Stable boundary for optional Property Intelligence modules.
 * A failed government API must return UNAVAILABLE instead of crashing LED Core.
 */
enum class ModuleStatus { SUCCESS, PARTIAL, UNAVAILABLE }

data class ModuleResult<T>(
    val status: ModuleStatus,
    val data: T? = null,
    val source: String,
    val checkedAt: String? = null,
    val message: String? = null
)

/** Calls an external module at most twice, as required by the project handoff. */
suspend fun <T> runOptionalModule(
    source: String,
    request: suspend () -> T
): ModuleResult<T> {
    var lastFailure: Throwable? = null
    repeat(2) {
        try {
            return ModuleResult(
                status = ModuleStatus.SUCCESS,
                data = request(),
                source = source
            )
        } catch (failure: Exception) {
            if (failure is CancellationException) throw failure
            lastFailure = failure
        }
    }
    return ModuleResult(
        status = ModuleStatus.UNAVAILABLE,
        source = source,
        message = lastFailure?.message ?: "Module unavailable"
    )
}
