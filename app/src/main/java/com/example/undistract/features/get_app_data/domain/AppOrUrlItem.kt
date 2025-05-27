package com.example.undistract.features.get_app_data.domain

import android.graphics.drawable.Drawable

sealed class AppOrUrlItem : Comparable<AppOrUrlItem> {
    abstract val name: String
    abstract val identifier: String
    abstract val icon: Drawable?

    // Untuk aplikasi
    data class AppItem(
        override val name: String,
        override val identifier: String,
        override val icon: Drawable?
    ) : AppOrUrlItem()

    // Untuk URL
    data class UrlItem(
        override val name: String,
        override val identifier: String,
        override val icon: Drawable?
    ) : AppOrUrlItem()

    // Agar bisa di-sort
    override fun compareTo(other: AppOrUrlItem): Int {
        return this.name.compareTo(other.name)
    }
}