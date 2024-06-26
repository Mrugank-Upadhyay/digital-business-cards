package cs446.dbc.viewmodels

import cs446.dbc.models.EventModel
import cs446.dbc.models.EventType

sealed class EventAction {
    data class PopulateEvent (val name: String, val location: String, val eventType: EventType, val maxUsers: Int = 1000): EventAction()
    data class InsertEvent (val event: EventModel): EventAction()
    data class InsertEvents (val events: MutableList<EventModel>): EventAction()
    data class RemoveEvent (val event: EventModel): EventAction()
    data class UpdateEvent (val currEventId: String, val updatedEvent: EventModel): EventAction()
    data class SortEvents (val compareBy: Comparator<EventModel> = compareBy<EventModel> { it.eventType }): EventAction()
}