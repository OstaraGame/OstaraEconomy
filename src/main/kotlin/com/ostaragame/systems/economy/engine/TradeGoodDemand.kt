package com.ostaragame.systems.economy.engine

import kotlinx.serialization.Serializable

@Serializable
data class TradeGoodDemand (val tradeGood: TradeGood, val location: Location, var unitsDemanded: Float, val demandRate: Float, var ceilingPrice: Float)
