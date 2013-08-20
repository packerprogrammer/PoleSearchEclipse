package com.glps.polesearch;

/**
 * Created by ghensley on 5/22/13.
 */
public class GlpsMarker {
    private String gridNumber;
    private String poleNumber;
    private double lat;
    private double lng;

    public String getGrid()
    {
        return gridNumber;
    }

    public String getPole()
    {
        return poleNumber;
    }

    public double[] getCoords()
    {
        double[] coords = new double[2];
        coords[0] = this.lat;
        coords[1] = this.lng;
        return coords;
    }

    public void setGrid(String grid)
    {
        this.gridNumber = grid;
    }

    public void setPole(String pole)
    {
        this.poleNumber = pole;
    }

    public void setCoord(double lat, double lng)
    {
        this.lat = lat;
        this.lng = lng;
    }
    public static Float GetDistance(float x, float y, float lng, float lat) {
        float a = x - lng;
        float b = y - lat;
        return (float) Math.sqrt((a * a) + (b * b));
    }
}
