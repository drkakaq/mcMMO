package com.gmail.nossr50.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.runnables.commands.McrankCommandAsyncTask;
import com.gmail.nossr50.runnables.commands.MctopCommandAsyncTask;
import com.gmail.nossr50.util.Misc;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.util.StringUtils;
import com.gmail.nossr50.util.commands.CommandUtils;
import com.gmail.nossr50.util.player.UserManager;
import com.gmail.nossr50.util.scoreboards.ScoreboardManager;
import com.gmail.nossr50.util.skills.SkillUtils;
import com.google.common.collect.ImmutableList;

public class McscoreboardCommand implements TabExecutor {
    private static final List<String> FIRST_ARGS = ImmutableList.of("clear", "reset", "keep", "show");
    private static final List<String> BOARD_TYPES = ImmutableList.of("top", "rank", "stats", "skill");
    private static final String help = "" // TODO put into locale file, add colors
            + "== Help for /mcscoreboard ==\n"
            + "/mcscoreboard clear - clear the mcmmo sidebar scoreboard\n"
            + "/mcscoreboard keep - prevent the mcmmo sidebar from being automatically cleared\n"
            + "/mcscoreboard show rank [playername] - show someone's rankings in the scoreboard\n"
            + "/mcscoreboard show skill <skillname> - show a skill scoreboard\n"
            + "/mcscoreboard show stats [playername] - show your skill levels in the scoreboard\n"
            + "/mcscoreboard show top [skillname] [page] - show the leaderboards in the scoreboard\n";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (CommandUtils.noConsoleUsage(sender)) {
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(help.split("\n"));
            return true;
        }

        if (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("reset")) {
            ScoreboardManager.clearBoard(sender.getName());
            sender.sendMessage("mcMMO scoreboard removed."); // TODO better message, locale
        }
        else if (args[0].equalsIgnoreCase("keep")) {
            ScoreboardManager.keepBoard(sender.getName());
        }
        else if (args[0].equalsIgnoreCase("show")) {
            if (args.length == 1) {
                sender.sendMessage(help.split("\n"));
                return true;
            }

            McMMOPlayer mcpl = UserManager.getPlayer(sender.getName());
            if (mcpl.getDatabaseATS() + Misc.PLAYER_DATABASE_COOLDOWN_MILLIS > System.currentTimeMillis()) {
                sender.sendMessage(LocaleLoader.getString("Commands.Database.Cooldown"));
                return true;
            }
            mcpl.actualizeDatabaseATS();

            if (args[1].equalsIgnoreCase("rank")) {
                if (args.length == 2) {
                    new McrankCommandAsyncTask(sender.getName(), sender, true, false).runTaskAsynchronously(mcMMO.p);
                }
                else {
                    new McrankCommandAsyncTask(args[2], sender, true, false).runTaskAsynchronously(mcMMO.p);
                }
            }
            else if (args[1].equalsIgnoreCase("top")) {
                if (args.length == 2) {
                    new MctopCommandAsyncTask(1, null, sender, true, false).runTaskAsynchronously(mcMMO.p);
                }
                else if (args.length == 3) {
                    if (StringUtils.isInt(args[2])) {
                        new MctopCommandAsyncTask(Math.abs(Integer.parseInt(args[2])), null, sender, true, false).runTaskAsynchronously(mcMMO.p);
                    }
                    else {
                        if (CommandUtils.isInvalidSkill(sender, args[2])) {
                            return true;
                        }
                        SkillType skill = SkillType.getSkill(args[2]);
                        if (CommandUtils.isChildSkill(sender, skill)) {
                            return true;
                        }
                        if (!Permissions.mctop(sender, skill)) {
                            sender.sendMessage(command.getPermissionMessage());
                            return true;
                        }
                        new MctopCommandAsyncTask(1, skill, sender, true, false).runTaskAsynchronously(mcMMO.p);
                    }
                }
                else if (args.length == 4) {
                    if (CommandUtils.isInvalidInteger(sender, args[3])) {
                        return true;
                    }
                    if (CommandUtils.isInvalidSkill(sender, args[2])) {
                        return true;
                    }
                    SkillType skill = SkillType.getSkill(args[2]);
                    if (CommandUtils.isChildSkill(sender, skill)) {
                        return true;
                    }
                    if (!Permissions.mctop(sender, skill)) {
                        sender.sendMessage(command.getPermissionMessage());
                        return true;
                    }
                    new MctopCommandAsyncTask(Math.abs(Integer.parseInt(args[3])), skill, sender, true, false).runTaskAsynchronously(mcMMO.p);
                }
            }
            else if (args[1].equalsIgnoreCase("skill")) {
                if (CommandUtils.isInvalidSkill(sender, args[2])) {
                    return true;
                }
                SkillType skill = SkillType.getSkill(args[2]);
                ScoreboardManager.enablePlayerSkillScoreboard(mcpl, skill);
            }
            else if (args[1].equalsIgnoreCase("stats")) {
                if (args.length == 2) {
                    ScoreboardManager.enablePlayerStatsScoreboard(mcpl);
                }
                else {
                    String playerName = Misc.getMatchedPlayerName(args[2]);
                    McMMOPlayer mcMMOPlayer = UserManager.getPlayer(playerName, true);
                    if (mcMMOPlayer == null) {
                        // Offline
                        PlayerProfile profile = mcMMO.getDatabaseManager().loadPlayerProfile(playerName, false); // Temporary Profile

                        if (CommandUtils.inspectOffline(sender, profile, Permissions.inspectOffline(sender))) {
                            return true;
                        }

                        ScoreboardManager.enablePlayerInspectScoreboard((Player) sender, profile);
                    }
                    else {
                        // Online, so do hidden checks
                        Player target = mcMMOPlayer.getPlayer();
                        if (CommandUtils.hidden(sender, target, Permissions.inspectHidden(sender))) {
                            if (!Permissions.inspectOffline(sender)) {
                                sender.sendMessage(LocaleLoader.getString("Inspect.Offline"));
                                return true;
                            }
                        }
                        else if (CommandUtils.tooFar(sender, target, Permissions.inspectFar(sender))) {
                            return true;
                        }

                        ScoreboardManager.enablePlayerInspectScoreboard((Player) sender, mcMMOPlayer.getProfile());
                    }
                }
            }
            else {
                sender.sendMessage("Not a valid scoreboard type");
            }
        }
        else {
            sender.sendMessage(help.split("\n"));
            return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        switch (args.length) {
            case 1:
                return StringUtil.copyPartialMatches(args[0], FIRST_ARGS, new ArrayList<String>(FIRST_ARGS.size()));
            case 2:
                if (args[0].equalsIgnoreCase("show")) {
                    return StringUtil.copyPartialMatches(args[1], BOARD_TYPES, new ArrayList<String>(BOARD_TYPES.size()));
                }
                break;
            case 3:
                if (!args[0].equalsIgnoreCase("show")) {
                    return ImmutableList.of();
                }
                if (args[1].equalsIgnoreCase("top") || args[1].equalsIgnoreCase("skill")) {
                    return StringUtil.copyPartialMatches(args[2], SkillType.SKILL_NAMES, new ArrayList<String>(SkillType.SKILL_NAMES.size()));
                }
                if (args[1].equalsIgnoreCase("rank") || args[1].equalsIgnoreCase("stats")) {
                    Set<String> playerNames = UserManager.getPlayerNames();
                    playerNames.add("me");
                    return StringUtil.copyPartialMatches(args[2], playerNames, new ArrayList<String>(playerNames.size()));
                }
                break;
            case 4:
                if (args[0].equalsIgnoreCase("show") && args[1].equalsIgnoreCase("top")) {
                    return ImmutableList.of("1", "2", "3", "4", "5");
                }
                break;
            default:
                break;
        }
        return ImmutableList.of();
    }
}
