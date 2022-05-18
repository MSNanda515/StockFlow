package com.msnanda515.stockflow.service

import com.msnanda515.stockflow.model.Item
import com.msnanda515.stockflow.model.ItemStatus
import com.msnanda515.stockflow.repository.ItemRepository
import org.springframework.stereotype.Service

@Service
class ItemService(val itemRepository: ItemRepository) {
    /**
     * Gets all active items
     */
    fun getAllItems(): List<Item> {
        return itemRepository.findAllByStatus(ItemStatus.ACTIVE)
    }


}