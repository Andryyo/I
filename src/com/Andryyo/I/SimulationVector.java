package com.Andryyo.I;

/**
 * Created by Андрей on 02.01.2015.
 */
public class SimulationVector {
    public double x, y;
    public SimulationVector( double x, double y )   {
        this.x = x;
        this.y = y;
    }

    public SimulationVector(SimulationVector v) {
        this.x = v.x;
        this.y = v.y;
    }

    public static SimulationVector add(SimulationVector v1, SimulationVector v2)    {
        return new SimulationVector(v1.x + v2.x, v1.y + v2.y);
    }

    public static SimulationVector substract(SimulationVector v1, SimulationVector v2)  {
        return new SimulationVector(v1.x - v2.x, v1.y - v2.y);
    }

    public static SimulationVector multiply(SimulationVector v1, double value)    {
        return new SimulationVector(v1.x * value, v1.y * value);
    }

    public static double value(SimulationVector v1)   {
        return Math.sqrt(v1.x*v1.x + v1.y*v1.y);
    }
}
