package com.rromaanoprojects.GPSPlus;

import com.rromaanoprojects.GPSPlus.GPSPlus;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class NodeManager {
    private GPSPlus plugin;
    private Map<String, NavigationNode> nodes;
    private Map<String, NavigationNode> destinations;

    public NodeManager(GPSPlus plugin) {
        this.plugin = plugin;
        this.nodes = new HashMap<>();
        this.destinations = new HashMap<>();
        loadNodes();
    }

    private void loadNodes() {
        FileConfiguration config = plugin.getNodesConfig();

        // Load regular nodes
        ConfigurationSection nodesSection = config.getConfigurationSection("nodes");
        if (nodesSection != null) {
            for (String nodeId : nodesSection.getKeys(false)) {
                ConfigurationSection nodeSection = nodesSection.getConfigurationSection(nodeId);
                if (nodeSection != null) {
                    double x = nodeSection.getDouble("x");
                    double y = nodeSection.getDouble("y");
                    double z = nodeSection.getDouble("z");
                    String world = nodeSection.getString("world");

                    NavigationNode node = new NavigationNode(nodeId, world, x, y, z);

                    // Load connections
                    List<String> connections = nodeSection.getStringList("connections");
                    for (String connection : connections) {
                        node.addConnection(connection);
                    }

                    nodes.put(nodeId, node);
                }
            }
        }

        // Load destinations
        ConfigurationSection destSection = config.getConfigurationSection("destinations");
        if (destSection != null) {
            for (String destId : destSection.getKeys(false)) {
                ConfigurationSection nodeSection = destSection.getConfigurationSection(destId);
                if (nodeSection != null) {
                    double x = nodeSection.getDouble("x");
                    double y = nodeSection.getDouble("y");
                    double z = nodeSection.getDouble("z");
                    String world = nodeSection.getString("world");

                    NavigationNode dest = new NavigationNode(destId, world, x, y, z);
                    destinations.put(destId, dest);
                }
            }
        }

        // Make sure connections are bidirectional
        validateConnections();
    }

    private void validateConnections() {
        for (NavigationNode node : nodes.values()) {
            for (String connectedId : node.getConnections()) {
                NavigationNode connectedNode = nodes.get(connectedId);
                if (connectedNode != null && !connectedNode.getConnections().contains(node.getId())) {
                    connectedNode.addConnection(node.getId());
                }
            }
        }
    }

    public void saveNodes() {
        FileConfiguration config = plugin.getNodesConfig();

        // Clear existing sections
        config.set("nodes", null);
        config.set("destinations", null);

        // Save nodes
        for (NavigationNode node : nodes.values()) {
            String path = "nodes." + node.getId();
            config.set(path + ".x", node.getX());
            config.set(path + ".y", node.getY());
            config.set(path + ".z", node.getZ());
            config.set(path + ".world", node.getWorld());
            config.set(path + ".connections", new ArrayList<>(node.getConnections()));
        }

        // Save destinations
        for (NavigationNode dest : destinations.values()) {
            String path = "destinations." + dest.getId();
            config.set(path + ".x", dest.getX());
            config.set(path + ".y", dest.getY());
            config.set(path + ".z", dest.getZ());
            config.set(path + ".world", dest.getWorld());
        }

        plugin.saveNodesConfig();
    }

    public boolean createNode(String id, Location location) {
        if (nodes.containsKey(id)) {
            return false;
        }

        NavigationNode node = new NavigationNode(id,
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ());
        nodes.put(id, node);
        saveNodes();
        return true;
    }

    public boolean createDestination(String id, Location location) {
        if (destinations.containsKey(id)) {
            return false;
        }

        NavigationNode dest = new NavigationNode(id,
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ());
        destinations.put(id, dest);
        saveNodes();
        return true;
    }

    public boolean connectNodes(String nodeA, String nodeB) {
        NavigationNode a = nodes.get(nodeA);
        NavigationNode b = nodes.get(nodeB);

        if (a == null || b == null) {
            return false;
        }

        a.addConnection(nodeB);
        b.addConnection(nodeA);
        saveNodes();
        return true;
    }

    public boolean deleteNode(String id) {
        NavigationNode node = nodes.get(id);
        if (node == null) {
            return false;
        }

        // Remove this node from all connections
        for (NavigationNode otherNode : nodes.values()) {
            otherNode.removeConnection(id);
        }

        // Remove the node itself
        nodes.remove(id);
        saveNodes();
        return true;
    }

    public boolean deleteDestination(String id) {
        NavigationNode dest = destinations.get(id);
        if (dest == null) {
            return false;
        }

        destinations.remove(id);
        saveNodes();
        return true;
    }

    public NavigationNode getNearestNode(Location location) {
        NavigationNode nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (NavigationNode node : nodes.values()) {
            if (!node.getWorld().equals(location.getWorld().getName())) {
                continue;
            }

            double distance = Math.pow(node.getX() - location.getX(), 2) +
                    Math.pow(node.getY() - location.getY(), 2) +
                    Math.pow(node.getZ() - location.getZ(), 2);

            if (distance < minDistance) {
                minDistance = distance;
                nearest = node;
            }
        }

        return nearest;
    }

    public NavigationNode getNode(String id) {
        return nodes.get(id);
    }

    public NavigationNode getDestination(String id) {
        return destinations.get(id);
    }

    public Collection<NavigationNode> getAllNodes() {
        return nodes.values();
    }

    public Collection<NavigationNode> getAllDestinations() {
        return destinations.values();
    }
}