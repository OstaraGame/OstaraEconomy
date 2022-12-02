package com.ostaragame.systems.economy.engine

import com.ostaragame.systems.economy.WorldTradeMap
import com.ostaragame.systems.economy.ui.TradeViewer

object WeatherEngine : ServerTick {

    private var dayCount = 1
    private var tickCount = 0
    override fun doTick() {
        tickCount++
        if (tickCount % 10 == 0) {
            dayCount++
            updateWeather()
        }
    }

    fun updateWeather() {
        val BadlandsAlbuquerque = TradeViewer.graph.getEdge("Badlands-Albuquerque")
        if (dayCount % 3 == 0) {
            BadlandsAlbuquerque.removeAttribute("weather")
            BadlandsAlbuquerque.setAttribute("weather", "HOT")
            WorldTradeMap.connections[BadlandsAlbuquerque.id]?.weather = Weather.HOT
        } else if (dayCount % 3 == 1){
            BadlandsAlbuquerque.removeAttribute("weather")
            BadlandsAlbuquerque.setAttribute("weather", "SNOW")
            WorldTradeMap.connections[BadlandsAlbuquerque.id]?.weather = Weather.SNOW
         } else {
            BadlandsAlbuquerque.removeAttribute("weather")
            BadlandsAlbuquerque.setAttribute("weather", "CLEAR")
            WorldTradeMap.connections[BadlandsAlbuquerque.id]?.weather = Weather.CLEAR
        }
        val weather = BadlandsAlbuquerque.getAttribute("weather")
        TradeViewer.spriteManager.getSprite("Badlands-Albuquerque").setAttribute("ui.class", weather)
        println("Weather on 'Badlands-Albuquerque' road $weather")
    }
}