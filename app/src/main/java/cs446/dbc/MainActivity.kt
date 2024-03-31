package cs446.dbc

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.compose.AppTheme
import cs446.dbc.components.AddEventDialog
import cs446.dbc.components.JoinEventDialog
import cs446.dbc.components.ReceiveDialog
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.models.CardType
import cs446.dbc.models.EventModel
import cs446.dbc.models.EventType
import cs446.dbc.models.Field
import cs446.dbc.models.FieldType
import cs446.dbc.models.TemplateType
import cs446.dbc.viewmodels.AppViewModel
import cs446.dbc.viewmodels.BusinessCardAction
import cs446.dbc.viewmodels.BusinessCardViewModel
import cs446.dbc.viewmodels.CreateEditViewModel
import cs446.dbc.viewmodels.EventAction
import cs446.dbc.viewmodels.EventViewModel
import cs446.dbc.views.CreateEventScreen
import cs446.dbc.views.EventScreen
import cs446.dbc.views.SharedCardsScreen
import cs446.dbc.views.UserCardsScreen
import cs446.dbc.views.EventMenuScreen
import java.util.UUID
import kotlin.math.log

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(appActivity = this)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(ExperimentalAnimationApi::class)
//    @OptIn(ExperimentalLifeCycleComposeApi::class)
    @Composable
    private fun App(appActivity: AppCompatActivity) {
        val appViewModel: AppViewModel = viewModel() {
            AppViewModel(savedStateHandle = createSavedStateHandle(), CardType.SHARED)
        }
        val cardViewModel: BusinessCardViewModel = viewModel() {
            BusinessCardViewModel(savedStateHandle = createSavedStateHandle(), CardType.SHARED)
        }
        val eventViewModel: EventViewModel = viewModel() {
            EventViewModel(savedStateHandle = createSavedStateHandle())
        }
        val createEditViewModel: CreateEditViewModel = viewModel() {
            CreateEditViewModel(savedStateHandle = createSavedStateHandle())
        }

        val appContext = LocalContext.current
        val navController = rememberNavController()
        val loadedSharedCards by appViewModel.loadedSharedCards.collectAsStateWithLifecycle()
        val loadedMyCards by appViewModel.loadedMyCards.collectAsStateWithLifecycle()
        val currEventViewId by eventViewModel.currEventViewId.collectAsStateWithLifecycle()

        LaunchedEffect(key1 = "load_cards") {
            if (!loadedSharedCards) {
                val cardList =
                    appViewModel.loadCardsFromDirectory(
                        appContext,
                        "businessCards",
                        CardType.SHARED
                    )
                cardViewModel.performAction(BusinessCardAction.InsertCards(cardList))
            }
        }

        val homeUiState by appViewModel.uiState.collectAsStateWithLifecycle()
        val snackBarHostState = remember { SnackbarHostState() }

        // TODO: remove after demo, we'll use this to start in the SharedCards Screen
        val sharedCardsList = listOf(
            BusinessCardModel(
                id = UUID.randomUUID().toString(),
                front = "A",
                back = "B",
                favorite = false,
                fields = mutableListOf(),
                cardType = CardType.SHARED
            ),
            BusinessCardModel(
                id = UUID.randomUUID().toString(),
                front = "C",
                back = "D",
                favorite = true,
                fields = mutableListOf(),
                cardType = CardType.SHARED
            ),
            BusinessCardModel(
                id = UUID.randomUUID().toString(),
                front = "E",
                back = "F",
                favorite = false,
                fields = mutableListOf(
                    Field(
                        "Full Name",
                        "Hanz Zimmer",
                        FieldType.TEXT
                    )
                ),
                cardType = CardType.SHARED
            ),
            BusinessCardModel(
                id = UUID.randomUUID().toString(),
                front = "G",
                back = "H",
                favorite = false,
                fields = mutableListOf(
                    Field(
                        "Phone Number",
                        "416-111-2222",
                        FieldType.PHONE_NUMBER
                    )
                ),
                cardType = CardType.SHARED
            ),
            BusinessCardModel(
                id = UUID.randomUUID().toString(),
                front = "I",
                back = "J",
                favorite = false,
                template = TemplateType.TEMPLATE_1,
                fields = mutableListOf(
                    Field(
                        "Full Name",
                        "John Doe",
                        FieldType.TEXT,
                    )
                ),
                cardType = CardType.SHARED
            ),
        )

        sharedCardsList.forEach { card ->
            cardViewModel.performAction(
                BusinessCardAction.PopulateCard(
                    front = card.front,
                    back = card.back,
                    favorite = card.favorite,
                    fields = card.fields,
                    cardType = card.cardType
                )
            )
        }

        appViewModel.updateScreenTitle("Saved Cards")

        AppTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
            ) {
                Scaffold(
                    snackbarHost = {
                        SnackbarHost(hostState = snackBarHostState)
                    },
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                AnimatedContent(
                                    targetState = homeUiState.screenTitle,
                                    label = "TopBarTitle",
                                    transitionSpec = {
                                        fadeIn() togetherWith fadeOut()
                                    }
                                ) {
                                    Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = { navController.navigate(Screen.Settings.route) },
                                ) {
                                    Icon(Icons.Outlined.Settings, "Settings")
                                }
                            }
                        )
                    },
                    bottomBar = {
                        BottomAppBar(
                            navController,
                            appViewModel,
                            cardViewModel,
                            eventViewModel,
                            createEditViewModel,
                            snackBarHostState,
                            appContext
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                    ) {
                        NavHost(
                            navController,
                            startDestination = Screen.Home.route,
                            enterTransition = {
                                slideInHorizontally {
                                    if (
                                        order(initialState.destination.route!!)
                                        < order(targetState.destination.route!!)
                                    ) it
                                    else -it
                                }
                            },
                            exitTransition = { slideOutHorizontally {
                                if (
                                    order(initialState.destination.route!!)
                                    < order(targetState.destination.route!!)
                                ) -it
                                else it
                            } },
                        ) {
                            composable(Screen.UserCards.route) {
                                cardViewModel.performAction(
                                    BusinessCardAction.UpdateCardContext(
                                        CardType.PERSONAL
                                    )
                                )
                                // TODO: Remove the example list after
                                UserCardsScreen(
                                    appViewModel, cardViewModel, listOf(
                                        BusinessCardModel(
                                            id = UUID.randomUUID().toString(),
                                            front = "A",
                                            back = "B",
                                            favorite = false,
                                            fields = mutableListOf(
                                                Field(
                                                    "Full Name",
                                                    "John Doe",
                                                    FieldType.TEXT,
                                                ),
                                                Field(
                                                    "Email",
                                                    "john@example.com",
                                                    FieldType.TEXT,
                                                ),
                                                Field(
                                                    "Organization",
                                                    "Test Org",
                                                    FieldType.TEXT
                                                )
                                            ),
                                            cardType = CardType.PERSONAL,
                                            template = TemplateType.TEMPLATE_1
                                        ),
                                        BusinessCardModel(
                                            id = UUID.randomUUID().toString(),
                                            front = "C",
                                            back = "D",
                                            favorite = true,
                                            fields = mutableListOf(
                                                Field(
                                                    "Full Name",
                                                    "Mary Doe",
                                                    FieldType.TEXT,
                                                ),
                                                Field(
                                                    "Email",
                                                    "mary@example.com",
                                                    FieldType.TEXT,
                                                ),
                                                Field(
                                                    "Organization",
                                                    "Test Org 2",
                                                    FieldType.TEXT
                                                )
                                            ),
                                            cardType = CardType.PERSONAL
                                        ),
                                    ), appContext
                                )
                            }
                            composable(Screen.Home.route) {
                                cardViewModel.performAction(
                                    BusinessCardAction.UpdateCardContext(
                                        CardType.SHARED
                                    )
                                )
                                SharedCardsScreen(
                                    appViewModel,
                                    cardViewModel,
                                    sharedCardsList,
                                    appContext
                                )
                            }
                            composable(Screen.Settings.route) {
                                appViewModel.updateScreenTitle("Settings") // TODO: Replace with SettingsScreen
                                // TODO: Remove after, for now just creating a new composable so we
                                //  don't get the home page showing up
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                ) {

                                }
                            }
                            composable(Screen.Events.route
                            ) {
                                EventScreen(eventViewModel, appViewModel, appContext, navController)
                            }
                            composable(Screen.EventMenu.route,
                                arguments = listOf(navArgument("eventId") {}))
                            {
                                val eventId = it.arguments?.getString("eventId")!!
                                EventMenuScreen(eventViewModel, appViewModel, appContext, navController, eventId)
                            }
                            composable(Screen.EventCreationMenu.route) {
                                CreateEventScreen(createEditViewModel, eventViewModel, appViewModel, cardViewModel, navController, currEventViewId)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun BottomAppBar(
        navController: NavHostController,
        appViewModel: AppViewModel,
        cardViewModel: BusinessCardViewModel,
        eventViewModel: EventViewModel,
        createEditViewModel: CreateEditViewModel,
        snackBarHostState: SnackbarHostState,
        context: Context
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currEventViewId by eventViewModel.currEventViewId.collectAsStateWithLifecycle()
        val events by eventViewModel.events.collectAsStateWithLifecycle()
        val myCards by cardViewModel.myBusinessCards.collectAsStateWithLifecycle()
        var showReceiveDialog by rememberSaveable {
            mutableStateOf(false)
        }
        val createEditEvent by createEditViewModel.createEditEvent.collectAsStateWithLifecycle()
        val eventBusinessCardList by createEditViewModel.eventBusinessCardList.collectAsStateWithLifecycle()
        var showSaveEventErrorDialog by rememberSaveable {
            mutableStateOf(false)
        }

        var showAddEventsDialog by rememberSaveable {
            mutableStateOf(false)
        }
        var showJoinEventDialog by rememberSaveable {
            mutableStateOf(false)
        }

        @Composable
        fun NavButton(screen: Screen, icon: ImageVector, description: String) {
            val isCurrentRoute = navBackStackEntry?.destination?.route == screen.route
            IconToggleButton(
                checked = isCurrentRoute,
                onCheckedChange = { if (!isCurrentRoute) navController.navigate(screen.route) },
            ) {
                Icon(icon, description)
            }
        }

        if (showReceiveDialog) {
            ReceiveDialog(snackBarHostState, sharedCardViewModel = cardViewModel) {
                showReceiveDialog = false
            }
        }

        if (showSaveEventErrorDialog) {
            AlertDialog(
                onDismissRequest = { showSaveEventErrorDialog = false },
                dismissButton = {
                    TextButton(onClick = { showSaveEventErrorDialog = false }) {
                        Text(text = "Dismiss")
                    }
                },
                confirmButton = {  },
                title = { Text(text = "Error", textAlign = TextAlign.Center) },
                text = { 
                    Text(
                        textAlign = TextAlign.Center,
                        text = "One or more of your entries are incorrect.\nEnsure that you have " +
                                "filled in the name and location of the event, and that the start " +
                                "date is before the end date.")
                }
            )
        }

        if (showAddEventsDialog) {
            AddEventDialog({ showAddEventsDialog = false }){eventType ->
                showAddEventsDialog = false
                when (eventType) {
                    "Host" -> {
                        // set to blank
                        createEditEvent.id = ""
                        navController.navigate(route = "create-event")
                    }
                    "Join" -> {
                        // TODO: If user has no cards, we just join them directly to the event
                        //  otherwise we have them pick out their cards and then join
                        if (myCards.isNotEmpty()) {
                            showJoinEventDialog = true
                        }
                        else {
                            // Join event directly
                        }
                        // TODO: test interaction with join event dialog and host event one after the other
                    }
                }
            }
        }

        if (showJoinEventDialog) {
            JoinEventDialog(cardViewModel = cardViewModel,
                createEditViewModel = createEditViewModel,
                onDismiss = { showJoinEventDialog = false }) {
                    // TODO: Do the event joining stuff here
                    //  with the server and all
                    // TODO: Request event from server, join user event, update server, add event
                    //   to list
                // make a function so we can use it here and in the above join
            }
        }

        androidx.compose.material3.BottomAppBar(
            modifier = Modifier.fillMaxWidth(),
            actions = {
                NavButton(Screen.UserCards, Icons.Outlined.Person, "My Cards")
                NavButton(Screen.Home, Icons.Outlined.People, "Saved Cards")
                NavButton(Screen.Events, Icons.Outlined.Event, "Events")
            },
            // TODO: Make sure to add parameters that we don't directly make (e.g. CardType)
            floatingActionButton = {
                AnimatedVisibility(
                    visible = navBackStackEntry?.destination?.route == Screen.UserCards.route,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                ) {
                    FloatingActionButton(
                        modifier = Modifier,
                        onClick = { /*TODO: Go to business card creation screen*/
                            val newCard = BusinessCardModel(
                                id = UUID.randomUUID().toString(),
                                front = "New Front",
                                back = "New Back",
                                favorite = false,
                                fields = mutableListOf(),
                                cardType = CardType.PERSONAL,
                            )

                            appViewModel.addCard(
                                newCard,
                                context,
                                "businessCards",
                                CardType.PERSONAL
                            )
                            cardViewModel.performAction(BusinessCardAction.InsertCard(newCard))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Add Cards"
                        )
                    }
                }
                AnimatedVisibility(
                    visible = navBackStackEntry?.destination?.route == Screen.Home.route,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                ) {
                    FloatingActionButton(
                        modifier = Modifier,
                        onClick = {
                            showReceiveDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = "Receive Card"
                        )
                    }
                }
                AnimatedVisibility(
                    visible = navBackStackEntry?.destination?.route == Screen.Events.route,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                ) {
                    FloatingActionButton(
                        modifier = Modifier,
                        onClick = {
                            // TODO: Show dialog for what to do, host event or join event
                            //  host event will go to create event screen
                            showAddEventsDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Add Event"
                        )
                    }
                }
                navBackStackEntry?.destination?.route?.let {
                    AnimatedVisibility(
                        visible = it.contains(Screen.EventMenu.route) &&
                            events.find { event -> event.id == currEventViewId }?.eventType == EventType.HOSTED,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut(),
                    ) {
                        FloatingActionButton(
                            modifier = Modifier,
                            onClick = {
                                navController.navigate(route = "create-event")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Create,
                                contentDescription = "Edit Event"
                            )
                        }
                    }
                }

                navBackStackEntry?.destination?.route?.let {
                    AnimatedVisibility(
                        visible = it.contains(Screen.EventCreationMenu.route),
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut(),
                    ) {
                        FloatingActionButton(
                            modifier = Modifier,
                            onClick = {
                                // TODO: button should either create a new hosted event or update the existing one
                                //  Get eventId from server and add it here
                                // TODO: Also need to make sure if the id is blank, we generate a new id
                                // TODO: we also need to update max users set if it was set
                                // TODO: we also need to set event type if it's empty string
                                // TODO: convert this to UTC again, so we have consistent time
                                val didSave = saveEvent(myCards, createEditEvent, eventViewModel, eventBusinessCardList, navController)
                                if (!didSave) {
                                    showSaveEventErrorDialog = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Save,
                                contentDescription = "Save Event"
                            )
                        }
                    }
                }
            }
        )
    }

    private fun order(route: String): Int {
        return when (route) {
            Screen.Settings.route -> 0
            Screen.UserCards.route -> 1
            Screen.Home.route -> 2
            Screen.Events.route -> 3
            else -> Int.MAX_VALUE
        }
    }

    sealed class Screen(val route: String) {
        object UserCards : Screen("my-cards")
        object Home : Screen("saved-cards")
        object Settings : Screen("settings")
        object Events : Screen("events")
        object EventMenu : Screen("event-menu/{eventId}")
        object EventCreationMenu : Screen("create-event")

    }

    private fun saveEvent(myCards: MutableList<BusinessCardModel>,
                          eventModel: EventModel,
                          eventViewModel: EventViewModel,
                          selectedCards: MutableList<BusinessCardModel>,
                          navController: NavHostController): Boolean
    {
        val event = EventModel(
            eventModel.id,
            eventModel.name,
            eventModel.location,
            eventModel.startDate,
            eventModel.endDate,
            eventModel.numUsers,
            eventModel.maxUsers,
            eventModel.maxUsersSet,
            eventModel.eventType
        )
        // if no id, we are creating a new event
        // NOTE: RIGHT NOW SINCE WE DON'T HAVE THESE EVENTS ON THE SERVER, THEY DON'T HAVE
        // IDs SO THEY'LL ALWAYS CREATE A NEW EVENT (EVEN IF WE ARE EDITING THEM RN)
        // IT STILL ALL WORKS
        if (event.id == "") {
            // TODO: Error check to ensure they have added a name and location
            // return if we succeed in saving the event
            if (event.name == "") return false
            if (event.location == "") return false
            // check if start date is less than end date
            if (event.startDate.toLong() > event.endDate.toLong()) return false

            // TODO: ensure they actually have business cards first
            //  otherwise if they haven't made any, it's fine not to upload any
            if (myCards.isNotEmpty() && selectedCards.isEmpty()) return false

            // TODO: Create event on server, join user into event, add user's cards to event
            // TODO: remove this id generation here, only temporary for local testing purposes
            //  until we add the server code

            event.id = UUID.randomUUID().toString()
            event.eventType = EventType.HOSTED
            eventViewModel.performAction(EventAction.InsertEvent(
                event = event
            ))
            navController.navigate(Screen.Events.route)
            eventViewModel.changeCurrEventViewId("")
            return true
        }
        // otherwise we are editing an event
        else {
            // TODO: send the updated event to server
            eventViewModel.performAction(EventAction.UpdateEvent(
                event.id, event
            ))
            navController.navigate(Screen.Events.route)
            eventViewModel.changeCurrEventViewId("")
            return true
        }
    }
}