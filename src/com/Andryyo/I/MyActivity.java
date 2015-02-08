package com.Andryyo.I;

import android.app.Activity;
import android.os.Bundle;

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

        /*s.addSimulationObject(
                new SimulationObject(
                        new SimulationObjectPosition(
                                new SimulationVector(0, 0),
                                new SimulationVector(0, 0),
                                new SimulationVector(0, 0),
                                100000000
                        )
                )
        );*/

        /*s.addSimulationObject(
                new SimulationObject(
                        1000000,
                        new SimulationObjectPosition(
                                new SimulationVector(0, 100),
                                new SimulationVector(0,0)
                        )
                )
        );*/

        /*s.addSimulationObject(
                new SimulationObject(
                        new SimulationObjectPosition(
                                new SimulationVector(100, 100),
                                new SimulationVector(0,-3000),
                                new SimulationVector(0, 0),
                                1000000
                        )
                )
        );

        s.addSimulationObject(
                new SimulationObject(
                        new SimulationObjectPosition(
                                new SimulationVector(-100, -100),
                                new SimulationVector(0,3000),
                                new SimulationVector(0, 0),
                                1000000
                        )
                )
        );*/

        s.addSimulationObject(
                new SimulationObject(
                        new SimulationObjectPosition(
                                new SimulationVector(100, -100),
                                new SimulationVector(-1500,0),
                                new SimulationVector(0, 0),
                                1000000000
                        )
                )
        );

        s.addSimulationObject(
                new SimulationObject(
                        new SimulationObjectPosition(
                                new SimulationVector(-100, 100),
                                new SimulationVector(1500,0),
                                new SimulationVector(0, 0),
                                1000000000
                        )
                )
        );

        s.predict();
    }
}
