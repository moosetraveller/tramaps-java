/*
 * Copyright (c) 2016 Thomas Zuberbuehler. All rights reserved.
 */

package ch.geomo.tramaps.graph.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public enum OctilinearDirection implements Direction {

    NORTH(0d, Alignment.HORIZONTAL),
    NORTH_EAST(45d, Alignment.DIAGONAL_45),
    EAST(90d, Alignment.VERTICAL),
    SOUTH_EAST(135d, Alignment.DIAGONAL_125),
    SOUTH(180d, Alignment.HORIZONTAL),
    SOUTH_WEST(225d, Alignment.DIAGONAL_45),
    WEST(270d, Alignment.VERTICAL),
    NORTH_WEST(315d, Alignment.DIAGONAL_125);

    private final double angle;
    private final Alignment alignment;

    OctilinearDirection(double angle, Alignment alignment) {
        this.angle = angle;
        this.alignment = alignment;
    }

    public double getAngle() {
        return angle;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    @Contract(pure = true)
    public boolean isHorizontal() {
        return this == EAST || this == WEST;
    }

    @Contract(pure = true)
    public boolean isVertical() {
        return this == NORTH || this == SOUTH;
    }

    /**
     * Returns the closest octilinear direction for this direction. Since
     * this implementation of {@link Direction} is always octilinear, the
     * current instance will always be returned.
     *
     * @return current instance
     */
    @NotNull
    @Override
    @Contract(value = "->!null", pure = true)
    public OctilinearDirection toOctilinear() {
        return this; // just satisfying interface :-)
    }

    /**
     * @return true if given {@link Direction} is an instance of {@link OctilinearDirection}
     */
    @Contract(value = "null -> false", pure = true)
    public static boolean isOctilinear(@Nullable Direction direction) {
        return direction instanceof OctilinearDirection;
    }

    /**
     * Finds the octilinear direction for given angle. If angle is not a multiple
     * of 45 degree, an octilinear direction will be evaluated using
     * {@link AnyDirection#toOctilinear()}.
     *
     * @return the octilinear direction for given angle
     */
    @NotNull
    public static OctilinearDirection fromAngle(double angle) {
        return Arrays.stream(values())
                .filter(direction -> direction.angle == (angle % 360))
                .findFirst()
                .orElseGet(() -> new AnyDirection(angle).toOctilinear());
    }

    /**
     * @return the opposite direction of this direction
     */
    @NotNull
    public Direction opposite() {
        return fromAngle(Math.abs(360 - angle));
    }

    @NotNull
    public OctilinearDirection rotate(@NotNull OctilinearDirection nullDirection) {
        return fromAngle(getAngle() + nullDirection.getAngle());
    }

    @Override
    public String toString() {
        return "Direction: {" + super.toString() + ", angle= " + angle + "}";
    }
}
