package com.Andryyo.I;

import com.Andryyo.I.SimulationVector;
/**
 * Created by Андрей on 02.01.2015.
 */
public class SimulationObject {
    private SimulationObjectPosition position;
    private SimulationVector ownAcceleration;
    private double mass;

    public SimulationObject(double mass, SimulationObjectPosition position)   {
        this.ownAcceleration = new SimulationVector(0, 0);
        this.mass = mass;
        this.position = position;
    }

    public SimulationObjectPosition getPosition()   {
        return position;
    }

    public void setPosition(SimulationObjectPosition p) {
        position = p;
    }

    public SimulationVector getOwnAcceleration()    {
        return ownAcceleration;
    }

    public double getMass()  {
        return mass;
    }
}
