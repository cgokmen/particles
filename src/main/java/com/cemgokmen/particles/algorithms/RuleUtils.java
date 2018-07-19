/*
 * Particles, a self-organizing particle system simulator.
 * Copyright (C) 2018  Cem Gokmen.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.cemgokmen.particles.algorithms;

import com.cemgokmen.particles.models.Particle;
import com.cemgokmen.particles.models.ParticleGrid;
import com.cemgokmen.particles.models.amoebot.AmoebotParticle;
import com.google.common.collect.Sets;
import org.la4j.Vector;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RuleUtils {
    protected static boolean checkProperty1(AmoebotParticle p, ParticleGrid.Direction d, Predicate<Particle> filter) {
        Particle n1 = p.getNeighborInDirection(d, 5, filter); // WE SHOULD NOT HAVE ACCESS TO THESE!
        Particle n2 = p.getNeighborInDirection(d, 1, filter);

        if (n1 != null || n2 != null) {
            List<Boolean> neighbors1 = new ArrayList<>(6);
            List<Boolean> neighbors2 = new ArrayList<>(6);

            for (int i = 0; i < 5; i++) {
                neighbors1.add(p.getNeighborInDirection(d, i + 1, filter) != null);
                neighbors2.add(p.getAdjacentPositionNeighborInDirection(d, i + 4, filter) != null);
            }

            int changes1 = 0;
            int changes2 = 0;

            for (int n = 0; n < 4; n++) {
                if (neighbors1.get(n) != neighbors1.get(n + 1)) {
                    changes1++;
                }
                if (neighbors2.get(n) != neighbors2.get(n + 1)) {
                    changes2++;
                }
            }

            return changes1 < 3 && changes2 < 3;
        }

        return false;
    }

    protected static boolean checkProperty2(AmoebotParticle p, ParticleGrid.Direction d, Predicate<Particle> filter) {
        Particle s1 = p.getNeighborInDirection(d, 5, filter);
        Particle s2 = p.getNeighborInDirection(d, 1, filter);

        if (s1 == null && s2 == null) {
            if (p.getAdjacentPositionNeighborParticles(d, false, filter).size() <= 1) {
                return false;
            }

            if (p.getNeighborInDirection(d, 2, filter) != null &&
                    p.getNeighborInDirection(d, 3, filter) == null &&
                    p.getNeighborInDirection(d, 4, filter) != null) {
                return false;
            }

            return p.getAdjacentPositionNeighborInDirection(d, 1, filter) == null ||
                    p.getAdjacentPositionNeighborInDirection(d, 0, filter) != null ||
                    p.getAdjacentPositionNeighborInDirection(d, 5, filter) == null;
        }

        return false;
    }

    protected static boolean checkParticleConnection(ParticleGrid grid, Predicate<Particle> filter) {
        return checkConnected(grid, position -> {
            boolean a = grid.isPositionValid(position);
            boolean b = grid.isPositionOccupied(position);
            return a && b;
        });
    }

    protected static boolean checkParticleHoles(ParticleGrid grid, Predicate<Particle> filter) {
        return checkConnected(grid, position -> {
            boolean a = grid.isPositionValid(position);
            boolean b = !grid.isPositionOccupied(position);
            return a && b;
        });
    }

    private static boolean checkConnected(ParticleGrid grid, Predicate<Vector> inclusionFilter) {
        int eligibleCount = 0;
        Vector start = null;

        for (Vector v: grid.getValidPositions()) {
            if (inclusionFilter.test(v)) {
                eligibleCount++;
                start = v;
            }
        }

        //System.out.println("Eligible: " + eligibleCount);

        if (eligibleCount == 0) return true;

        Set<Vector> searched = search(grid, start, inclusionFilter);
        //System.out.println("Searched: " + searched.size());

        return eligibleCount == searched.size();
    }

    private static Set<Vector> search(ParticleGrid grid, Vector start, Predicate<Vector> inclusionFilter) {
        Set<Vector> visited = Sets.newHashSet();
        Queue<Vector> queue = new LinkedList<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            Vector vertex = queue.remove();

            if (!visited.contains(vertex)) {
                visited.add(vertex);

                Set<Vector> nextUp = Arrays.<Vector>stream(grid.getAdjacentPositions(vertex)).filter(inclusionFilter).collect(Collectors.toSet());
                queue.addAll(Sets.difference(nextUp, visited));
            }
        }

        return visited;
    }
}
