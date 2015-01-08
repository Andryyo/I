package com.Andryyo.I;

import java.util.HashMap;

/**
 * Created by Андрей on 02.01.2015.
 */
public class SimulationObjectPosition {
    public SimulationVector position;
    public SimulationVector speed;
    public HashMap<SimulationObject, SimulationVector> forces;

    public SimulationObjectPosition(SimulationVector position, SimulationVector speed)   {
        this.position = position;
        this.speed = speed;
        this.forces = new HashMap<SimulationObject, SimulationVector>();
    }

    public SimulationObjectPosition(SimulationObjectPosition p1)   {
        this.position = new SimulationVector(p1.position);
        this.speed = new SimulationVector(p1.speed);
        this.forces = new HashMap<SimulationObject, SimulationVector>(p1.forces);
    }
}
