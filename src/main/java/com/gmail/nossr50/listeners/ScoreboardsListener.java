package com.gmail.nossr50.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.events.experience.McMMOPlayerLevelUpEvent;
import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;
import com.gmail.nossr50.util.scoreboards.ScoreboardManager;

public class ScoreboardsListener implements Listener {
    @SuppressWarnings("unused") // XXX remove this once you use it
    private final mcMMO plugin;

    public ScoreboardsListener(final mcMMO plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        ScoreboardManager.setupPlayer(e.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        ScoreboardManager.teardownPlayer(e.getPlayer());
    }

    @EventHandler
    public void onPlayerLevelUp(McMMOPlayerLevelUpEvent e) {
        ScoreboardManager.handleLevelUp(e.getPlayer(), e.getSkill());
    }

    @EventHandler
    public void onPlayerXp(McMMOPlayerXpGainEvent e) {
        ScoreboardManager.handleXp(e.getPlayer(), e.getSkill());
    }
}