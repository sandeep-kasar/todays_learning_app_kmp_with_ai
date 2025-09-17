package com.todays.learning.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.todays.learning.domain.utils.Constants

actual class DatastoreFactory(private val context: Context) {
    actual fun createDatastore(): DataStore<Preferences> {
        return initDataStore {
            context.filesDir.resolve(Constants.DATASTORE_FILE_NAME).absolutePath
        }
    }
}
