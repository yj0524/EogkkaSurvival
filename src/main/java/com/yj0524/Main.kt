package com.yj0524

import io.github.monun.kommand.*
import org.bukkit.Material
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.io.File
import java.util.*

open class Main : JavaPlugin(), Listener {

    var banMode = false
    var deathCount = 0

    override fun onEnable() {
        server.logger.info("Plugin Enabled")

        // Config.yml 파일 생성
        loadConfig()
        val cfile = File(dataFolder, "config.yml")
        if (cfile.length() == 0L) {
            config.options().copyDefaults(true)
            saveConfig()
        }

        kommandLoad()

        server.pluginManager.registerEvents(this, this)

        if (!banMode) {
            server.scheduler.scheduleSyncRepeatingTask(this, {
                for (player in server.onlinePlayers) {
                    player.sendActionBar("억까당한 횟수 : $deathCount" + "번")
                }
            }, 0, 1)
        }
    }

    override fun onDisable() {
        server.logger.info("Plugin Disabled")
    }

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
                        sender.sendMessage("§c사용법 : /eogkka config <type> [value]")
                    }
                    then("banmode") {
                        executes {
                            sender.sendMessage("밴 모드 : $banMode")
                        }
                        then("bool" to bool()) {
                            executes {
                                val bool: Boolean by it

                                banMode = bool
                                config.set("banMode", bool)
                                saveConfig()
                                sender.sendMessage("§a밴 모드를 $bool (으)로 설정했습니다.")
                            }
                        }
                    }
                    then("deathcount") {
                        executes {
                            sender.sendMessage("억까당한 횟수 : $deathCount" + "번")
                        }
                        then("int" to int()) {
                            executes {
                                val int: Int by it

                                deathCount = int
                                config.set("deathCount", int)
                                saveConfig()
                                sender.sendMessage("§a억까당한 횟수를 $int" + "번 (으)로 설정했습니다.")
                            }
                        }
                    }
                }
            }
        }
    }

    fun loadConfig() {
        // Load config
        val config = config
        banMode = config.getBoolean("banMode", false)
        deathCount = config.getInt("deathCount", 0)
        // Save config
        config.set("banMode", banMode)
        config.set("deathCount", deathCount)
        saveConfig()
    }

    // 플레이어가 죽었다면 죽은 플레이어의 이름을 출력
    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        event.deathMessage = null
        val player = event.entity
        if (banMode) {
            event.player.banPlayer("§c억까를 이기지 못하고 사망했습니다.")
        } else {
            deathCount++
        }
        server.broadcastMessage("§c" + player.name + "이(가) 억까를 이기지 못하고 사망했습니다.")
    }

    // 플레이어가 몹을 때리면 그 몹에게 준 대미지를 그대로 돌려받고, 몬스터에게 공격당하면 원래의 대미지에 2배를 받음
    @EventHandler
    fun onEntityDamageByEntity(e: EntityDamageByEntityEvent) {
        val dmgr = e.damager
        val d = e.finalDamage

        fun ducheon() {
            if (dmgr is Player) {
                dmgr.damage(d)
            }

            if (dmgr is Projectile) {
                if (dmgr.shooter is Player) {
                    (dmgr.shooter as Player).damage(d)
                }
            }
        }

        ducheon()

        if (dmgr is Monster) {
            e.damage = e.damage * 2
        }
    }

    // 낙하 대미지를 받았을 때 10%의 확률로 구속 1을 받고, 낙하 대미지 높이 * 60 틱동안 구속 효과를 받음
    @EventHandler
    fun onEntityDamage(e: EntityDamageEvent) {
        val entity = e.entity
        val random = Random()
        if (e.cause == EntityDamageEvent.DamageCause.FALL) {
            if (random.nextDouble() < 0.1) {
                if (entity is Player) {
                    entity.addPotionEffect(PotionEffect(PotionEffectType.SLOW, e.damage.toInt() * 60, 0))
                }
            }
        }
    }

    // 다이아몬드 광석을 캤을 때 10% 확률로 다이아몬드가 청금석이 됨 (청금석의 이름은 다이아몬드로 위장한 청금석)
    @EventHandler
    fun onDiamondOreBreak(event: BlockBreakEvent) {
        val block = event.block
        if (block.type == Material.DIAMOND_ORE) {
            val random = Random()
            if (random.nextDouble() < 0.1) {
                event.isCancelled = true
                val item = ItemStack(Material.LAPIS_LAZULI)
                val meta = item.itemMeta
                meta.setDisplayName("§r다이아몬드로 위장한 청금석")
                item.itemMeta = meta
                event.player.world.dropItemNaturally(block.location, item)
                block.type = Material.AIR
            }
        }
    }

    // 철 광석을 캤을 때 20% 확률로 철 원석이 구리 원석이 됨 (구리 원석의 이름은 철 원석으로 위장한 구리 원석)
    @EventHandler
    fun onIronOreBreak(event: BlockBreakEvent) {
        val block = event.block
        if (block.type == Material.IRON_ORE) {
            val random = Random()
            if (random.nextDouble() < 0.2) {
                event.isCancelled = true
                val item = ItemStack(Material.RAW_COPPER)
                val meta = item.itemMeta
                meta.setDisplayName("§r철 원석으로 위장한 구리 원석")
                item.itemMeta = meta
                event.player.world.dropItemNaturally(block.location, item)
                block.type = Material.AIR
            }
        }
    }
}
