package com.ostaragame.systems.economy.engine

import com.ostaragame.systems.economy.actors.NonPlayerTrader

data class Connection(val location1: Location, val location2: Location, val distance: Float,
                      var infrastructure: Infrastructure, val terrain: Terrain, var weather: Weather,
                      var travelers: MutableCollection<NonPlayerTrader>, )

enum class Infrastructure { NONE, TRAILS, ROUGH, IMPROVED, EFFICIENT, ADVANCED }

enum class Terrain { PLAINS, RIVERS, FOREST, HILLS, DESERT, MOUNTAINS, OCEAN }

enum class Weather { CLEAR, HOT, RAIN, SNOW, COLD, HURRICANE }