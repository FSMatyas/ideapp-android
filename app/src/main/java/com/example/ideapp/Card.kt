package com.example.ideapp

data class Message(
    var sender: String = "",
    var text: String = "",
    var timestamp: com.google.firebase.Timestamp? = null
)

data class Card(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var status: String = "",
    var creatorId: String = "",
    var messages: List<Message> = emptyList(),
    var submitterName: String = "",
    var submitterEmail: String = "",
    var createdAt: com.google.firebase.Timestamp? = null
)
