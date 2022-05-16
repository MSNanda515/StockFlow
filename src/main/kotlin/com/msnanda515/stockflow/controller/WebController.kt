package com.msnanda515.stockflow.controller

import com.msnanda515.stockflow.repository.ItemRepository
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping


@Controller
class WebController(
    private val itemRepository: ItemRepository
) {

    @GetMapping("/")
    fun index(): String {
        return "index"
    }
}