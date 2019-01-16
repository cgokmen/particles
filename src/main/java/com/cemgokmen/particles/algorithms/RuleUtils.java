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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RuleUtils {
    protected static boolean isMoveValidCompressionMove(AmoebotParticle p, ParticleGrid.Direction d, boolean swapsAllowed, boolean nonswapsAllowed) {
        return isMoveValidCompressionMove(p, d, swapsAllowed, nonswapsAllowed, null);
    }

    protected static boolean isMoveValidCompressionMove(AmoebotParticle p, ParticleGrid.Direction d, boolean swapsAllowed, boolean nonswapsAllowed, Predicate<Particle> filter) {
        if (!p.isDirectionWithinBounds(d)) {
            return false;
        }

        boolean isOccupied = p.getNeighborInDirection(d, 0, null) != null;
        if (isOccupied && swapsAllowed) return true;

        if (isOccupied || !nonswapsAllowed) {
            return false;
        }

        boolean cond1 = p.getNeighborParticles(false, filter).size() < 5;
        boolean cond2 = RuleUtils.checkProperty1(p, d, filter);
        boolean cond3 = RuleUtils.checkProperty2(p, d, filter);

        return cond1 && (cond2 || cond3);
    }

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
            boolean a = grid.isPositionValid(position, null);
            boolean b = grid.isPositionOccupied(position);
            return a && b;
        });
    }

    protected static boolean checkParticleHoles(ParticleGrid grid, Predicate<Particle> filter) {
        return checkConnected(grid, position -> {
            boolean a = grid.isPositionValid(position, null);
            boolean b = !grid.isPositionOccupied(position);
            return a && b;
        });
    }

    private static boolean checkConnected(ParticleGrid grid, Predicate<Vector> inclusionFilter) {
        int eligibleCount = (int) grid.getValidPositions().filter(inclusionFilter).count();
        Vector start = grid.getValidPositions().filter(inclusionFilter).findAny().orElse(null);

        //System.out.println("Eligible: " + eligibleCount);

        Function<Vector, Stream<Vector>> neighborGetter = (p -> grid.getAdjacentPositions(p).filter(inclusionFilter));

        Set<Vector> searched = search(start, neighborGetter);
        //System.out.println("Searched: " + searched.size());

        return eligibleCount == searched.size();
    }

    public static Set<Particle> getComponent(Particle origin, ParticleGrid grid) {
        Function<Particle, Stream<Particle>> neighborGetter = (p -> grid.getParticleNeighbors(p, false).stream());

        return search(origin, neighborGetter);
    }

    public static Set<Particle> getLargestComponent(ParticleGrid grid) {
        Set<Particle> largestComponent = Sets.newHashSet();
        Set<Particle> foundParticles = Sets.newHashSet();

        List<Particle> particles = grid.getAllParticles().collect(Collectors.toList());

        for (Particle p: particles) {
            if (!foundParticles.contains(p)) {
                // New component! Find it
                Set<Particle> component = getComponent(p, grid);

                // Is it the largest?
                if (component.size() > largestComponent.size()) largestComponent = component;

                // Mark the particles in the component as found
                foundParticles.addAll(component);
            }
        }

        return largestComponent;
    }

    private static <T> Set<T> search(T start, Function<T, Stream<T>> getNeighbors) {
        Set<T> visited = Sets.newHashSet();
        Queue<T> queue = new LinkedList<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            T vertex = queue.remove();

            if (!visited.contains(vertex)) {
                visited.add(vertex);

                Set<T> nextUp = getNeighbors.apply(vertex).collect(Collectors.toSet());
                queue.addAll(Sets.difference(nextUp, visited));
            }
        }

        return visited;
    }
}
