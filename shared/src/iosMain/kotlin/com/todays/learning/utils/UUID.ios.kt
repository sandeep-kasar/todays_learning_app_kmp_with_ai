package com.todays.learning.utils

import platform.Foundation.NSUUID.Companion.UUID

actual fun getUUIDString(): String = UUID().UUIDString