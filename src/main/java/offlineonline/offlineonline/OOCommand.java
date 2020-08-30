package offlineonline.offlineonline;

import offlineonline.offlineonline.util.Msg;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class OOCommand extends Command {

        private final Main main;

        public OOCommand(Main main) {
                super("OfflineOnline", "OfflineOnline.admin", "oo");

                this.main = main;

        }

        @Override
        public void execute(CommandSender sender, String[] args) {
                if (args.length <= 0) {
                        Msg.sendHelp(sender);
                } else {
                        switch (args[0].toLowerCase()) {
                                case "disable":
                                        Main.setDisable(true);
                                        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD
                                                + Msg.PREFIX.toString() + Msg.PLUGIN_DISABLE));
                                        break;
                                case "enable":
                                        Main.setDisable(false);
                                        sender.sendMessage(
                                                TextComponent.fromLegacyText(ChatColor.GOLD + Msg.PREFIX.toString()
                                                        + Msg.PLUGIN_ENABLE));
                                        break;
                                case "add":
                                        if (args.length<=2){
                                                sender.sendMessage("Please specify an ip + at least one username to allow");
                                                break;
                                        }
                                        final String name = args[1];

                                        //add player to config
                                        new Thread (() -> {
                                                List<String> ipUser = new ArrayList<>();
                                                try {
                                                        ipUser.add(InetAddress.getByName(args[1]).getHostAddress());
                                                        ipUser.addAll(Arrays.asList(args).subList(2, args.length));
                                                        Main.getManageCache().addPlayerCache(ipUser);
                                                } catch (UnknownHostException e) {
                                                        e.printStackTrace();
                                                }
                                        }).start();
                                        List<String> domains = Main.getConfig().getStringList("allowedIPs");

                                        StringBuilder sb = new StringBuilder();
                                        for (int i = 1; args.length>i; i++){
                                                sb.append(args[i]).append(":");
                                        }
                                        sb.deleteCharAt(sb.lastIndexOf(":"));

                                        domains.add(sb.toString());

                                        Main.getConfig().set("allowedIPs", domains);
                                        Main.getInstance().saveConfig();


                                        sender.sendMessage(
                                                TextComponent.fromLegacyText(ChatColor.GOLD + Msg.PREFIX.toString()
                                                        + Msg.PLAYER_ADDED_TO_ALLOWED_CRACKED_LIST.toString()
                                                        .replace("$player", name)));
                                        break;
                                case "remove":
                                        if (args.length==1)
                                                break;
                                        final String name2 = args[1];

                                        //remove player from config
                                        Main.getManageCache().removePlayerCache(name2);
                                        List<String> domains2 = Main.getConfig().getStringList("allowedIPs");
                                        domains2.removeIf(stringList -> stringList.split(":")[0].equals(name2));
                                        Main.getConfig().set("allowedIPs", domains2);
                                        Main.getInstance().saveConfig();

                                        sender.sendMessage(
                                                TextComponent.fromLegacyText(ChatColor.GOLD + Msg.PREFIX.toString()
                                                        + Msg.PLAYER_REMOVED_FROM_ALLOWED_CRACKED_LIST.toString()
                                                        .replace("$player", name2)));
                                        break;
                                case "list":
                                        StringBuilder msg = new StringBuilder(
                                                Msg.PREFIX.toString() + Msg.LIST_ALLOWED_PLAYERS);
                                        for (List<String> pc : Main.getManageCache().playerCacheList()) {
                                                for (String pc2 : pc){
                                                        msg.append(pc2).append(':');
                                                }
                                                msg.deleteCharAt(msg.lastIndexOf(":"));
                                                msg.append(", ");
                                        }
                                        msg.delete(msg.lastIndexOf(","), msg.lastIndexOf(" "));
                                        sender.sendMessage(
                                                TextComponent.fromLegacyText(ChatColor.GOLD + msg.toString()));
                                        break;
                                case "reload":
                                        main.loadConfig();
                                        new Thread (() -> {
                                                LinkedList<List<String>> ips = new LinkedList<>();
                                                for (String domain : Main.getConfig().getStringList("allowedIPs")){
                                                        String[] splitDomain = domain.split(":");
                                                        List<String> ipUser = new ArrayList<>();
                                                        try {
                                                                if (splitDomain.length<2) {
                                                                        ipUser.add(InetAddress.getByName(splitDomain[0]).getHostAddress());
                                                                        ips.add(ipUser);
                                                                } else {
                                                                        ipUser.add(InetAddress.getByName(splitDomain[0]).getHostAddress());
                                                                        ipUser.addAll(Arrays.asList(splitDomain).subList(1, splitDomain.length));
                                                                        ips.add(ipUser);
                                                                }

                                                        } catch (UnknownHostException e) {
                                                                Main.getInstance().getLogger().warning("domain "+domain+" does not resolve");
                                                        }
                                                }
                                                Main.getManageCache().setPlayerCache(ips);

                                        }).start();
                                        sender.sendMessage(
                                                TextComponent.fromLegacyText(ChatColor.GOLD + "Config reloaded!"));

                                        break;
                                default:
                                        Msg.sendHelp(sender);
                        }
                }
        }
}