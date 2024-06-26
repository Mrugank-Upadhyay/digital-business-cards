package cs446.dbc.api

import android.content.Context
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FileDataPart
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import cs446.dbc.models.BusinessCardModel
import cs446.dbc.models.CardType
import cs446.dbc.models.EventModel
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File


@Serializable
data class EventExists(
    val exists: Boolean
)

object ApiFunctions {
    private const val serverUrl: String = "https://digital-business-cards.fly.dev/api"
    private const val apiKey: String = "saj7eichiegei4fa9tae0phai6lie8Oach7aeTez9ohh5dagie4pi1eez4Booyee"
    private const val apiKeyParam: String = "key=$apiKey"
    private val format = Json { encodeDefaults = true }

    fun createUserId(): String {
        return runBlocking {
            val (_, _, result) = Fuel.post("$serverUrl/user?$apiKeyParam").awaitStringResponseResult()
            return@runBlocking result.get()
        }
    }

    fun createEvent(event: EventModel): String {
        return runBlocking {
            val body = format.encodeToString<EventModel>(event)
            Log.d("body", body)
            val (req, response, result) = Fuel.post("$serverUrl/event?$apiKeyParam").body(body).awaitStringResponseResult()
            Log.e("CREATE_EVENT_REQUEST", req.toString())
            Log.e("CREATE_EVENT_RESPONSE", response.toString())
            Log.e("CREATE_EVENT_RESULT", result.toString())
            // returns event id
            return@runBlocking format.decodeFromString<EventModel>(result.get()).id
        }
    }

    fun editEvent(event: EventModel): Boolean {
        return runBlocking {
            val eventId = event.id
            val body = format.encodeToString(event)
            val (_, response, _) = Fuel.put("$serverUrl/event/$eventId?$apiKeyParam").body(body).awaitStringResponseResult()
            // returns event id
            return@runBlocking response.statusCode == 200
        }
    }

    fun getEvent(eventId: String): EventModel {
        return runBlocking {
            val (req, response, result) = Fuel.get("$serverUrl/event/$eventId?$apiKeyParam")
                .awaitStringResponseResult()
            // returns event
            return@runBlocking format.decodeFromString<EventModel>(result.get())
        }
    }

    fun joinEvent(eventId: String, userId: String): EventModel {
        return runBlocking {

            val (req, response, result) = Fuel.post("$serverUrl/event/$eventId/user/$userId?$apiKeyParam")
                .awaitStringResponseResult()
            Log.e("JOIN_EVENT_REQUEST", req.toString())
            Log.e("JOIN_EVENT_RESPONSE", response.toString())
            Log.e("JOIN_EVENT_RESULT", result.toString())
            // returns event
            return@runBlocking format.decodeFromString<EventModel>(result.get())
        }
    }

    fun exitEvent(eventId: String, userId: String) {
        return runBlocking {
            val (_, _, _) = Fuel.delete("$serverUrl/event/$eventId/user/$userId?$apiKeyParam")
                .awaitStringResponseResult()
            // returns event
        }
    }


    fun checkEventExists(eventId: String): Boolean {
        return runBlocking {
            val (_, _, result) = Fuel.get("$serverUrl/event/$eventId/exists?$apiKeyParam").awaitStringResponseResult()
            val data = format.decodeFromString<EventExists>(result.get())
            return@runBlocking data.exists
        }
    }

    fun addEventCard(card: BusinessCardModel, eventId: String): String {
        return runBlocking {
            val body = format.encodeToString(card)
            val (req, response, result) = Fuel.post("$serverUrl/event/$eventId/card?$apiKeyParam").body(body).awaitStringResponseResult()
            Log.e("ADD_EVENT_CARD_REQUEST", req.toString())
            Log.e("ADD_EVENT_CARD_RESPONSE", response.toString())
            Log.e("ADD_EVENT_CARD_RESULT", result.toString())
            return@runBlocking result.get()
        }
    }

    fun getAllEventCards(eventId: String): MutableList<BusinessCardModel> {
        return runBlocking {
            val (_, _, result) = Fuel.get("$serverUrl/event/$eventId/card?$apiKeyParam").awaitStringResponseResult()
            val cardList = format.decodeFromString<MutableList<BusinessCardModel>>(result.get())
            cardList.forEach { card -> card.cardType = CardType.EVENT_VIEW }
            return@runBlocking cardList
        }
    }

    fun deleteEvent(eventId: String): Boolean {
        return runBlocking {
            val (_, response, _) = Fuel.delete("$serverUrl/event/$eventId?$apiKeyParam").awaitStringResponseResult()
            return@runBlocking response.statusCode == 200
        }
    }

    fun addUserCard(card: BusinessCardModel, userId: String): String {
        return runBlocking {
            val body = format.encodeToString(card)
            val (_, _, result) = Fuel.post("$serverUrl/user/$userId/card?$apiKeyParam").body(body).awaitStringResponseResult()
            return@runBlocking result.get()
        }
    }

    fun getUserCard(cardId: String, userId: String): BusinessCardModel {
        return runBlocking {
            val (_, _, result) = Fuel.get("$serverUrl/user/$userId/card/$cardId?$apiKeyParam").awaitStringResponseResult()
            Log.d("getUserCard", result.get())
            return@runBlocking format.decodeFromString<BusinessCardModel>(result.get())
        }
    }

    // TODO: Remember to update the image path to this value for the card
    // TODO: ensure saveUserCard is called first before this function is called
    fun uploadImage(imagePath: String, cardSide: String, userId: String, cardId: String, context: Context): String {
        return runBlocking {
            val directory = context.getExternalFilesDir(null)!!
            Log.e("UP_IMG_PATH", imagePath)
            Log.e("UP_IMG_ABS_PATH", "${directory.absolutePath}/$imagePath")
            val file = FileDataPart.from("${directory.absolutePath}/$imagePath", name = "image")
            val (req, response, result) = Fuel.upload("$serverUrl/user/$userId/card/$cardId/image/$cardSide?$apiKeyParam")
                .add(file)
                .awaitStringResponseResult()
            Log.e("UP_IMG_REQUEST", req.toString())
            Log.e("UP_IMG_RESPONSE", response.toString())
            Log.e("UP_IMG_RESULT", result.toString())
            return@runBlocking result.get()
        }
    }

    fun downloadImage(imagePath: String, context: Context) {
        // Assume imagePath will be the server path string
        return runBlocking {
            val path = imagePath.replace("_","/")
            val directory = context.getExternalFilesDir(null)!!
            val file =  File("${directory.absolutePath}/$imagePath")
            Fuel.download("$serverUrl/$path?$apiKeyParam").fileDestination { _, _ ->
                file
            }.response { req, res, result ->
                val (data, error) = result
                if (error != null) {
                    Log.e("fetchProfileImage", "error: $error")
                } else {
                    result.fold({ bytes ->
                        Log.e("fetchProfileImage", "file bytes --> ${file.length()}, response bytes -> ${bytes.size}")
                    }, { err ->
                        Log.e("fetchProfileImage", "error: $err")
                    })
                }
            }
        }
    }

}