package cs446.dbc.views

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cs446.dbc.api.ApiFunctions
import cs446.dbc.components.EventCard
import cs446.dbc.models.EventModel
import cs446.dbc.models.EventType
import cs446.dbc.viewmodels.AppViewModel
import cs446.dbc.viewmodels.EventAction
import cs446.dbc.viewmodels.EventViewModel
import kotlinx.coroutines.runBlocking

@Composable
fun EventScreen(eventViewModel: EventViewModel, appViewModel: AppViewModel, appContext: Context, navController: NavHostController) {

    appViewModel.updateScreenTitle("Events")
    val events by eventViewModel.events.collectAsStateWithLifecycle()
    val composeEvents = remember {
        mutableStateListOf<EventModel>()
    }
    var loadedEvents by remember {
        mutableStateOf(false)
    }

    eventViewModel.changeEventSnapshotList(composeEvents)
    // reset event id to ensure we don't keep a stale state

    LaunchedEffect(key1 = "SetCurrEventViewId") {
        eventViewModel.changeCurrEventViewId("")
    }

//    val loadedEvents by appViewModel.loadedEvents.collectAsStateWithLifecycle()

    // TODO: First load the events
//    LaunchedEffect(key1 = "load_events") {
//        if (!loadedEvents) {
//            // TODO: Convert into loading events from storage
//            //val cardList =
//            //    appViewModel.loadCardsFromDirectory(appContext, "businessCards", CardType.SHARED)
//            //sharedCardViewModel.performAction(BusinessCardAction.InsertCards(cardList))
//        }
//    }

    // TODO: Add the loading events stuff here
//    LaunchedEffect(key1 = "event_examples") {
        if (!loadedEvents) {
            // load events from storage and add them to eventsList
            val eventsList = mutableListOf<EventModel>()

            // TODO: remove example list after the above is working correctly
            for (i in 0..2) {
                eventViewModel.performAction(EventAction.PopulateEvent(
                    name = "Joined E$i",
                    location = "Toronto - $i",
                    eventType = EventType.JOINED
                ))
            }
            for (i in 0..2) {
                eventViewModel.performAction(EventAction.PopulateEvent(
                    name = "Hosted Event that is really long - $i",
                    location = "29 Westfold Ave, Toronto, Ontario, Canada - $i",
                    eventType = EventType.HOSTED,
                ))
            }
            eventViewModel.performAction(EventAction.SortEvents())
            eventsList.addAll(events)

            // Check if event still exists on server
            runBlocking {
                eventsList.forEach {event ->
                    val doesExist = ApiFunctions.checkEventExists(event.id)
                    if (doesExist) {
                        // TODO: populate event
                    }
                    else {
                        // delete event from local storage
                    }

                }
                loadedEvents = true
            }
        }
//    }

    if (composeEvents.isEmpty()) {
        composeEvents.addAll(events)
    }

    LazyColumn (
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        items(events) { event ->
            Box(modifier = Modifier.fillMaxWidth()) {
                EventCard(event, eventViewModel::performAction, onClickAction = {
                    eventViewModel.changeCurrEventViewId(event.id)
                    // TODO: Change this to allow for API <= Ver 32
                    navController.navigate(route = "event-menu/${event.id}")
                })
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}