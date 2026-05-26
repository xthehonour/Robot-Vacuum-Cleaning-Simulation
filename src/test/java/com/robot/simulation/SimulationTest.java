package com.robot.simulation;

import com.robot.simulation.model.*;
import com.robot.simulation.controller.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class SimulationTest {

    @Test
    public void testRobotMovement() {
        Robot robot = new Robot(0, 0, 100.0);
        robot.hareketEt(1, 1);
        assertEquals(1, robot.getX());
        assertEquals(1, robot.getY());
        assertEquals(99.0, robot.getPil());
    }

    @Test
    public void testRobotCleaning() {
        Robot robot = new Robot(0, 0, 100.0);
        robot.temizle(KirTipi.TOZ);
        assertEquals(95.0, robot.getPil());
    }

    @Test
    public void testBFSPathfinding() {
        Izgara izgara = new Izgara(5, 5);
        Hucre baslangic = izgara.getHucre(0, 0);
        Hucre hedef = izgara.getHucre(2, 2);

        List<Hucre> yol = YolBulucu.bfs(izgara, baslangic, hedef);
        assertFalse(yol.isEmpty());
        assertEquals(hedef, yol.get(yol.size() - 1));
    }
}
