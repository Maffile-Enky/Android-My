package com.toolbox.services

import at.favre.lib.crypto.bcrypt.BCrypt
import com.toolbox.models.*
import com.toolbox.repository.UserRepository
import com.toolbox.utils.JwtConfig

class AuthService {

    fun register(username: String, password: String): AuthResponse {
        if (username.length < 3) throw IllegalArgumentException("用户名至少3个字符")
        if (password.length < 4) throw IllegalArgumentException("密码至少4个字符")
        if (UserRepository.exists(username)) throw IllegalArgumentException("用户名已存在")

        val hash = BCrypt.withDefaults().hashToString(12, password.toCharArray())
        val user = UserRepository.create(username, hash, "user")
        val token = JwtConfig.generateToken(user.id, user.username, user.role)
        return AuthResponse(token, user)
    }

    fun login(username: String, password: String): AuthResponse {
        val (userId, hash) = UserRepository.findByUsername(username)
            ?: throw IllegalArgumentException("用户不存在")

        val result = BCrypt.verifyer().verify(password.toCharArray(), hash)
        if (!result.verified) throw IllegalArgumentException("密码错误")

        val user = UserRepository.findById(userId)
            ?: throw IllegalArgumentException("用户不存在")

        val token = JwtConfig.generateToken(user.id, user.username, user.role)
        return AuthResponse(token, user)
    }

    fun getProfile(userId: Long): User? = UserRepository.findById(userId)

    fun getAllUsers(): List<User> = UserRepository.findAll()

    fun deleteUser(id: Long): Boolean {
        val user = UserRepository.findById(id) ?: return false
        if (user.username == "root") throw IllegalArgumentException("不能删除默认管理员")
        return UserRepository.delete(id)
    }

    fun updateUserRole(id: Long, role: String): Boolean {
        if (role != "user" && role != "admin") throw IllegalArgumentException("角色必须是 user 或 admin")
        return UserRepository.updateRole(id, role)
    }
}
