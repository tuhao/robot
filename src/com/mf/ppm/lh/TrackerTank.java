package com.mf.ppm.lh;

import robocode.AdvancedRobot;
import robocode.GunTurnCompleteCondition;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class TrackerTank extends AdvancedRobot {

    double moveAngle = Math.PI / 6;

    double enemyBearing = 0;

    double enemyDistance = 1000;

    double safeDistance = 300;

    double firePower = 2;

    int dir = 1;

    int continueHit = 0;

    int shot = 0;

    private void moveToEnemy() {
        setTurnRightRadians(enemyBearing + moveAngle);
        setAhead(50);
        moveAngle = -moveAngle;
    }

    private void hideBullet() {
        setTurnLeftRadians(Math.PI / 2 - enemyBearing);
        setAhead(100 * dir);
        dir = -dir;
    }

    private void reloadBullet() {
        if (getEnergy() < 10) {
            firePower = getEnergy() / 10;
        } else if (continueHit >= 3) {
            firePower = 3;
        } else {
            firePower = 2;
        }
    }

    public void run() {
        setTurnLeftRadians(getHeadingRadians() % Math.PI / 2);
        while (true) {
            if (getRadarTurnRemainingRadians() == 0) {
                setTurnRadarRightRadians(Math.PI / 4);
            }
//            if (enemyDistance > safeDistance) {
//                moveToEnemy();
//            } else {
//                hideBullet();
//            }
            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        enemyDistance = e.getDistance();
        enemyBearing = e.getBearingRadians();
        if (shot > 0) {
            shot = 0;
            return;
        }
        double absoluteBearing = e.getBearingRadians() + getHeadingRadians();
        double bulletVelocity = 20 - 3 * firePower;
        double escapeRange = Math.sin(e.getHeadingRadians() - getGunHeadingRadians()) * e.getVelocity();
        double escapeAngle = Math.asin(escapeRange / bulletVelocity);
        double turnGunAngle = Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians() + escapeAngle);
        setTurnGunRightRadians(turnGunAngle);
        setTurnRadarRightRadians(Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians()));
        waitFor(new GunTurnCompleteCondition(this));
        reloadBullet();
        setFire(firePower);
        shot++;
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        double wallBearing = e.getBearingRadians();
        if (wallBearing >= -Math.PI / 2 && wallBearing < Math.PI / 2) {
            setBack(100);
            if (wallBearing < 0) {
                setTurnRightRadians(Math.PI / 2 + wallBearing);
            } else {
                setTurnLeftRadians(Math.PI / 2 - wallBearing);
            }
        } else {
            setAhead(100);
            if (wallBearing < Math.PI) {
                setTurnLeftRadians(Math.PI - wallBearing);
            } else {
                setTurnRightRadians(Math.PI + wallBearing);
            }
        }
    }

}
