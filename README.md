
# Real-Time Traffic Simulation

This project features a real time traffic simulation engine.

## Simple User Guide

`SUMO > Connect`, select the demanded SUMO binary and path to your .sumocfg file.
Then press `Start`.

Use the `Step` button to perform a singular simulation step of 0.5s.

Disconnect the Simulation via `SUMO > Disconnect`

## Requirements

- Java JRE Version 24 or higher
- SUMO Traffic Simulation Engine (`sumo` or `sumo-gui`, preferably version 1.15.0)

## Import Project (IntelliJ IDEA)

Open the project in IntelliJ IDEA.

Run the `libs/setup.sh` (Unix) or `libs/setup.cmd` (Windows) to install the TraaS library into your local repository.

(_Will be installed under `~/home/USERNAME/.m2/repository/de/tudresden/traas`_)

Refresh the maven project:
- Open the maven tab on the right side of the IntelliJ UI (the 'm'-button).
- Run `Reload all Maven Projects` (The swirly-reload-arrow symbol).

### Running the Application

Select the Run Configuration `Main` nd press `Run  'Main'`.

### Building an executable Jar file

`Build > Build Artifacts > trafficsim:jar > Build`

The final executable jar will be located at `out/artifacts/trafficsim_jar/trafficsim.jar`
and can be executed by running `java -jar trafficsim.jar`
