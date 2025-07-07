package com.ankit.note_app.database.security

import io.jsonwebtoken.security.Password
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class HashedEncoder {

    private val bCrypt = BCryptPasswordEncoder()

    fun encode(rawString : String) : String = bCrypt.encode(rawString)

    fun matches(rawString: String, hashed: String) : Boolean = bCrypt.matches(rawString, hashed)


}