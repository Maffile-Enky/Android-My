package com.toolbox.plugins

import com.toolbox.repository.PostRepository
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabase() {
    Database.connect(
        url = "jdbc:h2:file:./data/toolbox;DB_CLOSE_DELAY=-1",
        driver = "org.h2.Driver"
    )
    PostRepository.initTable()
}
