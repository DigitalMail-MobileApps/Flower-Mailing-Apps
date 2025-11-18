package org.lsm.flower_mailing.notifications

import com.google.firebase.messaging.FirebaseMessaging

object TopicManager {
    private const val PREFIX = "digitalmail_role_"

    fun subscribeForRole(role: String) {
        val topic = PREFIX + role.lowercase()
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    println("Failed to subscribe to topic: $topic")
                }
            }
    }

    fun unsubscribeFromRole(role: String) {
        val topic = PREFIX + role.lowercase()
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
    }
}