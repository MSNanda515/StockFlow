package com.msnanda515.stockflow.repository

import com.msnanda515.stockflow.model.Item
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface ItemRepository : MongoRepository<Item, String> {
    fun findOneById(id: ObjectId): Item
    override fun deleteAll()
}