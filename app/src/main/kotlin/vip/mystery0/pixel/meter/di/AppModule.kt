package de.teddybaerd.nowbarmeter.di

import android.app.NotificationManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.PowerManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import de.teddybaerd.nowbarmeter.data.repository.DataStoreRepository
import de.teddybaerd.nowbarmeter.data.repository.NetworkRepository
import de.teddybaerd.nowbarmeter.data.repository.dataStore
import de.teddybaerd.nowbarmeter.data.source.impl.SpeedDataSource
import de.teddybaerd.nowbarmeter.service.NotificationHelper
import de.teddybaerd.nowbarmeter.ui.overlay.OverlayWindow

val appModule = module {
    single { androidContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    single { androidContext().getSystemService(Context.POWER_SERVICE) as PowerManager }
    single { androidContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    single { DataStoreRepository(androidContext().dataStore) }
    single { SpeedDataSource(get()) }

    single { NetworkRepository(get(), get()) }

    factory { NotificationHelper(androidContext()) }
    factory { OverlayWindow(androidContext(), get()) }
}
