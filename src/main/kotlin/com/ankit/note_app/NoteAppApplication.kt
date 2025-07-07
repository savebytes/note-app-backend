package com.ankit.note_app

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NoteAppApplication

fun main(args: Array<String>) {

	val dotenv = Dotenv.load()

	// Push .env variables into system properties
	dotenv.entries().forEach {
		System.setProperty(it.key, it.value)
	}

	println("Loaded DB_URL: " + System.getProperty("SPRING_DATA_MONGODB_URI"))
	println("Loaded PORT: " + System.getProperty("PORT"))
	println("JWT SECRET KEY: " + System.getProperty("JWT_SECRET_BASE64"))

	runApplication<NoteAppApplication>(*args)
}

