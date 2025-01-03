package dev.kuwa.protocolChanger.commands

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.command.SimpleCommand.Invocation
import dev.kuwa.protocolChanger.ProtocolChanger
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.io.IOException

class ReloadCommand(
    private val protocolChanger: ProtocolChanger
) : SimpleCommand {

    override fun execute(invocation: Invocation) {
        val source: CommandSource = invocation.source()

        try {
            protocolChanger.loadConfiguration()
        } catch (e: IOException) {
            protocolChanger.logger.error("Failed to load configuration", e)
        }

        source.sendMessage(Component.text("Reload Config!", NamedTextColor.AQUA))
    }

    // コマンドを実行する権限があるかどうかを制御するメソッド
    override fun hasPermission(invocation: Invocation): Boolean {
        return invocation.source().hasPermission("protocolchanger.reload")
    }
}