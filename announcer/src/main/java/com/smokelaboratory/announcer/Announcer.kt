package com.smokelaboratory.announcer

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect

/**
 * helper class to register, announce and unregister announcements using kotlin flow
 */
class Announcer {

    private val logTag = javaClass.simpleName

    /**
     * channel used for communicate between [announce] scope and app-level scope
     */
    private var announceChannel = Channel<Pair<String, Announcement>>()

    /**
     * map of announcementIds and assigned flows
     */
    private val registeredAnnouncements: LinkedHashMap<String, MutableSharedFlow<Announcement>> =
        linkedMapOf()

    private val appLevelJob: Job

    init {
        /**
         * spins up a coroutine at app level and keeps it alive
         * listens for an announcement request and emits the provided data
         */
        appLevelJob = GlobalScope.launch {
            while (true) {
                announceChannel.receive().let {
                    registeredAnnouncements[it.first]?.emit(it.second)
                }
            }
        }
    }

    /**
     * registers announcements and assigns a flow to each of them
     * @param announcementId : id to uniquely identify an announcement
     * @param overwrite : used to forcefully overwrite an already registered announcement
     * @param replayCount : a count of previous values to emit when any collector is added
     * @param onBufferOverflow : technique to manage buffer overflow
     */
    fun <T : Announcement> registerAnnouncement(
        announcementId: String,
        overwrite: Boolean = false,
        replayCount: Int = 0,
        onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND
    ) {
        if (registeredAnnouncements.containsKey(announcementId)) {
            if (overwrite) {
                registerAnnouncement<T>(announcementId, replayCount, onBufferOverflow)
                Log.w(
                    logTag,
                    "The announcement with ID:${announcementId} is re-registered with a new announcer"
                )
            } else
                Log.e(logTag, "An announcement with ID:${announcementId} is already registered")
        } else
            registerAnnouncement<T>(announcementId, replayCount, onBufferOverflow)
    }

    private fun <T> registerAnnouncement(
        announcementId: String,
        replayCount: Int,
        onBufferOverflow: BufferOverflow
    ) {
        registeredAnnouncements[announcementId] =
            MutableSharedFlow<T>(
                replayCount,
                onBufferOverflow = onBufferOverflow
            ) as MutableSharedFlow<Announcement>
    }

    /**
     * unregisters an announcement using its ID
     * Note : unregistering an announcement will unsubscribe all its listeners forever
     * @param announcementId : id of announcement to unregister
     */
    fun unregisterAnnouncement(announcementId: String) {
        registeredAnnouncements.remove(announcementId)
    }

    /**
     * subscribes a listener to an announcement
     * @param announcementId : id of announcement to listen to
     * @param coroutineScope : scope assigned to the listener. if this scope is canceled, the listener is cancelled automatically
     * @param dispatcher : thread on which the listener will be subscribed
     * @param listener : block to send announced data to the caller
     */
    fun <T> listenAnnouncement(
        announcementId: String,
        coroutineScope: CoroutineScope,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        listener: suspend (T) -> Unit
    ) {
        registeredAnnouncements[announcementId]?.let {
            coroutineScope.launch(dispatcher) {
                it.collect { announcement ->
                    listener.invoke(announcement as T)
                }
            }
        }
    }

    /**
     * announces provided data via an announcer
     * @param announcement : id of announcement to send data to
     * @param announcement : data to announce. it should extend [Announcement]
     */
    suspend fun announce(announcementId: String, announcement: Announcement) {
        announceChannel.send(Pair(announcementId, announcement))
    }

    /**
     * shuts down the entire announcement system
     * to restart the system, a fresh instance of [Announcer] should be created with all new announcements
     */
    fun shutDown() {
        appLevelJob.cancel(CancellationException())
        registeredAnnouncements.clear()
        announceChannel.cancel(CancellationException())
    }
}