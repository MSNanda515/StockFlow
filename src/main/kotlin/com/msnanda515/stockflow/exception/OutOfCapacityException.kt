package com.msnanda515.stockflow.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Used when warehouse runs out of capacity to store new items
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class OutOfCapacityException(msg: String) : WarehouseCapacityException(msg)