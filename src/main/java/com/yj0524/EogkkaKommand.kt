package com.yj0524

import io.github.monun.kommand.*

class EogkkaKommand : Main() {

    fun kommandLoad() {
        val greedy = KommandArgument.string(StringType.GREEDY_PHRASE)

        kommand {
            register("eogkka") {
                requires {
                    isOp
                }
                executes {
                    sender.sendMessage("§c사용법 : /eogkka <type>")
                }
                then("info") {
                    executes {
                        sender.sendMessage("Plugin Name : " + pluginMeta.name)
                        sender.sendMessage("Plugin Version : " + pluginMeta.version)
                        sender.sendMessage("Plugin API Version : " + pluginMeta.apiVersion)
                    }
                }
                then("notice") {
                    executes {
                        sender.sendMessage("§c사용법 : /eogkka notice <message>")
                    }
                    then("message" to greedy) {
                        executes {
                            val message: String by it

                            server.broadcastMessage("§a[억까 서바이벌 공지] §r" + message)
                        }
                    }
                }
                then("forcestop") {
                    executes {
                        saveConfig()
                        server.shutdown()
                    }
                }
                then("config") {
                    executes {
                        sender.sendMessage("§c사용법 : /eogkka config <type> <value>")
                    }
                    then("banmode") {
                        executes {
                            sender.sendMessage("§c사용법 : /eogkka config banmode <bool>")
                        }
                        then("bool" to bool()) {
                            executes {
                                val bool: Boolean by it

                                banMode = bool
                                sender.sendMessage("§a밴 모드를 $bool (으)로 설정했습니다.")
                            }
                        }
                    }
                    then("deathcount") {
                        executes {
                            sender.sendMessage("§c사용법 : /eogkka config deathcount <int>")
                        }
                        then("int" to int()) {
                            executes {
                                val int: Int by it

                                deathCount = int
                                sender.sendMessage("§a억까당한 횟수를 $int (으)로 설정했습니다.")
                            }
                        }
                    }
                }
            }
        }
    }
}
