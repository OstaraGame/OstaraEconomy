package com.ostaragame.systems.economy.engine

import kotlinx.serialization.Serializable

@Serializable
data class TradeGood(val name: String, val type: TradeLibrary.TradeGoodType, var cost: Float, val volume: Float)
