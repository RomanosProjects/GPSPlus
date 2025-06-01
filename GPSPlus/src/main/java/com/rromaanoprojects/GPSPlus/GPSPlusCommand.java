package com.rromaanoprojects.GPSPlus;

import com.rromaanoprojects.GPSPlus.GPSPlus;
import com.rromaanoprojects.GPSPlus.NavigationNode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GPSPlusCommand implements CommandExecutor, TabCompleter {
    private GPSPlus plugin;

    public GPSPlusCommand(GPSPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cQuesto comando può essere eseguito solo da player.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "createnode":
                if (!player.hasPermission("gpsplus.admin")) {
                    player.sendMessage("§cNon hai il permesso di utilizzare questo comando.");
                    return true;
                }

                if (args.length < 2) {
                    player.sendMessage("§cUsage: /gpsplus createnode <id>");
                    return true;
                }

                String nodeId = args[1];
                boolean created = plugin.getNodeManager().createNode(nodeId, player.getLocation());

                if (created) {
                    player.sendMessage("§6[GPSPlus] §aNodo '" + nodeId + "' creato.");
                } else {
                    player.sendMessage("§6[GPSPlus] §cÈ già esistente un nodo con quell'id.");
                }
                break;

            case "createdest":
                if (!player.hasPermission("gpsplus.admin")) {
                    player.sendMessage("§cNon hai il permesso di utilizzare questo comando.");
                    return true;
                }

                if (args.length < 2) {
                    player.sendMessage("§cUsage: /gpsplus createdest <name>");
                    return true;
                }

                String destName = args[1];
                boolean destCreated = plugin.getNodeManager().createDestination(destName, player.getLocation());

                if (destCreated) {
                    player.sendMessage("§6[GPSPlus] §aDestinazione '" + destName + "' creata.");
                } else {
                    player.sendMessage("§6[GPSPlus] §cÈ già esistente una destinazione con quell'id..");
                }
                break;

            case "connect":
                if (!player.hasPermission("gpsplus.admin")) {
                    player.sendMessage("§cNon hai il permesso di utilizzare questo comando.");
                    return true;
                }

                if (args.length < 3) {
                    player.sendMessage("§cUtilizzo: /gpsplus connect <nodeA> <nodeB>");
                    return true;
                }

                String nodeA = args[1];
                String nodeB = args[2];
                boolean connected = plugin.getNodeManager().connectNodes(nodeA, nodeB);

                if (connected) {
                    player.sendMessage("§6[GPSPlus] §aNodi connessi.");
                } else {
                    player.sendMessage("§6[GPSPlus] §cUno o entrambi i nodi non esistono.");
                }
                break;

            case "goto":
                if (args.length < 2) {
                    player.sendMessage("§cUtilizzo: /gpsplus goto <destination>");
                    return true;
                }

                String destination = args[1];
                boolean started = plugin.getNavigationManager().startNavigation(player, destination);

                if (started) {
                    player.sendMessage("§6[GPSPlus] §aNavigazione verso '" + destination + "' iniziata.");
                } else {
                    player.sendMessage("§6[GPSPlus] §cImpossibile navigare a quella destinazione. Assicurati che esista e che ci sia un sentiero valido");
                }
                break;

            case "stop":
                plugin.getNavigationManager().stopNavigation(player);
                break;

            case "deletenode":
                if (!player.hasPermission("gpsplus.admin")) {
                    player.sendMessage("§cNon hai il permesso di utilizzare questo comando.");
                    return true;
                }

                if (args.length < 2) {
                    player.sendMessage("§cUtilizzo: /gpsplus deletenode <id>");
                    return true;
                }

                String nodeToDelete = args[1];
                boolean deleted = plugin.getNodeManager().deleteNode(nodeToDelete);

                if (deleted) {
                    player.sendMessage("§6[GPSPlus] §aNodo '" + nodeToDelete + "' cancellato.");
                } else {
                    player.sendMessage("§6[GPSPlus] §cIl nodo non esiste.");
                }
                break;

            case "deletedest":
                if (!player.hasPermission("gpsplus.admin")) {
                    player.sendMessage("§cNon hai il permesso di utilizzare questo comando.");
                    return true;
                }

                if (args.length < 2) {
                    player.sendMessage("§cUtilizzo: /gpsplus deletedest <name>");
                    return true;
                }

                String destToDelete = args[1];
                boolean destDeleted = plugin.getNodeManager().deleteDestination(destToDelete);

                if (destDeleted) {
                    player.sendMessage("§6[GPSPlus] §aDestinazione '" + destToDelete + "' cancellata.");
                } else {
                    player.sendMessage("§6[GPSPlus] §cLa destinazione non esiste.");
                }
                break;

            case "listall":
                if (!player.hasPermission("gpsplus.admin")) {
                    player.sendMessage("§cNon hai il permesso di eseguire questo comando.");
                    return true;
                }

                player.sendMessage("§6[GPSPlus] §fNodi:");
                for (NavigationNode node : plugin.getNodeManager().getAllNodes()) {
                    // Create clickable delete button using TextComponent
                    TextComponent message = new TextComponent("§7- §f" + node.getId() + " §7(" +
                            node.getX() + ", " + node.getY() + ", " + node.getZ() + ") ");

                    TextComponent deleteButton = new TextComponent("§c[x]");
                    deleteButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gpsplus deletenode " + node.getId()));
                    deleteButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("Clicca qui per cancellare il nodo").create()));

                    message.addExtra(deleteButton);
                    player.spigot().sendMessage(message);

                }

                player.sendMessage("§6[GPSPlus] §fDestinations:");
                for (NavigationNode destNode : plugin.getNodeManager().getAllDestinations()) {
                    // Create clickable delete button using TextComponent
                    TextComponent message = new TextComponent("§7- §f" + destNode.getId() + " §7(" +
                            destNode.getX() + ", " + destNode.getY() + ", " + destNode.getZ() + ") ");

                    TextComponent deleteButton = new TextComponent("§c[x]");
                    deleteButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gpsplus deletedest " + destNode.getId()));
                    deleteButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("Click to delete this destination").create()));

                    message.addExtra(deleteButton);
                    player.spigot().sendMessage(message);
                }

                player.sendMessage("§6[GPSPlus] §fConnessioni:");
                for (NavigationNode node : plugin.getNodeManager().getAllNodes()) {
                    for (String connection : node.getConnections()) {
                        player.sendMessage(String.format(
                                "§6[Connection] §a%s §7<--> §a%s",
                                node.getId(),
                                connection
                        ));
                    }
                }
                break;

            case "about":
                sendAboutMessage(player);
                break;

            default:
                sendHelpMessage(player);
                break;
        }

        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage("§6=== GPSPlus Commands ===");

        if (player.hasPermission("gpsplus.admin")) {
            player.sendMessage("§f/gpsplus createnode <id> §7- Crea un nuovo nodo alla tua posizione.");
            player.sendMessage("§f/gpsplus createdest <name> §7- Crea una nuova destinazione alla tua posizione.");
            player.sendMessage("§f/gpsplus connect <nodeA> <nodeB> §7- Collega due nodi.");
            player.sendMessage("§f/gpsplus deletenode <id> §7- Cancella un nodo.");
            player.sendMessage("§f/gpsplus deletedest <name> §7- Cancella una destinazione.");
            player.sendMessage("§f/gpsplus listall §7- Lista di tutti i nodi, tutte le destinazioni e tutti i collegamenti.");
        }

        player.sendMessage("§f/gpsplus goto <destination> §7- Naviga verso una destinazione.");
        player.sendMessage("§f/gpsplus stop §7- Stoppa la navigazione.");
        player.sendMessage("§f/gpsplus about §7- Impara come utilizzare il plugin.");
    }

    private void sendAboutMessage(Player player) {
        player.sendMessage("§6=== GPSPlus - Sistema di navigazione ===");
        player.sendMessage("§fGPSPlus è un sistema di navigazione pensato per i server RP, firmato @rromaanoprojects.");
        player.sendMessage("§f");
        player.sendMessage("§fCome funziona:");
        player.sendMessage("§71. Un amministratore crea una rete di nodi per la mappa");
        player.sendMessage("§72. I nodi vengono collegati per creare delle strade percorribili");
        player.sendMessage("§73. Per creare una destinazione serve sia un nodo che una destinazione con lo stesso nome");
        player.sendMessage("§74. Gli utenti possono navigare verso le destinazioni '/gpsplus goto <name>'");
        player.sendMessage("§74. Per fermare la navigazione possono utilizzare '/gpsplus stop'");
        player.sendMessage("§75. Il sistema calcola il sentiero ottimale e guida l'utente");
        player.sendMessage("§f");
        if (player.hasPermission("gpsplus.admin")) {
            player.sendMessage("§fPer gli admin:");
            player.sendMessage("§7- Crea nodi con '/gpsplus createnode <id>'");
            player.sendMessage("§7- Collega nodes con '/gpsplus connect <nodeA> <nodeB>'");
            player.sendMessage("§7- Crea destinazioni con '/gpsplus createdest <name>'");
            player.sendMessage("§7- Usa '/gpsplus listall' per vedere tutti i nodi, le destinazioni e i collegamenti tra nodi");
            player.sendMessage("§7- Clicca il bottone §c[x]§7 affianco a un nodo/destinazione per cancellarlo");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        Player player = (Player) sender;

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();

            if (player.hasPermission("gpsplus.admin")) {
                completions.addAll(Arrays.asList("createnode", "createdest", "connect", "deletenode", "deletedest", "listall"));
            }

            completions.addAll(Arrays.asList("goto", "stop", "about"));

            return completions.stream()
                    .filter(c -> c.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("goto")) {
                // Tab complete for destinations
                return plugin.getNodeManager().getAllDestinations().stream()
                        .map(NavigationNode::getId)
                        .filter(id -> id.startsWith(args[1]))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("connect") && player.hasPermission("gpsplus.admin")) {
                // Tab complete for first node
                return plugin.getNodeManager().getAllNodes().stream()
                        .map(NavigationNode::getId)
                        .filter(id -> id.startsWith(args[1]))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("deletenode") && player.hasPermission("gpsplus.admin")) {
                // Tab complete for node to delete
                return plugin.getNodeManager().getAllNodes().stream()
                        .map(NavigationNode::getId)
                        .filter(id -> id.startsWith(args[1]))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("deletedest") && player.hasPermission("gpsplus.admin")) {
                // Tab complete for destination to delete
                return plugin.getNodeManager().getAllDestinations().stream()
                        .map(NavigationNode::getId)
                        .filter(id -> id.startsWith(args[1]))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("connect") && player.hasPermission("gpsplus.admin")) {
                // Tab complete for second node
                return plugin.getNodeManager().getAllNodes().stream()
                        .map(NavigationNode::getId)
                        .filter(id -> id.startsWith(args[2]))
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }
}