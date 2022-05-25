package com.mf.ppm.lh;

import robocode.*;
import robocode.util.Utils;

public class SniperTank extends AdvancedRobot {

    double enemyBearing = 0;

    double enemyDistance = 1000;

    double enemyEnergy = 100D;

    double firePower = 2;

    int swingDir = 1;

    int continuousHit = 0;

    int continuousMissed = 0;

    boolean hitWall = false;

    private void moveToCenter() {
        double x = getX();
        double y = getY();
        double deltaX = x - getBattleFieldWidth() / 2;
        double deltaY = y - getBattleFieldHeight() / 2;
        double angleToCenter = Math.atan(Math.abs(deltaX / deltaY));
        if (deltaX >= 0) {
            if (deltaY >= 0) {
                turnLeftRadians(Math.PI - angleToCenter + getHeadingRadians());
            } else {
                turnLeftRadians(angleToCenter + getHeadingRadians());
            }
        } else {
            if (deltaY >= 0) {
                turnLeftRadians(Math.PI + angleToCenter + getHeadingRadians());
            } else {
                turnLeftRadians(-angleToCenter + getHeadingRadians());
            }
        }
        waitFor(new TurnCompleteCondition(this));
        setAhead(Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2)));
        waitFor(new MoveCompleteCondition(this));
    }

    private void turnHeading() {
        if (enemyBearing >= 90D && enemyBearing < 180D) {
            setTurnRight(enemyBearing - 90D);
        } else if (enemyBearing >= -90D && enemyBearing < 0) {
            setTurnRight(enemyBearing + 90D);
        } else if (enemyBearing >= 0 && enemyBearing < 90D) {
            setTurnRight(enemyBearing - 90D);
        } else if (enemyBearing >= -180D && enemyBearing < -90D) {
            setTurnRight(enemyBearing + 90D);
        }
    }

    private void reloadBullet() {
        if (getEnergy() < 10) {
            firePower = getEnergy() / 10;
        } else if (continuousHit >= 2 || enemyDistance <= 300) {
            firePower = 3;
        } else {
            firePower = 2;
        }
    }

    public void run() {
        moveToCenter();
        while (true) {
            setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

            double delta = 0;
            if (Math.random() > .5) {
                delta = 50;
            }
            double moveDistance = (100 + delta) * swingDir;
            if (enemyDistance < 200) {
                moveDistance = moveDistance / 2;
            }
            ahead(moveDistance);
            turnHeading();
            if (enemyDistance >= 200) {
                ahead(moveDistance);
            }
            swingDir = -swingDir;
            execute();
        }
    }

    private void aimByVelocity(ScannedRobotEvent e) {
        double bulletVelocity = 20 - 3 * firePower;
        double escapeRange = Math.sin(e.getHeadingRadians() - getGunHeadingRadians()) * e.getVelocity();
        double escapeAngle = Math.asin(escapeRange / bulletVelocity);
        double absoluteBearing = e.getBearingRadians() + getHeadingRadians();
        double turnGunAngle = Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians() + escapeAngle);
        setTurnGunRightRadians(turnGunAngle);
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        enemyDistance = e.getDistance();
        enemyBearing = e.getBearing();
        enemyEnergy = e.getEnergy();
        double absoluteBearing = e.getBearingRadians() + getHeadingRadians();
        aimByVelocity(e);
        setTurnRadarRightRadians(Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians()));
        waitFor(new GunTurnCompleteCondition(this));
        reloadBullet();
        setFire(firePower);
    }

    @Override
    public void onBulletMissed(BulletMissedEvent event) {
        continuousHit = 0;
        continuousMissed++;
    }

    @Override
    public void onBulletHit(BulletHitEvent event) {
        continuousMissed = 0;
        continuousHit++;
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        hitWall = true;
        double wallBearing = e.getBearing();
        if (wallBearing >= -90D && wallBearing < 90D) {
            if (wallBearing < 0) {
                setTurnRight(120D + wallBearing);
            } else {
                setTurnLeft(120D - wallBearing);
            }
        } else {
            if (wallBearing < -90D) {
                setTurnRight(180D + wallBearing);
            } else {
                setTurnLeft(180D - wallBearing);
            }
        }
        waitFor(new TurnCompleteCondition(this));
        setAhead(200);
        hitWall = false;
    }

    public void onWin(WinEvent e) {
        for (int i = 0; i < 50; i++) {
            turnRight(30);
            turnLeft(30);
        }
    }

}
