package com.ankit.note_app.database.repository

import com.ankit.note_app.database.model.User
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepository : MongoRepository<User, ObjectId>{
    fun findByEmail(email: String) : User?
}