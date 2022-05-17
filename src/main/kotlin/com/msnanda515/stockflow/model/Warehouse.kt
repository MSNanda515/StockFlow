package com.msnanda515.stockflow.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.stereotype.Indexed

data class Warehouse(
    var wareNo: Long,
    var name: String,
    var location: String,
    @Id
    val id: ObjectId = ObjectId.get(),
)