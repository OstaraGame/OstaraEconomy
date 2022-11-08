package com.ostaragame.systems.economy.engine

object TradeLibrary {
    val tradeGoods: MutableMap<String,TradeGood> = mutableMapOf()

    enum class TradeGoodType {
        EDIBLES,
        WEARABLES,
        VALUABLES,
        SPECIAL,
        FUELS,
        MINERALS,
        ALCHEMY,
        TEXTILES,
        WIDGETS,
        TOOLS,
        BUILDING

    }
}