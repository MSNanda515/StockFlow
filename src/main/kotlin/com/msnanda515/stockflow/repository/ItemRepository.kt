package com.msnanda515.stockflow.repository

import com.msnanda515.stockflow.model.Item
import com.msnanda515.stockflow.model.ItemStatus
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface ItemRepository : MongoRepository<Item, String> {
    fun findOneById(id: ObjectId): Item?

    fun findAllByStatus(itemStatus: ItemStatus): List<Item>

    fun findTopByOrderByItemNoDesc(): Item?

    fun findAllByItemNo(itemNo: Long): List<Item>
    override fun deleteAll()
}