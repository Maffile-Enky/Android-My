package com.toolbox.plugins

import com.toolbox.repository.ChatRepository
import com.toolbox.repository.HomeRepository
import com.toolbox.repository.PostRepository
import com.toolbox.repository.UserRepository
import com.toolbox.repository.VersionRepository
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabase() {
    Database.connect(
        url = "jdbc:h2:file:./data/toolbox;DB_CLOSE_DELAY=-1",
        driver = "org.h2.Driver"
    )
    PostRepository.initTable()
    ChatRepository.initTable()
    HomeRepository.initTable()
    UserRepository.initTable()
    VersionRepository.initTable()
}
