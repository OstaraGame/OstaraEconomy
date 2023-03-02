package com.ostaragame.systems.economy.actors

import com.ostaragame.systems.economy.engine.TradeGood
import kotlinx.serialization.Serializable

@Serializable
data class Cargo (val tradeGood: TradeGood, var units:Int)