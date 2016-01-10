package com.nvbn.hubotforglass

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.result.Result
import com.github.salomonbrys.kotson.jsonObject
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.deferred
import nl.komponents.kovenant.then
import nl.komponents.kovenant.functional.bind
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.warn
import org.json.JSONArray
import org.json.JSONObject

/**
 * Wraps json response in promise.
 */
public fun Request.jsonPromise(): Promise<JSONObject, Exception> {
    val response = deferred<JSONObject, Exception>()
    responseJson { req, res, result ->
        when (result) {
            is Result.Failure -> response.reject(result.error!!)
            is Result.Success -> response.resolve(result.value!!)
        }
    }
    return response.promise
}

/**
 * Converts `JSONArray` to `List<String>`
 */
public fun JSONArray.asStringList() = if (length() == 0) {
    listOf()
} else {
    (0..(length() - 1)).map { getString(it) }
}

class HubotClient(val url: String) : AnkoLogger {
    /**
     * Promise with authorisation details.
     */
    val authorised by lazy {
        Fuel.post("$url/polling/subscribe/")
                .jsonPromise()
                .fail { warn("Can't authorize because $it") }
                .then { it.getString("user") }
    }

    /**
     * Sends message to hubot.
     */
    fun sendMessage(text: String) = authorised.success { user ->
        Fuel.post("$url/polling/message/")
                .body(jsonObject("user" to user, "text" to text).toString())
                .header("Content-Type" to "application/json")
                .jsonPromise()
                .success { info("Message $text sent") }
                .fail { warn("Can't send message $text because $it") }
    }

    /**
     * Receives hubot responses.
     */
    fun getResponses() = authorised.bind { user ->
        Fuel.get("$url/polling/response/$user/")
                .jsonPromise()
                .fail { warn("Can't get messages: $it") }
                .success { info("Got responses: $it") }
                .then { it.getJSONArray("messages").asStringList() }
    }
}
