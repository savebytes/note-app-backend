package com.ankit.note_app.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class MainController {

    @GetMapping
    fun getMain() : String {
        return "This is the main Page for Note App"
    }
}