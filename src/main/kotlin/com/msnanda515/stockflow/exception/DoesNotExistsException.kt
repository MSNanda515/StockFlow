package com.msnanda515.stockflow.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * The request resource does not exist
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class DoesNotExistsException(msg: String) : Exception(msg)