package com.ostaragame.systems.economy.engine

import com.ostaragame.systems.economy.actors.NonPlayerTrader
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Connection(val location1: String, val location2: String, val distance: Float,
                      var infrastructure: Infrastructure, val terrain: Terrain, var weather: Weather,
                      var travelers: MutableList<NonPlayerTrader>, )

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