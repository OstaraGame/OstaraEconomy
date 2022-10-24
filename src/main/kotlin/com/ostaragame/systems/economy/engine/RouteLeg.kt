package com.ostaragame.systems.economy.engine

import com.ostaragame.systems.economy.actors.NonPlayerTrader

data class RouteLeg(val connection: Connection, val nextStop: Location, val traderActivity: NonPlayerTrader.TraderActivity)