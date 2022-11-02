package org.dgeek.imagemanipulator.di

import org.dgeek.imagemanipulator.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {
    viewModel { MainViewModel() }
}