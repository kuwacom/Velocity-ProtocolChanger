package dev.kuwa.protocolChanger;

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyPingEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.ServerPing
import dev.kuwa.protocolChanger.commands.ReloadCommand
import org.slf4j.Logger
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path


@Plugin(
    id = "protocol-changer",
    name = "ProtocolChanger",
    version = BuildConstants.VERSION,
    description = "protocol version を変えるシンプルなplugin",
    authors = ["kuwa"]
)
class ProtocolChanger @Inject constructor(
    @DataDirectory private val dataDirectory: Path,
    val logger: Logger
) {
    private lateinit var proxy: ProxyServer

    private lateinit var configManager: ConfigManager
    private lateinit var config: PluginConfig

    @Inject
    fun ProtocolChanger(proxy: ProxyServer) {
        this.proxy = proxy
    }

    @Subscribe
    fun onProxyInitialize(event: ProxyInitializeEvent) {
        try {
            loadConfiguration()
        } catch (e: IOException) {
            logger.error("Failed to load configuration", e)
        }

        val commandManager = proxy.commandManager

        val reloadCommand = commandManager
            .metaBuilder("protocol-reload")
            .aliases("p-reload")
            .plugin(this)
            .build()

        commandManager.register(reloadCommand, ReloadCommand(this))
    }

    @Throws(IOException::class)
    fun loadConfiguration() {
        if (!Files.exists(dataDirectory)) {
            Files.createDirectories(dataDirectory)
        }
//
//        val configFile = dataDirectory.resolve("config.properties")
//        val properties = Properties()
//
//        if (Files.notExists(configFile)) {
//            properties.setProperty("protocol", "Velocity Server")
//            Files.write(configFile, propertiesToString(properties).toByteArray())
//        } else {
//            properties.load(Files.newInputStream(configFile))
//        }
//
//        protocol = properties.getProperty("protocol", "Velocity Server")

        try {
            val configPath = dataDirectory.resolve("config.toml")
            configManager = ConfigManager(configPath, logger)
            config = configManager.loadConfig()
        } catch (e: IOException) {
            logger.error("Failed to initialize config manager.", e)
        }

    }
//
//    @Subscribe(order = PostOrder.LAST)
//    fun onProxyQuery(event: ProxyQueryEvent) {
//        println(event)
//        val proxyResponse = event.response ?: return
//
////        val updatedProxyResponse  = proxyResponse.toBuilder().proxyVersion(config.protocolName)
//        proxyResponse.toBuilder().proxyVersion(config.protocolName).build()
//    }

    @Subscribe
    fun onProxyPing(event: ProxyPingEvent) {
        val ping = event.ping ?: return

        val updatedVersion = ServerPing.Version(ping.version.protocol, config.protocolName)
        val updatedPing = ping.asBuilder().version(updatedVersion).build()
        event.ping = updatedPing
    }
//
//    @Subscribe
//    fun onConnectionHandshake(event: ConnectionHandshakeEvent) {
//        println(event)
//    }
}
