package robotjy;

import robocode.*;

import java.awt.Color;


// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * Jane - a robot by (your name here)
 */
public class Jane extends AdvancedRobot {
    /**
     * run: Jane's default behavior
     */
    boolean movingForward;

    double previousEnergy = 100;

    int movementDirection = 1;

    int gunDirection = 1;

    public void run() {
        // Set colors
        setBodyColor(Color.RED);
        setGunColor(Color.RED);
        setRadarColor(Color.RED);
        setBulletColor(Color.RED);
        setScanColor(Color.RED);

        //The gun is rotated in the clockwise rotation.
        setTurnGunRight(99999);
    }


    /**
     * onScannedRobot: What to do when you see another robot
     */
    public void onScannedRobot(ScannedRobotEvent e) {
        // Stay at right angles to the opponent
        setTurnRight(e.getBearing() + 90 - 30 * movementDirection);

        double changeInEnergy = previousEnergy - e.getEnergy();

        // Move
        if (changeInEnergy > 0 && changeInEnergy <= 3) {
            movementDirection = -movementDirection;
            setAhead((e.getDistance() / 4 + 25) * movementDirection);
            movingForward = true;
        }
        //
        gunDirection = -gunDirection;
        setTurnGunRight(99999 * gunDirection);
        // Fire directly at target
        fire(2);
        // Track the energy level
        previousEnergy = e.getEnergy();

    }

    /**
     * onHitWall: What to do when you hit a wall
     */
    public void onHitWall(HitWallEvent e) {
        if (movingForward) {
            setBack(40000);
            movingForward = false;
        } else {
            setAhead(40000);
            movingForward = true;
        }
    }
}
