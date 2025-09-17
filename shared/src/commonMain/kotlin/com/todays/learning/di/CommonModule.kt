package com.todays.learning.di


import com.todays.learning.data.datasources.SettingsRepositoryImpl
import com.todays.learning.data.datasources.TimetableRepositoryImpl
import com.todays.learning.domain.repositories.SettingsRepository
import com.todays.learning.domain.repositories.TimetableRepository
import com.todays.learning.domain.utils.Constants.BASE_URL
import com.todays.learning.domain.utils.Constants.URL_PATH
import com.todays.learning.ui.screens.home.HomeViewModel
import com.todays.learning.ui.screens.main.MainViewModel
import com.todays.learning.ui.screens.details.DetailsViewModel
import com.todays.learning.ui.screens.me.MeViewModel
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.addDefaultResponseValidation
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.module.Module
import org.koin.dsl.module

fun commonModule(enableNetworkLogs: Boolean) = module {
    /**
     * Creates a http client for Ktor that is provided to the
     * API client via constructor injection
     */
    single {
        HttpClient {
            expectSuccess = true
            addDefaultResponseValidation()

            defaultRequest {
                url {
                    protocol = URLProtocol.HTTPS
                    host = BASE_URL
                    path(URL_PATH)
                }
            }

            if (enableNetworkLogs) {
                install(Logging) {
                    level = LogLevel.ALL
                    logger = object : Logger {
                        override fun log(message: String) {
                            Napier.i(tag = "Http Client", message = message)
                        }
                    }
                }.also {
                    Napier.base(DebugAntilog())
                }
            }

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = false
                    isLenient = true
                })
            }
        }
    }

    single<TimetableRepository> { TimetableRepositoryImpl(httpClient = get()) }
    single<SettingsRepository> { SettingsRepositoryImpl() }

    // ViewModels
    viewModelOf(::MainViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::DetailsViewModel)
    viewModelOf(::MeViewModel)
}


expect fun platformModule(): Module
