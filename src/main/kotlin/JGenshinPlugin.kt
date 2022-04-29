package top.jie65535.jgs

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.utils.info

object JGenshinPlugin : KotlinPlugin(
    JvmPluginDescription(
        id = "top.jie65535.mirai-console-jgs-plugin",
        name = "J Genshin Plugin",
        version = "0.1.0"
    ) {
        author("jie65535")
        info("原神查询插件")
    }
) {
    private val handbook = mutableMapOf<String, Int>()

    override fun onEnable() {
        logger.info { "Plugin loaded" }

        loadHandbook()

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
    }

    private fun loadHandbook() {
        val stream = this::class.java.getResourceAsStream("/Handbook.txt")?.bufferedReader()
        if (stream == null) {
            logger.error("资源文件为空")
        } else {
            while (stream.ready()) {
                val line = stream.readLine()
                val s = line.indexOf(':')
                if (s > 0) {
                    val id = line.substring(0, s-1).toInt()
                    val name = line.substring(s+2)
                    if (!handbook.containsKey(name))
                        handbook[name] = id
                }
            }
            stream.close()
        }
    }
}
