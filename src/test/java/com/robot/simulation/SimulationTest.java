package com.robot.simulation;

import com.robot.simulation.controller.PathFinder;
import com.robot.simulation.model.CleaningAlgorithm;
import com.robot.simulation.model.DirtType;
import com.robot.simulation.model.GridPoint;
import com.robot.simulation.model.RobotVacuum;
import com.robot.simulation.model.RoomGrid;
import com.robot.simulation.model.VacuumSimulation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimulationTest {

    @Test
    public void testRobotMovement() {
        RobotVacuum robot = new RobotVacuum(new GridPoint(0, 0));
        robot.moveTo(new GridPoint(1, 0));
        robot.consume(1.1);
        assertEquals(new GridPoint(1, 0), robot.getPosition());
        assertEquals(98.9, robot.getBatteryLevel(), 0.001);
    }

    @Test
    public void testRobotCleaning() {
        VacuumSimulation simulation = new VacuumSimulation();
        simulation.addDirt(new GridPoint(1, 12), DirtType.DUST);
        simulation.setAlgorithm(CleaningAlgorithm.SMART);
        simulation.start();
        for (int i = 0; i < 12; i++) {
            simulation.tick();
        }
        assertTrue(simulation.getRoomGrid().cell(1, 12).isVisited());
    }

    @Test
    public void testBFSPathfinding() {
        RoomGrid grid = new RoomGrid(5, 5);
        grid.addObstacleRect(1, 0, 1, 3);
        List<GridPoint> path = PathFinder.shortestPath(grid, new GridPoint(0, 0), new GridPoint(2, 2));
        assertFalse(path.isEmpty());
        assertEquals(new GridPoint(2, 2), path.get(path.size() - 1));
    }
}
