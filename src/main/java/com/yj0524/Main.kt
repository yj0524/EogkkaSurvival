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
import java.util.*

class Main : JavaPlugin(), Listener {

    override fun onEnable() {
        server.logger.info("Plugin Enabled")

        kommandLoad()

        server.pluginManager.registerEvents(this, this)
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
                        server.shutdown()
                    }
                }
            }
        }
    }

    // 플레이어가 죽었다면 죽은 플레이어의 이름을 콘솔에 출력
    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        event.deathMessage = null
        val player = event.entity
        event.player.banPlayer("§c억까를 이기지 못하고 사망했습니다.")
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
                    entity.addPotionEffect(PotionEffect(PotionEffectType.SLOW, e.damage.toInt() * 40, 0))
                }
            }
        }
    }

    // 다이아몬드를 캤을 때 10% 확률로 다이아몬드가 청금석이 됨 (청금석의 이름은 다이아몬드로 위장한 청금석)
    @EventHandler
    fun onDiamondOreBreak(event: BlockBreakEvent) {
        val block = event.block
        if (block.type == Material.DIAMOND_ORE) {
            val random = Random()
            if (random.nextDouble() < 0.1) {
                block.type = Material.LAPIS_ORE
                block.state.update()
                val item = ItemStack(Material.LAPIS_LAZULI, 1)
                val meta = item.itemMeta
                meta.setDisplayName("다이아몬드로 위장한 청금석")
                val lore = ArrayList<String>()
                lore.add("눈치가 너무 없어버린 청금석")
                item.itemMeta = meta
                meta.lore = lore
                event.player.world.dropItemNaturally(block.location, item)
            }
        }
    }

    // 철을 캤을 때 50% 확률로 철이 구리가 됨 (구리의 이름은 철로 위장한 구리)
    @EventHandler
    fun onIronOreBreak(event: BlockBreakEvent) {
        val block = event.block
        if (block.type == Material.IRON_ORE) {
            val random = Random()
            if (random.nextDouble() < 0.5) {
                block.type = Material.COPPER_ORE
                block.state.update()
                val item = ItemStack(Material.COPPER_INGOT, 1)
                val meta = item.itemMeta
                meta.setDisplayName("철로 위장한 구리")
                val lore = ArrayList<String>()
                lore.add("눈치가 너무 없어버린 구리")
                item.itemMeta = meta
                meta.lore = lore
                event.player.world.dropItemNaturally(block.location, item)
            }
        }
    }
}
