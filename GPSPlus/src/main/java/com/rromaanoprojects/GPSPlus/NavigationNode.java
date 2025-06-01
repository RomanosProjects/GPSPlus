package com.rromaanoprojects.GPSPlus;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;

public class NavigationNode {
    private String id;
    private String world;
    private double x;
    private double y;
    private double z;
    private Set<String> connections;

    public NavigationNode(String id, String world, double x, double y, double z) {
        this.id = id;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.connections = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public String getWorld() {
        return world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public Set<String> getConnections() {
        return connections;
    }

    public void addConnection(String nodeId) {
        connections.add(nodeId);
    }

    public void removeConnection(String nodeId) {
        connections.remove(nodeId);
    }

    public Location toBukkitLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    public double distanceTo(NavigationNode other) {
        if (!world.equals(other.world)) {
            return Double.MAX_VALUE;
        }

        return Math.sqrt(Math.pow(x - other.x, 2) +
                Math.pow(y - other.y, 2) +
                Math.pow(z - other.z, 2));
    }
}