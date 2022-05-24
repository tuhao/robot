package com.mf.ppm.lh;

import robocode.*;
import robocode.util.Utils;

public class TrackerTank extends AdvancedRobot {

    double moveAngle = 0D;

    double enemyBearing = 0;

    double enemyDistance = 1000;

    double safeDistance = 400;

    double firePower = 2;

    int dir = 1;

    int continuousHit = 0;

    int shot = 0;

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

    private void moveToEnemy() {
        if (hitWall) {
            return;
        }
        setTurnRight(enemyBearing);
        setAhead(100);
    }

    private void hideBullet() {
        if (hitWall) {
            return;
        }
        double delta = 0;
        if (Math.random() > .618) {
            delta = 50;
        }
        if (enemyBearing >= 90D && enemyBearing < 180D) {
            setTurnRight(enemyBearing - 90D);
        } else if (enemyBearing >= -90D && enemyBearing < 0) {
            setTurnRight(enemyBearing + 90D);
        } else if (enemyBearing >= 0 && enemyBearing < 90D) {
            setTurnRight(enemyBearing - 90D);
        } else if (enemyBearing >= -180D && enemyBearing < -90D) {
            setTurnRight(enemyBearing + 90D);
        }
        setAhead((100 + delta) * dir);
        waitFor(new MoveCompleteCondition(this));
        dir = -dir;
    }

    private void reloadBullet() {
        if (getEnergy() < 10) {
            firePower = getEnergy() / 10;
        } else if (continuousHit >= 3) {
            firePower = 3;
        } else {
            firePower = 2;
        }
    }

    public void run() {
        moveToCenter();
        while (true) {
            if (getRadarTurnRemainingRadians() == 0) {
                setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
            }
            moveBody();
            execute();
        }
    }

    private void moveBody() {
        if (enemyDistance > safeDistance) {
            moveToEnemy();
        } else {
            hideBullet();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        enemyDistance = e.getDistance();
        enemyBearing = e.getBearing();
        if (shot > 1) {
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
        setAhead(100);
        hitWall = false;
    }

    public void onWin(WinEvent e) {
        for (int i = 0; i < 50; i++) {
            turnRight(30);
            turnLeft(30);
        }
    }

}
