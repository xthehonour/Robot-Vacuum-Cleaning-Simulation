package com.robot.simulation.controller;

import com.robot.simulation.model.GridPoint;
import com.robot.simulation.model.RoomGrid;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public final class PathFinder {
    private PathFinder() {
    }

    public static List<GridPoint> shortestPath(RoomGrid grid, GridPoint start, GridPoint target) {
        if (start.equals(target)) {
            return List.of(start);
        }

        Queue<GridPoint> queue = new ArrayDeque<>();
        Map<GridPoint, GridPoint> parents = new HashMap<>();
        Set<GridPoint> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            GridPoint current = queue.poll();
            for (GridPoint neighbor : grid.neighbors(current)) {
                if (visited.contains(neighbor)) {
                    continue;
                }
                visited.add(neighbor);
                parents.put(neighbor, current);
                if (neighbor.equals(target)) {
                    return rebuildPath(parents, target);
                }
                queue.add(neighbor);
            }
        }
        return Collections.emptyList();
    }

    private static List<GridPoint> rebuildPath(Map<GridPoint, GridPoint> parents, GridPoint target) {
        List<GridPoint> path = new ArrayList<>();
        GridPoint current = target;
        while (current != null) {
            path.add(0, current);
            current = parents.get(current);
        }
        return path;
    }
}
