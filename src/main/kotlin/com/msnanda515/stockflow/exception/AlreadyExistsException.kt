package com.msnanda515.stockflow.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class AlreadyExistsException(msg: String) : Exception(msg)