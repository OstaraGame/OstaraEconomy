package com.ostaragame.systems.economy.ui

import com.ostaragame.systems.economy.WorldTradeMap
import com.ostaragame.systems.economy.engine.Location
import com.ostaragame.systems.economy.engine.TradeGoodDemand
import com.ostaragame.systems.economy.engine.TradeGoodSupply
import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants
import org.graphstream.ui.spriteManager.SpriteManager

//private const val SUPPLY_SPRITE_START_ANGLE = 30.0
//private const val DEMAND_SPRITE_START_ANGLE = 330.0

//private const val SPRITE_OFFSET_ANGLE = 15.0

private const val SPRITE_RADIUS = 25.0

//private const val SPRITE_Z_ROTATION_ANGLE = 0.0



object TradeViewer {

    val graph: Graph
    val spriteManager:SpriteManager

    init {
        System.setProperty("org.graphstream.ui", "swing")
        graph = SingleGraph("Cloud Canyon")

        graph.setAttribute("ui.stylesheet", GRAPH_STYLESHEET)
        graph.setAutoCreate(true)
        graph.setStrict(false)

        spriteManager = SpriteManager(graph)
    }


    fun updateSupplyAndDemandOnGraph() {

        for (location: Location in WorldTradeMap.locations.values) {
            //TODO: Use Sprite Manager plus something to "update" (remove things that don't exist at a location any longer)
            for ((demandCount, demand: TradeGoodDemand) in location.demand.withIndex()) {
                val demandSprite = spriteManager.addSprite("demandX" + demand.tradeGood.name + "X" + location.name)
                demandSprite.setAttribute("ui.class", demand.tradeGood.name.lowercase())
                demandSprite.attachToNode(location.name)
//                demandSprite.setPosition(SPRITE_RADIANS, DEMAND_SPRITE_START_ANGLE - (demandCount * SPRITE_OFFSET_ANGLE),
//                    SPRITE_Z_ROTATION_ANGLE
//                )
                demandSprite.setPosition(StyleConstants.Units.PX, -SPRITE_RADIUS, 20.0 * demandCount, 0.0)
            }
            for ((supplyCount, supply: TradeGoodSupply) in location.supply.withIndex()) {
                val supplySprite = spriteManager.addSprite("supplyX" + supply.tradeGood.name + "X" + location.name)
                supplySprite.setAttribute("ui.class", supply.tradeGood.name.lowercase())
                supplySprite.attachToNode(location.name)
//                supplySprite.setPosition(SPRITE_RADIANS, SUPPLY_SPRITE_START_ANGLE + (supplyCount * SPRITE_OFFSET_ANGLE),
//                    SPRITE_Z_ROTATION_ANGLE
//                )
                supplySprite.setPosition(StyleConstants.Units.PX, SPRITE_RADIUS, 20.0 * supplyCount, 0.0)
            }

        }
    }
}

    const val GRAPH_STYLESHEET = "node {" +
            "	fill-color: grey;" +
            "}" +
            "node.marked {" +
            "	fill-color: red;" +
            "}" +
            "node.level3 {" +
            "	size: 40px, 40px;" +
            "}" +
            "node.level2 {" +
            "	size: 30px, 30px;" +
            "}" +
            "node.level1 {" +
            "	size: 20px, 20px;" +
            "}"+
            "sprite {" +
            "	size: 15px, 15px;" +
            "	shape: box;" +
            "   fill-color: black;" +
            "}"+
            "sprite.trader {" +
            "	size: 15px, 15px;" +
            "	shape: cross;" +
            "   fill-color: blue;" +
            "}"+
            "sprite.traderderrmann {" +
            "	size: 15px, 15px;" +
            "	shape: cross;" +
            "   fill-color: green;" +
            "}"+
            "sprite.traderanon {" +
            "	size: 15px, 15px;" +
            "	shape: cross;" +
            "   fill-color: red;" +
            "}"+
            "sprite.water {" +
            "	size: 15px, 15px;" +
            "	shape: circle;" +
            "   fill-color: blue;" +
            "}"+
            "sprite.glass {" +
            "	size: 15px, 15px;" +
            "	shape: box;" +
            "   fill-color: blue;" +
            "}"+
            "sprite.parts {" +
            "	size: 15px, 15px;" +
            "	shape: rounded-box;" +
            "   fill-color: grey;" +
            "}"+
            "sprite.fabric {" +
            "	size: 15px, 15px;" +
            "	shape: diamond;" +
            "   fill-color: orange;" +
            "}"+
            "sprite.SNOW {" +
            "	size: 15px, 15px;" +
            "	shape: cross;" +
            "   fill-color: Snow;" +
            "   shadow-mode: plain;" +
            "}"+
            "sprite.HOT {" +
            "	size: 15px, 15px;" +
            "	shape: circle;" +
            "   fill-color: Yellow;" +
            "   shadow-mode: plain;" +
            "}"+
            "sprite.CLEAR {" +
            "	size: 15px, 15px;" +
            "	shape: circle;" +
            "   fill-color: LightBlue;" +
            "   shadow-mode: plain;" +
            "}"

