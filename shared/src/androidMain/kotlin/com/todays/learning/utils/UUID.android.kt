package com.todays.learning.utils

import java.util.UUID

actual fun getUUIDString(): String = UUID.randomUUID().toString()