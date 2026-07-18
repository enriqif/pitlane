package com.widoo.pitlane.di

import androidx.room.Room
import com.widoo.pitlane.data.local.AppDatabase
import com.widoo.pitlane.data.local.PreferencesManager
import com.widoo.pitlane.data.repository.ServiceRepository
import com.widoo.pitlane.data.repository.FuelRepository
import com.widoo.pitlane.data.repository.ReminderRepository
import com.widoo.pitlane.data.repository.VehicleRepository
import com.widoo.pitlane.ui.screen.fuel.FuelViewModel
import com.widoo.pitlane.ui.screen.home.HomeViewModel
import com.widoo.pitlane.ui.screen.onboarding.OnboardingViewModel
import com.widoo.pitlane.ui.screen.service.ServiceViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "pitlane.db"
        ).build()
    }

    single { get<AppDatabase>().serviceRecordDao() }
    single { get<AppDatabase>().fuelLogDao() }
    single { get<AppDatabase>().reminderDao() }
    single { get<AppDatabase>().vehicleDao() }

    single { VehicleRepository(get()) }
    single { ServiceRepository(get()) }
    single { FuelRepository(get()) }
    single { ReminderRepository(get()) }


    viewModel { HomeViewModel(get(), get(), get()) }

    single { PreferencesManager(androidContext()) }
    viewModel { OnboardingViewModel(get(), get()) }
    viewModel { HomeViewModel(get(), get(), get()) }


    viewModel { ServiceViewModel(get(), get(), get()) }
    viewModel { FuelViewModel(get(), get(), get()) }


}