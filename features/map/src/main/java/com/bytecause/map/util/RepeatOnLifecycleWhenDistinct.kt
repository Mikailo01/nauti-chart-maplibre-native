package com.bytecause.map.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume


/**
 * Same as [LifecycleOwner.repeatOnLifecycle], but with additional flow parameter.
 *
 * **Warning:** Use only on flow which produces lightweight objects, because current state is being cached
 *   for further comparisons, which can have negative impact on performance.
 *
 * ```
 *   class MyActivity : AppCompatActivity() {
 *       override fun onCreate(savedInstanceState: Bundle?) {
 *           /* ... */
 *           // Runs the block of code in a coroutine, with each distinct emission from the passed flow
 *           // as a receiver, when the lifecycle is at least STARTED.
 *           // The coroutine will be cancelled when the ON_STOP event happens and will
 *           // restart executing if the lifecycle receives the ON_START event again.
 *           lifecycleScope.launch {
 *               repeatOnLifecycleWhenDistinct(Lifecycle.State.STARTED, state) { newState ->
 *                      updateUi(newState)
 *              }
 *          }
 *       }
 *   }
 *   ```
 *
 *  @see Lifecycle.repeatOnLifecycleWhenDistinct
 */
suspend fun <T> LifecycleOwner.repeatOnLifecycleWhenDistinct(
    state: Lifecycle.State,
    flow: Flow<T>,
    block: suspend CoroutineScope.(T) -> Unit
): Unit = lifecycle.repeatOnLifecycleWhenDistinct(state, flow, block)

/**
 * Same as [Lifecycle.repeatOnLifecycle], but executes the [block] only when a distinct value is emitted
 * from the provided [flow].
 * This prevents unnecessary invocations of [block] if the emitted state is unchanged,
 * keeping the UI or other processes up-to-date without redundant operations.
 *
 * This function is particularly useful in scenarios where lifecycle events (like turning the screen off and on)
 * could cause re-collection of the flow, but you want to avoid re-processing when the current state hasn’t changed.
 *
 * **Warning:** Use only on flow which produces lightweight objects, because current state is being cached
 *   for further comparisons, which can have negative impact on performance.
 *
 * ```
 *   class MyActivity : AppCompatActivity() {
 *       override fun onCreate(savedInstanceState: Bundle?) {
 *           /* ... */
 *           // Runs the block of code in a coroutine, with each distinct emission from the passed flow
 *           // as a receiver, when the lifecycle is at least STARTED.
 *           // The coroutine will be cancelled when the ON_STOP event happens and will
 *           // restart executing if the lifecycle receives the ON_START event again.
 *           lifecycleScope.launch {
 *               repeatOnLifecycleWhenDistinct(Lifecycle.State.STARTED, state) { newState ->
 *                      updateUi(newState)
 *              }
 *          }
 *       }
 *   }
 *   ```
 *
 *
 * @param block The block to run when the lifecycle is at least in [state] state.
 * @param flow The [Flow] providing distinct values to be processed by [block].
 */
suspend fun <T> Lifecycle.repeatOnLifecycleWhenDistinct(
    state: Lifecycle.State,
    flow: Flow<T>,
    block: suspend CoroutineScope.(T) -> Unit
) {
    require(state !== Lifecycle.State.INITIALIZED) {
        "repeatOnLifecycleWhenDistinct cannot start work with the INITIALIZED lifecycle state."
    }

    if (currentState === Lifecycle.State.DESTROYED) return

    // This scope is required to preserve context before we move to Dispatchers.Main
    coroutineScope {
        withContext(Dispatchers.Main.immediate) {
            // Check the current state of the lifecycle as the previous check is not guaranteed
            // to be done on the main thread.
            if (currentState === Lifecycle.State.DESTROYED) return@withContext

            // Instance of the running repeating coroutine
            var launchedJob: Job? = null

            // Current state will be saved into this variable, so the logic can decide, if new value
            // produced by passed flow as argument is distinct or not.
            var currentState: T? = null

            // Registered observer
            var observer: LifecycleEventObserver? = null
            try {
                // Suspend the coroutine until the lifecycle is destroyed or
                // the coroutine is cancelled
                suspendCancellableCoroutine<Unit> { cont ->
                    // Lifecycle observers that executes `block` when the lifecycle reaches certain state, and
                    // cancels when it falls below that state.
                    val startWorkEvent = Lifecycle.Event.upTo(state)
                    val cancelWorkEvent = Lifecycle.Event.downFrom(state)
                    val mutex = Mutex()
                    observer = LifecycleEventObserver { _, event ->
                        if (event == startWorkEvent) {
                            // Launch the repeating work preserving the calling context
                            launchedJob = this@coroutineScope.launch {
                                // Mutex makes invocations run serially,
                                // coroutineScope ensures all child coroutines finish
                                mutex.withLock {
                                    coroutineScope {
                                        flow.collect {
                                            if (currentState == it) return@collect

                                            // Save current state
                                            currentState = it
                                            block(it)
                                        }
                                    }
                                }
                            }
                            return@LifecycleEventObserver
                        }
                        if (event == cancelWorkEvent) {
                            launchedJob?.cancel()
                            launchedJob = null
                        }
                        if (event == Lifecycle.Event.ON_DESTROY) {
                            cont.resume(Unit)
                        }
                    }
                    this@repeatOnLifecycleWhenDistinct.addObserver(observer as LifecycleEventObserver)
                }
            } finally {
                launchedJob?.cancel()
                observer?.let {
                    this@repeatOnLifecycleWhenDistinct.removeObserver(it)
                }
            }
        }
    }
}
