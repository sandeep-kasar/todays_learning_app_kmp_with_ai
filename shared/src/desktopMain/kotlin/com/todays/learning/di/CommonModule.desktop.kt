package com.todays.learning.di

import com.todays.learning.utils.DatastoreFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single { DatastoreFactory().createDatastore() }
}