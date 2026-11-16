// Modified by teddybaerd for the Nowbar Meter continuation (2026): independent app identity, localization, privacy controls and release maintenance.
package de.teddybaerd.nowbarmeter.data.source

import androidx.compose.runtime.Immutable

@Immutable
data class NetSpeedData(
    val downloadSpeed: Long = 0L,
    val uploadSpeed: Long = 0L,
    val totalSpeed: Long = downloadSpeed + uploadSpeed,
)

data class NetworkTrafficData(
    val rxBytes: Long,
    val txBytes: Long,
)

interface ISpeedDataSource {
    suspend fun getTrafficData(): NetworkTrafficData
}
