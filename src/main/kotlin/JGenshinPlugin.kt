package top.jie65535.jgs

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.utils.info
import java.io.File

object JGenshinPlugin : KotlinPlugin(
    JvmPluginDescription(
        id = "top.jie65535.mirai-console-jgs-plugin",
        name = "J Genshin Plugin",
        version = "0.2.0"
    ) {
        author("jie65535")
        info("原神查询插件")
    }
) {
    private val handbook = mutableMapOf<String, Int>()
    private val handbookPath = resolveConfigPath("Handbook.txt")

    private fun getHandFile(): File {
        val f = handbookPath.toFile()
        if (!f.exists() && f.createNewFile()) {
            this::class.java.getResourceAsStream("/Handbook.txt")?.copyTo(f.outputStream())
        }
        return f
    }

    override fun onEnable() {

        loadHandbook(getHandFile())

        val eventChannel = GlobalEventChannel.parentScope(this)
        val findCommand = "id"
        eventChannel.subscribeMessages {
            startsWith(findCommand) { arg ->
                val keyword = arg.trim()
                if (keyword.isEmpty()) {
                    return@startsWith
                }
                val id = handbook[keyword]
                val msg = if (id != null) {
                    "$keyword : $id"
                } else {
                    handbook.keys.asSequence()
                        .filter { it.contains(keyword) }
                        .map { "$it : ${handbook[it]}" }
                        .take(10)
                        .joinToString("\n")
                }
                if (msg.isNotEmpty()) {
                    subject.sendMessage(msg)
                }
            }
        }

        logger.info { "${handbook.size} 词条已加载，若要更新ID文件，请替换$handbookPath" }
    }

    private fun loadHandbook(file: File) {
        file.bufferedReader().use {
            while (it.ready()) {
                val line = it.readLine()
                val s = line.indexOf(':')
                if (s > 0) {
                    val id = line.substring(0, s).trim().toInt()
                    val name = line.substring(s+1).trim()
                    if (!handbook.containsKey(name))
                        handbook[name] = id
                }
            }
        }
    }
}
