package com.Andryyo.I;

import java.util.HashMap;

/**
 * Created by Андрей on 02.01.2015.
 */
public class SimulationObjectPosition {
    public SimulationVector position;
    public SimulationVector speed;
    public SimulationVector ownAcceleration;
    public double mass;
    public HashMap<SimulationObject, SimulationVector> forces;

    public SimulationObjectPosition(SimulationVector position, SimulationVector speed, SimulationVector ownAcceleration, double mass)   {
        this.position = position;
        this.speed = speed;
        this.ownAcceleration = ownAcceleration;
        this.mass = mass;
        this.forces = new HashMap<SimulationObject, SimulationVector>();
    }

    public SimulationObjectPosition(SimulationObjectPosition p1)   {
        this.position = new SimulationVector(p1.position);
        this.speed = new SimulationVector(p1.speed);
        this.forces = new HashMap<SimulationObject, SimulationVector>(p1.forces);
        this.ownAcceleration = new SimulationVector(p1.ownAcceleration);
        this.mass = p1.mass;
    }

    public SimulationObjectPosition(double [] array)   {
        this.position = new SimulationVector(array[0], array[1]);
        this.speed = new SimulationVector(array[2], array[3]);
        this.ownAcceleration = new SimulationVector(array[4], array[5]);
        this.mass = array[6];
        this.forces = new HashMap<SimulationObject, SimulationVector>();
    }

    public double [] toArray()  {
        return new double[] {position.x, position.y, speed.x, speed.y, ownAcceleration.x, ownAcceleration.y, mass};
    }
}
