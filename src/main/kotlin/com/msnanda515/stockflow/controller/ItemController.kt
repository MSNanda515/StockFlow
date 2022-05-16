package com.msnanda515.stockflow.controller

import com.msnanda515.stockflow.model.Item
import com.msnanda515.stockflow.model.ItemRequestVM
import com.msnanda515.stockflow.repository.ItemRepository
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("api/v1/items")
class ItemController(
    val itemRepository: ItemRepository
) {
    @GetMapping
    fun getAllItems(): ResponseEntity<List<Item>> {
        val items = itemRepository.findAll()
        return ResponseEntity.ok(items)
    }

    @PostMapping(
        consumes = ["application/json"],
    )
    fun createItem(@RequestBody request: ItemRequestVM): ResponseEntity<Item> {
        val item = itemRepository.save(
            Item(
                itemNo = request.itemNo,
                name = request.name,
                description = request.description,
                department = request.department,
                status = request.status
            )
        )
        return ResponseEntity(item, HttpStatus.CREATED)
    }
}