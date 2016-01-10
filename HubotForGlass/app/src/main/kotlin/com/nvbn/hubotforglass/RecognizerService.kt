package com.nvbn.hubotforglass

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.speech.RecognizerIntent
import com.google.android.glass.timeline.LiveCard
import com.google.android.glass.widget.CardBuilder
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.android.startKovenant
import nl.komponents.kovenant.android.stopKovenant
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.async
import org.jetbrains.anko.ctx
import org.jetbrains.anko.info

val HUBOT_URL = "http://hubot.local:9999"

class RecognizerService : Service(), AnkoLogger {
    val client = HubotClient(HUBOT_URL)
    var intent: Intent? = null

    val card by lazy { LiveCard(this, "HUBOT_LIVE_CARD") }

    override fun onBind(intent: Intent): IBinder? {
        this.intent = intent
        return null
    }

    /**
     * Polls responses from hubot.
     */
    tailrec fun checkResponses(previous: Promise<Any?, Any?>?) {
        info("Check responses")
        if (previous == null || previous.isDone()) {
            val responses = client.getResponses().success {
                if (it.count() > 0) showResponse(it.joinToString(","))
            }
            checkResponses(responses)
        } else {
            Thread.sleep(500)
            checkResponses(previous)
        }
    }

    fun checkResponses() = checkResponses(null)

    override fun onCreate() {
        super.onCreate()
        startKovenant()
        async() { checkResponses() }
    }

    /**
     * Sends message to hubot when voice command received.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (intent is Intent) {
            val command = intent.extras
                    .getStringArrayList(RecognizerIntent.EXTRA_RESULTS)[0]

            info("Command received: $command.")
            async() { client.sendMessage(command) }
        }

        return START_STICKY
    }

    /**
     * Recreates card for new responses.
     */
    fun updateCard() {
        card.unpublish()
        val intent = Intent(ctx, MenuActivity::class.java)
        card.setAction(PendingIntent.getActivity(ctx, 0, intent, 0))
        card.publish(LiveCard.PublishMode.REVEAL)
    }

    /**
     * Shows card with received hubot responses.
     */
    fun showResponse(response: String) {
        updateCard()

        val views = CardBuilder(ctx, CardBuilder.Layout.TEXT)
                .setFootnote("Hubot")
                .setText(response)
                .setTimestamp("just now")
                .remoteViews

        card.setViews(views)
    }

    override fun onDestroy() {
        super.onDestroy()
        card.unpublish()
        stopKovenant()
    }
}
