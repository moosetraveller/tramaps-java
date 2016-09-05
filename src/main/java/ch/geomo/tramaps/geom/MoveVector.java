/*
 * Copyright (c) 2016 Thomas Zuberbuehler. All rights reserved.
 */

package ch.geomo.tramaps.geom;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.math.Vector2D;
import org.jetbrains.annotations.NotNull;

import static ch.geomo.tramaps.geom.util.GeomUtil.get;
import static ch.geomo.tramaps.geom.util.GeomUtil.getGeomUtil;

/**
 * An implementation of {@link Vector2D} providing constructor to create a vector with a {@link LineString}.
 *
 * @see Vector2D
 */
public class MoveVector extends Vector2D {

    public static final Vector2D VECTOR_ALONG_X_AXIS = new Vector2D(1, 0);
    public static final Vector2D VECTOR_ALONG_Y_AXIS = new Vector2D(0, 1);

    private final LineString lineString;

    public MoveVector() {
        super(0, 0);
        lineString = getGeomUtil().createLineString();
    }

    public MoveVector(@NotNull LineString lineString) {
        super(lineString.getStartPoint().getCoordinate(), lineString.getEndPoint().getCoordinate());
        this.lineString = lineString;
    }

    @Override
    public double getX() {
        return getGeomUtil().getPrecisionModel().makePrecise(super.getX());
    }

    @Override
    public double getY() {
        return getGeomUtil().getPrecisionModel().makePrecise(super.getY());
    }

    @NotNull
    @SuppressWarnings("unused")
    public LineString getLineString() {
        return lineString;
    }

    @NotNull
    public Vector2D getProjection(@NotNull Vector2D alongVector) {
        return alongVector.multiply(dot(alongVector) / alongVector.dot(alongVector));
    }

    @Override
    public String toString() {
        return "MoveVector: {x = " + getX() + ", y =" + getY() + "}";
    }

}