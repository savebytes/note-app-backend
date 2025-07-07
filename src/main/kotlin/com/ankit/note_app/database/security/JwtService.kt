package com.ankit.note_app.database.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwt
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Base64
import java.util.Date

@Service
class JwtService(
    @Value("JWT_SECRET_BASE64") private val jwtSecret : String
){

    private val secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret))

    private val accessTokenValidityMs = 15L * 60L * 1000L
    val refreshTokenValidityMs = 30L * 24 * 60 * 60 * 1000L

    private fun generateToken(
        userId : String,
        types: String,
        expiry : Long
    ) : String {
        val now = Date()
        val expiryDate = Date(now.time + expiry)
        return Jwts.builder()
            .subject(userId)
            .claim("type", types)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    fun generateAccessToken(userId: String) : String {
        return generateToken(userId, "access", accessTokenValidityMs)
    }

    fun generateRefreshToken(userId: String) : String {
        return generateToken(userId, "refresh", refreshTokenValidityMs)
    }

    fun validAccessToken(token: String) : Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        return tokenType == "access"
    }

    fun validRefreshToken(token: String) : Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        return tokenType == "refresh"
    }

    fun getUserIdFromToken(token: String) : String{
        val claims = parseAllClaims(token) ?: throw IllegalArgumentException("Invalid Token...")
        return claims.subject
    }

    private fun parseAllClaims(token: String) : Claims? {
        val rawToken = if (token.startsWith("Bearer ")) {
            token.removePrefix("Bearer ")
        } else token
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(rawToken)
                .payload
        } catch (e : Exception) {
            null
        }
    }

}