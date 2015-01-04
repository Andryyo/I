package com.Andryyo.I;

import android.app.Activity;
import android.os.Bundle;

import java.util.HashMap;

public class MyActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Simulation s = new Simulation();
        ((SimulationView)findViewById(R.id.view)).setSimulation(s);
        s.setOnUpdateListener(((SimulationView)findViewById(R.id.view)));

        /*s.addSimulationObject(
                new SimulationObject(
                        500000,
                        new SimulationObjectPosition(
                                0,
                                0,
                                new SimulationVector(0,0)
                        )
                )
        );*/

        s.addSimulationObject(
                new SimulationObject(
                        1,
                        new SimulationObjectPosition(
                                new SimulationVector(0, 100),
                                new SimulationVector(Math.sqrt(1.5*Simulation.GRAVITATIONAL_CONSTANT*100000/100),0)
                               // new SimulationVector(718, 0)
                        )
                )
        );

        s.addSimulationObject(
                new SimulationObject(
                        300000,
                        new SimulationObjectPosition(
                                new SimulationVector(0, 0),
                                new SimulationVector(0,0)
                        )
                )
        );

        /*s.addSimulationObject(
                new SimulationObject(
                        1000,
                        new SimulationObjectPosition(
                                10,
                                10,
                                new SimulationVector(0,10)
                        )
                )
        );*/
    }
}
