package com.ankit.note_app.database.model

import io.jsonwebtoken.security.Password
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("users")
data class User(
    val email : String,
    val hashedPassword : String,
    @Id val id: ObjectId = ObjectId()
)
