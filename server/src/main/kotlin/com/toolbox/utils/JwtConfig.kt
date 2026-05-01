package com.toolbox.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import java.util.Date

object JwtConfig {
    private const val SECRET = "toolbox-jwt-secret-key-2024"
    private const val ISSUER = "toolbox-server"
    private const val VALIDITY_MS = 24 * 60 * 60 * 1000L // 24 hours

    private val algorithm = Algorithm.HMAC256(SECRET)

    private val verifier = JWT.require(algorithm)
        .withIssuer(ISSUER)
        .build()

    fun generateToken(userId: Long, username: String, role: String): String {
        return JWT.create()
            .withIssuer(ISSUER)
            .withSubject(userId.toString())
            .withClaim("username", username)
            .withClaim("role", role)
            .withIssuedAt(Date())
            .withExpiresAt(Date(System.currentTimeMillis() + VALIDITY_MS))
            .sign(algorithm)
    }

    fun verifyToken(token: String): DecodedJWT? {
        return try {
            verifier.verify(token)
        } catch (e: Exception) {
            null
        }
    }
}
