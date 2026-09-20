package com.rshea.cryptotracker.util

import platform.Foundation.NSUUID

actual fun randomUUID() = NSUUID().UUIDString