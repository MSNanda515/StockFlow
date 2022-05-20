package com.msnanda515.stockflow.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Thrown when invalid capacity required for warehouse
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidCapacityException(msg: String) : Exception(msg)