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
import java.io.FileInputStream
import java.nio.file.StandardOpenOption

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
                    mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf()
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
                var edge = graph.addEdge("${loc1?.name}-${loc2?.name}", loc1?.name, loc2?.name)
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
                    record.get(TradeGoodHeaders.Cost).toFloat(),
                    record.get(TradeGoodHeaders.Volume).toFloat())
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
                record.get(TradeGoodDemandHeaders.UnitsDemanded).toFloat(),
                record.get(TradeGoodDemandHeaders.DemandRate).toFloat(),
                record.get(TradeGoodDemandHeaders.CeilingPrice).toFloat(),
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
                record.get(TradeGoodSupplyHeaders.Inventory).toFloat(),
                record.get(TradeGoodSupplyHeaders.InventoryMax).toFloat(),
                record.get(TradeGoodSupplyHeaders.RestockRateBase).toFloat(),
                record.get(TradeGoodSupplyHeaders.RestockRate).toFloat(),
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
        LocationName,TradeGoodName,UnitsDemanded,DemandRate,CeilingPrice
    }

    enum class TradeGoodSupplyHeaders:CSVHeader {
        LocationName,TradeGoodName,Inventory,InventoryMax,RestockRateBase,RestockRate
    }

    /*
    fun writeOutConnections() {
        val connections = mutableListOf<Connection>()

        var nearLocation = WorldTradeMap.locations["Old Town"]!!
        var distantLocation = WorldTradeMap.locations["Scrapyard"]!!
        var connection = Connection(nearLocation.name, distantLocation.name,20.0f,Infrastructure.ROUGH, Terrain.HILLS, Weather.CLEAR, mutableListOf())
        connections.add(connection)

        nearLocation = WorldTradeMap.locations["Old Town"]!!
        distantLocation = WorldTradeMap.locations["Lucky Bend"]!!
        connection = Connection(nearLocation.name, distantLocation.name,100.0f,Infrastructure.IMPROVED, Terrain.HILLS, Weather.CLEAR, mutableListOf())
        connections.add(connection)

        nearLocation = WorldTradeMap.locations["Old Town"]!!
        distantLocation = WorldTradeMap.locations["Badlands"]!!
        connection = Connection(nearLocation.name, distantLocation.name,120.0f,Infrastructure.ROUGH, Terrain.PLAINS, Weather.CLEAR, mutableListOf())
        connections.add(connection)

        nearLocation = WorldTradeMap.locations["Lucky Bend"]!!
        distantLocation = WorldTradeMap.locations["Bluewater"]!!
        connection = Connection(nearLocation.name, distantLocation.name,120.0f,Infrastructure.IMPROVED, Terrain.PLAINS, Weather.CLEAR, mutableListOf())
        connections.add(connection)

        nearLocation = WorldTradeMap.locations["Lucky Bend"]!!
        distantLocation = WorldTradeMap.locations["Badlands"]!!
        connection = Connection(nearLocation.name, distantLocation.name,200.0f,Infrastructure.ROUGH, Terrain.PLAINS, Weather.CLEAR, mutableListOf())
        connections.add(connection)

        nearLocation = WorldTradeMap.locations["Badlands"]!!
        distantLocation = WorldTradeMap.locations["Albuquerque"]!!
        connection = Connection(nearLocation.name, distantLocation.name,100.0f,Infrastructure.TRAILS, Terrain.DESERT, Weather.HOT, mutableListOf())
        connections.add(connection)

        nearLocation = WorldTradeMap.locations["Badlands"]!!
        distantLocation = WorldTradeMap.locations["Black Rock Canyon Town"]!!
        connection = Connection(nearLocation.name, distantLocation.name,50.0f,Infrastructure.IMPROVED, Terrain.HILLS, Weather.HOT, mutableListOf())
        connections.add(connection)

        nearLocation = WorldTradeMap.locations["Black Rock Canyon Town"]!!
        distantLocation = WorldTradeMap.locations["South Albuquerque Farms"]!!
        connection = Connection(nearLocation.name, distantLocation.name,75.0f,Infrastructure.IMPROVED, Terrain.RIVERS, Weather.CLEAR, mutableListOf())
        connections.add(connection)

        nearLocation = WorldTradeMap.locations["Albuquerque"]!!
        distantLocation = WorldTradeMap.locations["South Albuquerque Farms"]!!
        connection = Connection(nearLocation.name, distantLocation.name,15.0f,Infrastructure.EFFICIENT, Terrain.PLAINS, Weather.CLEAR, mutableListOf())
        connections.add(connection)

        val jsonOutput = jsonformat.encodeToString(connections)

        val jsonFilePath = Paths.get("connections.json")
        Files.writeString(jsonFilePath,jsonOutput,StandardOpenOption.CREATE)
    }

     */
}

interface CSVHeader
