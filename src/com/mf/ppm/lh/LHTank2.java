package com.mf.ppm.lh;

import robocode.*;
import robocode.util.Utils;

import java.awt.*;

public class LHTank2 extends AdvancedRobot {

    double dir = 1;

    int beenHit = 0;

    public void run() {
        setBodyColor(Color.orange);
        setGunColor(Color.orange);
        setRadarColor(Color.red);

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        while (true) {
            setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
            execute();
        }

    }

    private double reloadBullet(ScannedRobotEvent e) {
        if (getEnergy() < 10) {
            return getEnergy() / 10;
        } else if (e.getDistance() > 500) {
            return 2;
        } else {
            return 3;
        }
    }

    private void aimByVelocity(ScannedRobotEvent e, double firePower) {
        double bulletVelocity = 20 - 3 * firePower;
        double escapeRange = Math.sin(e.getHeadingRadians() - getGunHeadingRadians()) * e.getVelocity();
        double escapeAngle = Math.asin(escapeRange / bulletVelocity);
        double absoluteBearing = e.getBearingRadians() + getHeadingRadians();
        double turnGunAngle = Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians() + escapeAngle);
        setTurnGunRightRadians(turnGunAngle);
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        double absoluteBearing = e.getBearingRadians() + getHeadingRadians();
        double firePower = reloadBullet(e);
        aimByVelocity(e, firePower);
        waitFor(new GunTurnCompleteCondition(this));
        double turn = absoluteBearing + Math.PI / 2;
        double deltaAngle = Math.max(0.5, (1 / e.getDistance()) * 100) * dir;
        if (e.getDistance() > 300) {
            setMaxVelocity(Math.max(2, Math.random() * 10));
            turn -= deltaAngle;
        } else if (e.getDistance() < 100) {
            setMaxVelocity(Rules.MAX_VELOCITY);
            turn += deltaAngle;
        } else {
            setMaxVelocity(Rules.MAX_VELOCITY);
            turn -= Math.min(.3, Math.random()) * deltaAngle;
        }
        setFire(firePower);
        setTurnRightRadians(Utils.normalRelativeAngle(turn - getHeadingRadians()));
        double deltaDistance = Math.max(50, Math.random() * 100);
        setAhead((100 + deltaDistance) * dir);
        setTurnRadarRightRadians(Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians()));
    }

    public void onHitWall(HitWallEvent e) {
        dir = -dir;
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        beenHit++;
        if (beenHit % 2 == 0) {
            dir = -dir;
        }
    }

    @Override
    public void onWin(WinEvent event) {
        for (int i = 0; i < 50; i++) {
            turnRight(30);
            turnLeft(30);
        }
    }
}
