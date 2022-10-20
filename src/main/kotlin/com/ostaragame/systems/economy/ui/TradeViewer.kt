package com.ostaragame.systems.economy.ui

import com.ostaragame.systems.economy.WorldTradeMap
import com.ostaragame.systems.economy.engine.Location
import com.ostaragame.systems.economy.engine.TradeGoodDemand
import com.ostaragame.systems.economy.engine.TradeGoodSupply
import org.graphstream.graph.Edge
import org.graphstream.graph.Graph
import org.graphstream.graph.Node
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants
import org.graphstream.ui.spriteManager.SpriteManager

private const val SUPPLY_SPRITE_START_ANGLE = 30.0
private const val DEMAND_SPRITE_START_ANGLE = 330.0

private const val SPRITE_OFFSET_ANGLE = 15.0

private const val SPRITE_RADIUS = 25.0

private const val SPRITE_Z_ROTATION_ANGLE = 0.0



class TradeViewer {


    public fun loadedGraph(): Graph {
        val graph: Graph = SingleGraph("Cloud Canyon")

        graph.setAttribute("ui.stylesheet", styleSheet);
        graph.setAutoCreate(true)
        graph.setStrict(false)
        return graph
    }


    public fun updateSupplyAndDemandOnGraph(graph: Graph, worldTradeMap: WorldTradeMap, spriteManager: SpriteManager) {

        for (location: Location in WorldTradeMap.locations.values) {
            //TODO: Use Sprite Manager plus something to "update" (remove things that don't exist at a location any longer)
            for ((demandCount, demand: TradeGoodDemand) in location.demand.withIndex()) {
                val demandSprite = spriteManager.addSprite("demandX" + demand.tradeGood.name + "X" + location.name)
                demandSprite.setAttribute("ui.class",demand.tradeGood.name.lowercase())
                demandSprite.attachToNode(location.name)
//                demandSprite.setPosition(SPRITE_RADIANS, DEMAND_SPRITE_START_ANGLE - (demandCount * SPRITE_OFFSET_ANGLE),
//                    SPRITE_Z_ROTATION_ANGLE
//                )
                demandSprite.setPosition(StyleConstants.Units.PX, -SPRITE_RADIUS,20.0*demandCount,0.0)
            }
            for ((supplyCount, supply: TradeGoodSupply) in location.supply.withIndex()) {
                val supplySprite = spriteManager.addSprite("supplyX" + supply.tradeGood.name + "X" + location.name)
                supplySprite.setAttribute("ui.class",supply.tradeGood.name.lowercase())
                supplySprite.attachToNode(location.name)
//                supplySprite.setPosition(SPRITE_RADIANS, SUPPLY_SPRITE_START_ANGLE + (supplyCount * SPRITE_OFFSET_ANGLE),
//                    SPRITE_Z_ROTATION_ANGLE
//                )
                supplySprite.setPosition(StyleConstants.Units.PX, SPRITE_RADIUS,20.0*supplyCount,0.0)
            }

        }
    }

    public fun tutorialGraph() {
        val graph: Graph = SingleGraph("Tutorial 1")

        graph.setAttribute("ui.stylesheet", styleSheet);
        graph.setAutoCreate(true)
        graph.setStrict(false)
        graph.display()

        var node: Node = graph.addNode("Old Town")
        graph.addEdge("Scrapper Road", "Old Town", "Scrapyard")
//        node.setAttribute("ui.class", "level2")
        graph.addNode("Scrapyard")
        graph.addNode("Lucky Bend")
        graph.addNode("Badlands")
        graph.addNode("Bluewater")
        graph.addNode("Black Rock Canyon Town")
        node = graph.addNode("Albuquerque")
//        node.setAttribute("ui.class", "level3")
        graph.addNode("South Albuquerque Farms")


        graph.addEdge("Trader Road", "Old Town", "Lucky Bend")
        graph.addEdge("Old Town Road", "Old Town", "Badlands")

        graph.addEdge("Cloud Canyon Way", "Lucky Bend", "Badlands")
        graph.addEdge("Blue Creek Path", "Lucky Bend", "Bluewater")

        graph.addEdge("Black Rock Road", "Badlands", "Black Rock Canyon Town")
        graph.addEdge("Badlands-Albuquerque", "Badlands", "Albuquerque")

        graph.addEdge("Farm to Market Road", "Albuquerque", "South Albuquerque Farms")
        graph.addEdge("Back Road", "Black Rock Canyon Town", "South Albuquerque Farms")

        for (node: Node in graph)
            node.setAttribute("ui.label", node.id)

       for (edge: Edge in graph.edges())
            edge.setAttribute("ui.label", edge.id)


        //explore(graph.getNode("Albuquerque"));
    }

    fun explore(source: Node) {
        val k = source.breadthFirstIterator

        while (k.hasNext()) {
            val next = k.next()
            next!!.setAttribute("ui.class", "marked")
            sleep()
        }
    }

    protected fun sleep() {
        try {
            Thread.sleep(1000)
        } catch (_: Exception) {
        }
    }

    private var styleSheet = "node {" +
            "	fill-color: black;" +
            "}" +
            "node.marked {" +
            "	fill-color: red;" +
            "}" +
            "node.level3 {" +
            "	size: 60px, 60px;" +
            "}" +
            "node.level2 {" +
            "	size: 40px, 40px;" +
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
            "}"

}