package com.example.workassistance.util

/** Sealed wrapper so ViewModels can distinguish loading / success / error states. */
sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
}
