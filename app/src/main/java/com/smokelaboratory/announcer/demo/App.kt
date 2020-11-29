package com.smokelaboratory.announcer.demo

import android.app.Application
import com.smokelaboratory.announcer.Announcer
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(module {
                /**
                 * injecting singleton instance of [Announcer] in koin
                 * this is used throughout the app for all types of announcements
                 */
                single { Announcer() }
            })
        }
    }
}