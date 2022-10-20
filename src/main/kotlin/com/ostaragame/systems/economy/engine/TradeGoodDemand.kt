package com.ostaragame.systems.economy.engine

data class TradeGoodDemand (val tradeGood: TradeGood, val location: Location, var unitsDemanded: Float, val demandRate: Float, var ceilingPrice: Float)
