package com.js.nowakelock.data.repository.daDetail

import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.dao.DADao
import com.js.nowakelock.data.db.dao.InfoEventDao
import com.js.nowakelock.data.db.entity.InfoEvent
import com.js.nowakelock.data.db.entity.St
import com.js.nowakelock.data.model.DAItem
import com.js.nowakelock.data.model.EventItem
import com.js.nowakelock.data.model.HourData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Implementation of DADetailRepository that accesses device automation details
 * from the database and provides data transformations for UI consumption.
 */
class DADetailRepositoryImpl(
    private val daDao: DADao, private val infoEventDao: InfoEventDao
) : DADetailRepository {

    companion object {
        private const val TAG = "DADetailRepositoryImpl"
        private const val ONE_HOUR_MS = 60 * 60 * 1000L
    }

    /**
     * Get device automation item data
     * @param name Name of the item
     * @param userId User ID
     * @return Flow of DAItem
     */
    override fun getDAItem(name: String, type: Type, userId: Int): Flow<DAItem> {
        val infoFlow = daDao.loadInfo(name, type, userId)
        val stFlow = daDao.loadSt(name, type, userId)

        return combine(infoFlow, stFlow) { info, st ->
            DAItem.fromEntities(info, st)
        }.distinctUntilChanged().flowOn(Dispatchers.IO)
    }

    /**
     * Get recent events for a device automation item
     * @param name Name of the item
     * @param userId User ID
     * @param limit Maximum number of events to return
     * @return Flow of EventItem list
     */
    override fun getRecentEvents(
        name: String, type: Type, userId: Int, limit: Int
    ): Flow<List<EventItem>> {
        // Get events from the last hour
        val oneHourAgo = System.currentTimeMillis() - ONE_HOUR_MS

        return infoEventDao.getRecentEvents(name, type, userId, oneHourAgo, limit).map { events ->
            events.map { event -> convertToEventItem(event) }
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Get timeline data for a device automation item
     * @param name Name of the item
     * @param userId User ID
     * @param hours Number of hours to include in timeline
     * @return Flow of HourData list
     */
    override fun getTimelineData(
        name: String, type: Type, userId: Int, hours: Int
    ): Flow<List<HourData>> {
        // Get events from the last 24 hours
        val timeStart = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(hours.toLong())

        return infoEventDao.getEventsInTimeRange(name, type, userId, timeStart).map { events ->
            aggregateEventsByHour(events, hours)
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Update settings for a device automation item
     * @param setting The settings to update
     */
    override suspend fun updateDAItemSettings(setting: St) {
        withContext(Dispatchers.IO) {
            daDao.insert(setting)
        }
    }

    /**
     * Convert InfoEvent to EventItem for UI presentation
     */
    private fun convertToEventItem(event: InfoEvent): EventItem {
        val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val duration = (event.endTime ?: System.currentTimeMillis()) - event.startTime

        return EventItem(
            type = event.type,
            time = event.startTime,
            duration = duration,
            isBlocked = event.isBlocked,
            formattedTime = dateFormat.format(event.startTime),
            formattedDuration = formatDuration(duration)
        )
    }

    /**
     * Format duration in milliseconds to a human-readable string
     */
    private fun formatDuration(durationMs: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60

        return if (minutes > 0) {
            "$minutes m $seconds s"
        } else {
            "$seconds s"
        }
    }

    /**
     * Aggregate events by hour for timeline visualization
     */
    private fun aggregateEventsByHour(events: List<InfoEvent>, hours: Int): List<HourData> {
        val hourlyData = mutableMapOf<Int, HourData>()
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        // Initialize data for all hours with zero counts
        for (i in 0 until hours) {
            val hour = (currentHour - i + 24) % 24
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            hourlyData[hour] = HourData(
                hour = hour, 
                label = formatHourLabel(hour), 
                total = 0, 
                blocked = 0
            )
        }

        // Process events
        events.forEach { event ->
            calendar.timeInMillis = event.startTime
            val eventHour = calendar.get(Calendar.HOUR_OF_DAY)

            // Only process events that fall within our hourly data
            hourlyData[eventHour]?.let { hourData ->
                hourlyData[eventHour] = hourData.copy(
                    total = hourData.total + 1,
                    blocked = hourData.blocked + if (event.isBlocked) 1 else 0
                )
            }
        }

        // Convert to ordered list - chronological order (past to present)
        return (0 until hours).map { i ->
            // Start from oldest time (current hour - (hours-1)) and move forward
            val hour = (currentHour - (hours - 1) + i + 24) % 24
            hourlyData[hour] ?: HourData(hour = hour, label = formatHourLabel(hour), total = 0, blocked = 0)
        }
    }

    /**
     * Format hour to AM/PM label
     */
    private fun formatHourLabel(hour: Int): String {
        return when (hour) {
            0 -> "12AM"
            12 -> "12PM"
            in 1..11 -> "${hour}AM"
            else -> "${hour - 12}PM"
        }
    }
}