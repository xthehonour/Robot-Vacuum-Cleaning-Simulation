package com.robot.simulation.controller;

import com.robot.simulation.model.Hucre;
import com.robot.simulation.model.Izgara;

import java.util.*;

public class YolBulucu {

    public static List<Hucre> bfs(Izgara izgara, Hucre baslangic, Hucre hedef) {
        if (baslangic == null || hedef == null) return Collections.emptyList();

        Queue<Hucre> queue = new LinkedList<>();
        Map<Hucre, Hucre> parentMap = new HashMap<>();
        Set<Hucre> visited = new HashSet<>();

        queue.add(baslangic);
        visited.add(baslangic);

        while (!queue.isEmpty()) {
            Hucre current = queue.poll();
            if (current == hedef) {
                return yoluOlustur(parentMap, hedef);
            }

            for (Hucre komsu : getKomsuHucreler(izgara, current)) {
                if (!visited.contains(komsu) && !komsu.isEngel()) {
                    visited.add(komsu);
                    parentMap.put(komsu, current);
                    queue.add(komsu);
                }
            }
        }
        return Collections.emptyList();
    }

    private static List<Hucre> yoluOlustur(Map<Hucre, Hucre> parentMap, Hucre hedef) {
        List<Hucre> path = new ArrayList<>();
        Hucre current = hedef;
        while (current != null) {
            path.add(0, current);
            current = parentMap.get(current);
        }
        return path;
    }

    private static List<Hucre> getKomsuHucreler(Izgara izgara, Hucre hucre) {
        List<Hucre> komsular = new ArrayList<>();
        int x = hucre.getX();
        int y = hucre.getY();

        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        for (int[] d : directions) {
            Hucre komsu = izgara.getHucre(x + d[0], y + d[1]);
            if (komsu != null) komsular.add(komsu);
        }
        return komsular;
    }
}
