package com.rromaanoprojects.GPSPlus;

import com.rromaanoprojects.GPSPlus.GPSPlus;
import com.rromaanoprojects.GPSPlus.NavigationNode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class NavigationManager {
    private GPSPlus plugin;
    private Map<UUID, NavigationSession> activeSessions;

    public NavigationManager(GPSPlus plugin) {
        this.plugin = plugin;
        this.activeSessions = new HashMap<>();
    }

    public boolean startNavigation(Player player, String destinationId) {
        NavigationNode destination = plugin.getNodeManager().getDestination(destinationId);
        if (destination == null) {
            return false;
        }

        // Stop any existing navigation
        stopNavigation(player);

        // Find nearest node to player
        Location playerLoc = player.getLocation();
        NavigationNode startNode = plugin.getNodeManager().getNearestNode(playerLoc);

        if (startNode == null) {
            return false;
        }

        // Calculate path
        List<NavigationNode> path = calculatePath(startNode, destination);
        if (path == null || path.isEmpty()) {
            return false;
        }

        // Start navigation session
        NavigationSession session = new NavigationSession(player.getUniqueId(), path, destination);
        activeSessions.put(player.getUniqueId(), session);

        // Start the navigation task
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                updateNavigation(player);
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second

        session.setTask(task);
        return true;
    }

    public void stopNavigation(Player player) {
        NavigationSession session = activeSessions.get(player.getUniqueId());
        if (session != null) {
            session.getTask().cancel();
            activeSessions.remove(player.getUniqueId());
            player.sendMessage("§6[GPSPlus] §cNavigazione stoppata.");
        }
    }

    private List<NavigationNode> calculatePath(NavigationNode start, NavigationNode end) {
        // A* pathfinding algorithm
        Map<String, Double> gScore = new HashMap<>();
        Map<String, Double> fScore = new HashMap<>();
        Map<String, String> cameFrom = new HashMap<>();
        Set<String> openSet = new HashSet<>();
        Set<String> closedSet = new HashSet<>();

        // Initialize scores
        gScore.put(start.getId(), 0.0);
        fScore.put(start.getId(), heuristic(start, end));
        openSet.add(start.getId());

        while (!openSet.isEmpty()) {
            // Find node with lowest fScore
            String current = null;
            double lowestFScore = Double.MAX_VALUE;

            for (String nodeId : openSet) {
                double score = fScore.getOrDefault(nodeId, Double.MAX_VALUE);
                if (score < lowestFScore) {
                    lowestFScore = score;
                    current = nodeId;
                }
            }

            // If we reached the end, reconstruct path
            if (current.equals(end.getId())) {
                return reconstructPath(cameFrom, current);
            }

            // Process current node
            openSet.remove(current);
            closedSet.add(current);

            NavigationNode currentNode = plugin.getNodeManager().getNode(current);
            if (currentNode == null) continue;

            // Check all neighbors
            for (String neighborId : currentNode.getConnections()) {
                if (closedSet.contains(neighborId)) continue;

                NavigationNode neighbor = plugin.getNodeManager().getNode(neighborId);
                if (neighbor == null) continue;

                double tentativeGScore = gScore.getOrDefault(current, Double.MAX_VALUE) +
                        currentNode.distanceTo(neighbor);

                if (!openSet.contains(neighborId)) {
                    openSet.add(neighborId);
                } else if (tentativeGScore >= gScore.getOrDefault(neighborId, Double.MAX_VALUE)) {
                    continue;
                }

                // This path is the best until now
                cameFrom.put(neighborId, current);
                gScore.put(neighborId, tentativeGScore);
                fScore.put(neighborId, gScore.get(neighborId) + heuristic(neighbor, end));
            }
        }

        // No path found
        return null;
    }

    private double heuristic(NavigationNode a, NavigationNode b) {
        // Use straight-line distance as heuristic
        return a.distanceTo(b);
    }

    private List<NavigationNode> reconstructPath(Map<String, String> cameFrom, String current) {
        List<NavigationNode> path = new ArrayList<>();
        while (cameFrom.containsKey(current)) {
            NavigationNode node = plugin.getNodeManager().getNode(current);
            if (node != null) {
                path.add(0, node);
            }
            current = cameFrom.get(current);
        }

        // Add the starting node
        NavigationNode startNode = plugin.getNodeManager().getNode(current);
        if (startNode != null) {
            path.add(0, startNode);
        }

        return path;
    }

    private void updateNavigation(Player player) {
        NavigationSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;

        List<NavigationNode> path = session.getPath();
        if (path.isEmpty()) {
            stopNavigation(player);
            player.sendMessage("§6[GPSPlus] §aSei arrivato a destinazione!");
            return;
        }

        // Get next waypoint
        NavigationNode nextWaypoint = path.get(0);
        Location playerLoc = player.getLocation();
        Location waypointLoc = nextWaypoint.toBukkitLocation();

        // Check if player reached the waypoint
        double distance = playerLoc.distance(waypointLoc);
        if (distance < 5.0) { // Within 5 blocks
            path.remove(0);
            if (path.isEmpty()) {
                // Reached the final waypoint
                stopNavigation(player);
                player.sendMessage("§6[GPSPlus] §aSei arrivato a destinazione!");
                return;
            }
            // Get the new next waypoint
            nextWaypoint = path.get(0);
            waypointLoc = nextWaypoint.toBukkitLocation();
        }

        // Calculate direction to waypoint
        double dx = waypointLoc.getX() - playerLoc.getX();
        double dz = waypointLoc.getZ() - playerLoc.getZ();
        String direction = getCardinalDirection(dx, dz);

        // Send navigation message
        player.sendMessage(String.format(
                "§6[GPSPlus] §f→ Dirigiti verso %s per %.1f blocchi.",
                direction, distance
        ));
    }

    private String getCardinalDirection(double dx, double dz) {
        double angle = Math.atan2(dz, dx) * 180 / Math.PI;
        if (angle < 0) {
            angle += 360;
        }

        if (angle >= 337.5 || angle < 22.5) {
            return "est";
        } else if (angle >= 22.5 && angle < 67.5) {
            return "sud-est";
        } else if (angle >= 67.5 && angle < 112.5) {
            return "sud";
        } else if (angle >= 112.5 && angle < 157.5) {
            return "sud-ovest";
        } else if (angle >= 157.5 && angle < 202.5) {
            return "ovest";
        } else if (angle >= 202.5 && angle < 247.5) {
            return "nord-ovest";
        } else if (angle >= 247.5 && angle < 292.5) {
            return "nord";
        } else {
            return "nord-est";
        }
    }

    private static class NavigationSession {
        private UUID playerId;
        private List<NavigationNode> path;
        private NavigationNode destination;
        private BukkitTask task;

        public NavigationSession(UUID playerId, List<NavigationNode> path, NavigationNode destination) {
            this.playerId = playerId;
            this.path = path;
            this.destination = destination;
        }

        public UUID getPlayerId() {
            return playerId;
        }

        public List<NavigationNode> getPath() {
            return path;
        }

        public NavigationNode getDestination() {
            return destination;
        }

        public BukkitTask getTask() {
            return task;
        }

        public void setTask(BukkitTask task) {
            this.task = task;
        }
    }
}