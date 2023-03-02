package com.ostaragame.systems.economy.utility

import com.ostaragame.systems.economy.WorldTradeMap
import com.ostaragame.systems.economy.engine.*
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.graphstream.graph.Graph
import org.graphstream.graph.Node
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.graphstream.graph.Edge
import java.io.FileInputStream

class LocationDataLoader {

    val jsonformat = Json { prettyPrint = true }

    private fun loadCsvRecords(fileName: String, headers: Class<out Enum<*>>): List<CSVRecord> {
        val resource: URL? = LocationDataLoader::class.java.getResource(fileName)
        val csvFile: File? = resource?.toURI()?.let { Paths.get(it).toFile() }
        val formatBuilder: CSVFormat.Builder = CSVFormat.Builder.create(CSVFormat.DEFAULT)
        formatBuilder.setHeader(headers)
        formatBuilder.setSkipHeaderRecord(true)

        if (csvFile == null) {
            error("CSV File not found $fileName")
        }
        val reader = Files.newBufferedReader(csvFile.toPath())
        val csvParser = CSVParser(reader, formatBuilder.build())
        return csvParser.records
    }


    fun loadDGSLocationAndConnectionsIntoGraph(graph: Graph, worldTradeMap: WorldTradeMap) {
        println("Loading world locations")
        val resource: URL? = LocationDataLoader::class.java.getResource("/cloudcanyon.dgs")
        val dgsFile: File? = resource?.toURI()?.let { Paths.get(it).toFile() }
        graph.read(dgsFile!!.path)
        for (node in graph.nodes()) {
            val locationName = node.id
            val coords = node.getAttribute("xyz") as Array<*>
            val x:Double = coords[0] as Double
            val y:Double = coords[1] as Double
             if (locationName !in worldTradeMap.locations) {
                val location = Location(
                    locationName, WorldTradeMap.getNextLocationId(),
                    mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), x, y
                )
                worldTradeMap.locations[locationName] = location
            }
        }

        for (edge in graph.edges()) {

            val loc0:Location? = WorldTradeMap.locations[edge.node0.id]
            val loc1:Location? = WorldTradeMap.locations[edge.node1.id]
            val connection = connectonForEdge(edge)
            WorldTradeMap.connections["${edge.node0.id}-${edge.node1.id}"] = connection

            loc0.let { it?.connections?.add(connection) }
            loc1.let { it?.connections?.add(connection) }
        }
    }

    fun connectonForEdge(edge: Edge) : Connection {
        println( "${edge.id} ${Infrastructure.valueOf(edge.getAttribute("infrastructure", String::class.java) )}")

        return Connection(
            edge.node0.id,
            edge.node1.id,
            (edge.getAttribute("distance") ?: 1.0) as Double,
            Infrastructure.valueOf(edge.getAttribute("infrastructure") as String),
            Terrain.valueOf(edge.getAttribute("terrain") as String),
            Weather.valueOf(edge.getAttribute("weather") as String),
            mutableListOf()
        )
    }
    @OptIn(ExperimentalSerializationApi::class)
    fun loadLocationAndConnectionsIntoGraph(graph: Graph, worldTradeMap: WorldTradeMap) {
        println("Loading world locations")

        val list: List<CSVRecord> = loadCsvRecords("/Locations.csv", LocationHeaders::class.java)
        for (record:CSVRecord in list) {
            val nearLocationName:String = record.get(LocationHeaders.Name)
            val nearLocation:Location
            if (nearLocationName in worldTradeMap.locations) {
                nearLocation = worldTradeMap.locations.getValue(nearLocationName)
            } else {
                nearLocation = Location(
                    nearLocationName, WorldTradeMap.getNextLocationId(),
                    mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), 0.0, 0.0
                )
                worldTradeMap.locations[nearLocationName] = nearLocation
            }
            //println("Loading $name")
            val node: Node = graph.addNode(nearLocationName)
            node.setAttribute("ui.label", nearLocationName)
            node.setAttribute("ui.class", record.get(LocationHeaders.Size) )
        }

        val resource: URL? = LocationDataLoader::class.java.getResource("/connections.json")
        val jsonFile: File? = resource?.toURI()?.let { Paths.get(it).toFile() }
        jsonFile.let{
            val connectionList = jsonformat.decodeFromStream<MutableList<Connection>>(FileInputStream(it))
            for (connection in connectionList) {
                val loc1:Location? = WorldTradeMap.locations[connection.location1]
                val loc2:Location? = WorldTradeMap.locations[connection.location2]
                loc1.let { it?.connections?.add(connection) }
                loc2.let { it?.connections?.add(connection) }
                val edge = graph.addEdge("${loc1?.name}-${loc2?.name}", loc1?.name, loc2?.name)
                edge.setAttribute("layout.weight",connection.distance)

            }
        }
    }


    fun loadTradeGoods() {
        println("Loading Trade Goods")
        val list: List<CSVRecord> = loadCsvRecords("/TradeGoods.csv",TradeGoodHeaders::class.java)
        for (record:CSVRecord in list) {
            val key = record.get(TradeGoodHeaders.Name)
            try {
                TradeLibrary.tradeGoods[key] = TradeGood(key,
                    TradeLibrary.TradeGoodType.valueOf(record.get(TradeGoodHeaders.Type)),
                    record.get(TradeGoodHeaders.Cost).toInt(),
                    record.get(TradeGoodHeaders.Volume).toInt())
            } catch (e: IllegalArgumentException) {
                error("Exception '${e.message}' loading '$key'")
            }
        }
    }

    fun loadTradeGoodDemands(worldTradeMap: WorldTradeMap) {
        println("Loading Trade Good Demands")
        val list: List<CSVRecord> = loadCsvRecords("/TradeGoodDemands.csv", TradeGoodDemandHeaders::class.java)
        for (record:CSVRecord in list) {
            val locationName = record.get(TradeGoodDemandHeaders.LocationName)
            val location = worldTradeMap.locations[locationName]
            if (location == null) {
                println("WARNING: No location found for $locationName")
                continue
            }
            val tradeGoodName = record.get(TradeGoodDemandHeaders.TradeGoodName)
            val tradeGood = TradeLibrary.tradeGoods[tradeGoodName]
            if (tradeGood == null) {
                println("WARNING: No Trade Good found for $tradeGoodName")
                continue
            }
            val tradeGoodDemand = TradeGoodDemand(tradeGood,
                location,
                record.get(TradeGoodDemandHeaders.DemandRate).toInt(),
                record.get(TradeGoodDemandHeaders.Inventory).toInt(),
                record.get(TradeGoodDemandHeaders.StorageClass).toInt(),
                record.get(TradeGoodDemandHeaders.CeilingPrice).toInt(),
            )
            location.demand.add(tradeGoodDemand)
        }
    }

    fun loadTradeGoodSupply(worldTradeMap: WorldTradeMap) {
        println("Loading Trade Good Supply")
        val list: List<CSVRecord> = loadCsvRecords("/TradeGoodSupply.csv",TradeGoodSupplyHeaders::class.java)
        for (record:CSVRecord in list) {
            val locationName = record.get(TradeGoodSupplyHeaders.LocationName)
            val location = worldTradeMap.locations[locationName]
            if (location == null) {
                println("WARNING: No location found for $locationName")
                continue
            }
            val tradeGoodName = record.get(TradeGoodSupplyHeaders.TradeGoodName)
            val tradeGood = TradeLibrary.tradeGoods[tradeGoodName]
            if (tradeGood == null) {
                println("WARNING: No Trade Good found for $tradeGoodName")
                continue
            }
            val tradeGoodSupply = TradeGoodSupply(tradeGood,
                location,
                record.get(TradeGoodSupplyHeaders.Inventory).toInt(),
                record.get(TradeGoodSupplyHeaders.InventoryMax).toInt(),
                record.get(TradeGoodSupplyHeaders.RestockRateBase).toInt(),
                record.get(TradeGoodSupplyHeaders.RestockRate).toInt(),
                tradeGood.cost
            )
            location.supply.add(tradeGoodSupply)
        }
    }

    enum class LocationHeaders:CSVHeader {
        Name, Size
    }

    enum class TradeGoodHeaders:CSVHeader {
        Name, Type, Cost, Volume
    }

    enum class TradeGoodDemandHeaders:CSVHeader {
        LocationName,TradeGoodName,DemandRate,Inventory,StorageClass,CeilingPrice
    }

    enum class TradeGoodSupplyHeaders:CSVHeader {
        LocationName,TradeGoodName,Inventory,InventoryMax,RestockRateBase,RestockRate
    }

    /*
    fun writeOutConnections() {
        val connections = mutableListOf<Connection>()

        var nearLocation = WorldTradeMap.locations["Old Town"]!!
        var distantLocation = WorldTradeMap.locations["Scrapyard"]!!
        var connection = Connection(nearLocation.name, distantLocation.name,20.0,Infrastructure.ROUGH, Terrain.HILLS, Weather.CLEAR, mutableListOf())
        connections.add(connection)

        nearLocation = WorldTradeMap.locations["Old Town"]!!
        distantLocation = WorldTradeMap.locations["Lucky Bend"]!!
        connection = Connection(nearLocation.name, distantLocation.name,100.0,Infrastructure.IMPROVED, Terrain.HILLS, Weather.CLEAR, mutableListOf())
        connections.add(connection)

        nearLocation = WorldTradeMap.locations["Old Town"]!!
        distantLocation = WorldTradeMap.locations["Badlands"]!!
        connection = Connection(nearLocation.name, distantLocation.name,120.0,Infrastructure.ROUGH, Terrain.PLAINS, Weather.CLEAR, mutableListOf())
        connections.add(connection)

        nearLocation = WorldTradeMap.locations["Lucky Bend"]!!
        distantLocation = WorldTradeMap.locations["Bluewater"]!!
        connection = Connection(nearLocation.name, distantLocation.name,120.0,Infrastructure.IMPROVED, Terrain.PLAINS, Weather.CLEAR, mutableListOf())
        connections.add(connection)

        nearLocation = WorldTradeMap.locations["Lucky Bend"]!!
        distantLocation = WorldTradeMap.locations["Badlands"]!!
        connection = Connection(nearLocation.name, distantLocation.name,200.0,Infrastructure.ROUGH, Terrain.PLAINS, Weather.CLEAR, mutableListOf())
        connections.add(connection)

        nearLocation = WorldTradeMap.locations["Badlands"]!!
        distantLocation = WorldTradeMap.locations["Albuquerque"]!!
        connection = Connection(nearLocation.name, distantLocation.name,100.0,Infrastructure.TRAILS, Terrain.DESERT, Weather.HOT, mutableListOf())
        connections.add(connection)

        nearLocation = WorldTradeMap.locations["Badlands"]!!
        distantLocation = WorldTradeMap.locations["Black Rock Canyon Town"]!!
        connection = Connection(nearLocation.name, distantLocation.name,50.0,Infrastructure.IMPROVED, Terrain.HILLS, Weather.HOT, mutableListOf())
        connections.add(connection)

        nearLocation = WorldTradeMap.locations["Black Rock Canyon Town"]!!
        distantLocation = WorldTradeMap.locations["South Albuquerque Farms"]!!
        connection = Connection(nearLocation.name, distantLocation.name,75.0,Infrastructure.IMPROVED, Terrain.RIVERS, Weather.CLEAR, mutableListOf())
        connections.add(connection)

        nearLocation = WorldTradeMap.locations["Albuquerque"]!!
        distantLocation = WorldTradeMap.locations["South Albuquerque Farms"]!!
        connection = Connection(nearLocation.name, distantLocation.name,15.0,Infrastructure.EFFICIENT, Terrain.PLAINS, Weather.CLEAR, mutableListOf())
        connections.add(connection)

        val jsonOutput = jsonformat.encodeToString(connections)

        val jsonFilePath = Paths.get("connections.json")
        Files.writeString(jsonFilePath,jsonOutput,StandardOpenOption.CREATE)
    }

     */
}

interface CSVHeader
