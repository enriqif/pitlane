package com.widoo.pitlane.di

import com.widoo.pitlane.data.local.AppDatabase
import com.widoo.pitlane.data.local.PreferencesManager
import com.widoo.pitlane.data.repository.ServiceRepository
import com.widoo.pitlane.data.repository.FuelRepository
import com.widoo.pitlane.data.repository.ReminderRepository
import com.widoo.pitlane.data.repository.TripRepository
import com.widoo.pitlane.data.repository.VehicleRepository
import com.widoo.pitlane.ui.screen.charts.ChartsViewModel
import com.widoo.pitlane.ui.screen.fuel.FuelViewModel
import com.widoo.pitlane.ui.screen.home.HomeViewModel
import com.widoo.pitlane.ui.screen.onboarding.OnboardingViewModel
import com.widoo.pitlane.ui.screen.profile.VehicleProfileViewModel
import com.widoo.pitlane.ui.screen.reminder.ReminderViewModel
import com.widoo.pitlane.ui.screen.service.ServiceViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Misma instancia singleton que usan TripTrackingService y los widgets
    // (AppDatabase.getInstance) — si Koin armara la suya propia con
    // Room.databaseBuilder, el Flow de Room no se invalidaría entre instancias y la UI
    // no se enteraría de escrituras hechas desde el service hasta reabrir la app.
    single { AppDatabase.getInstance(androidContext()) }

    single { get<AppDatabase>().serviceRecordDao() }
    single { get<AppDatabase>().fuelLogDao() }
    single { get<AppDatabase>().reminderDao() }
    single { get<AppDatabase>().vehicleDao() }
    single { get<AppDatabase>().tripDao() }

    single { VehicleRepository(get()) }
    single { ServiceRepository(get()) }
    single { FuelRepository(get()) }
    single { ReminderRepository(get()) }
    single { TripRepository(get()) }



    single { PreferencesManager(androidContext()) }

    viewModel { OnboardingViewModel(get(), get()) }

    viewModel { VehicleProfileViewModel(get(), get(), androidContext()) }

    viewModel { HomeViewModel(get(), get(), get(), get(), get(), androidContext()) }

    viewModel { ServiceViewModel(get(), get(), get()) }

    viewModel { FuelViewModel(get(), get(), get(),  androidContext()) }

    viewModel { ChartsViewModel(get(), get(), get()) }

    viewModel { ReminderViewModel(get(), get(),androidContext()) }

    viewModel { ReminderViewModel(get(), get(),androidContext()) }
}