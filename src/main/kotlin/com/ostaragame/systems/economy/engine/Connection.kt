package com.ostaragame.systems.economy.engine

import com.ostaragame.systems.economy.actors.NonPlayerTrader
import kotlinx.serialization.Serializable

@Serializable
data class Connection(
    val location1: String, val location2: String, val distance: Double,
    var infrastructure: Infrastructure, val terrain: Terrain, var weather: Weather,
    var travelers: MutableList<NonPlayerTrader>, ) {


    fun name(): String {
        return "${location1}-${location2}"
    }

    fun travelDirection(startLocationName:String): Int {
        if (startLocationName == location1)
            return 0
        else
            return 1
    }

}
val SelfConnection = Connection("Self", "Self", 0.0, Infrastructure.NONE, Terrain.PLAINS, Weather.CLEAR, mutableListOf())


/*
NONE - Wild Country
TRAILS - Goat trails, Hiking trails
ROUGH - Dirt Road
IMPROVED - Gravel Road, Brick/Cobblestone
EFFICIENT - Asphalt, Rails
ADVANCED - Mag Lev, Hyperloop, Moving Roads
 */
enum class Infrastructure { NONE, TRAILS, ROUGH, IMPROVED, EFFICIENT, ADVANCED }

enum class Terrain { PLAINS, RIVERS, FOREST, HILLS, DESERT, MOUNTAINS, OCEAN }

enum class Weather { CLEAR, CLOUDY, FOG, HOT, RAIN, SNOW, COLD, HURRICANE, BLIZZARD }