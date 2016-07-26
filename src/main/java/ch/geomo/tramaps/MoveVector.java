package ch.geomo.tramaps;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.math.Vector2D;

public class MoveVector extends Vector2D {

    public static final Vector2D VECTOR_ALONG_X_AXIS = new Vector2D(1, 0);
    public static final Vector2D VECTOR_ALONG_Y_AXIS = new Vector2D(0, 1);

    private final LineString lineString;

    public MoveVector(LineString lineString) {
        super(lineString.getStartPoint().getCoordinate(), lineString.getEndPoint().getCoordinate());
        this.lineString = lineString;
    }

    @SuppressWarnings("unused")
    public LineString getLineString() {
        return lineString;
    }


}
