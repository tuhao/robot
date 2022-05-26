package com.mf.ppm.lh;

import robocode.*;
import robocode.util.Utils;

public class SniperTank extends AdvancedRobot {

    double enemyBearing = 0;

    double enemyDistance = 1000;

    double enemyEnergy = 100D;

    double safeDistance = 300;

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

    private void turnHeading(double delta) {
        if (enemyBearing >= 90D && enemyBearing < 180D) {
            setTurnRight(enemyBearing - 90D - delta);
        } else if (enemyBearing >= -90D && enemyBearing < 0) {
            setTurnRight(enemyBearing + 90D + delta);
        } else if (enemyBearing >= 0 && enemyBearing < 90D) {
            setTurnRight(enemyBearing - 90D - delta);
        } else if (enemyBearing >= -180D && enemyBearing < -90D) {
            setTurnRight(enemyBearing + 90D + delta);
        }
    }

    private double reloadBullet() {
        if (getEnergy() < 10) {
            return getEnergy() / 10;
        } else if (continuousHit >= 2 || enemyDistance < safeDistance) {
            return 3;
        } else {
            return 2;
        }
    }

    public void run() {
        setAdjustRadarForGunTurn(true);
        setAdjustGunForRobotTurn(true);
        moveToCenter();
        while (true) {
            setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

            double deltaAngle = 0;
            double moveDistance = 100;
            double deltaDistance = Math.max(50, Math.random() * 100);
            if (enemyDistance < safeDistance) {
                deltaAngle = -30D * swingDir;
            }
            ahead((100 + deltaDistance) * swingDir);
            turnHeading(deltaAngle);
            if (enemyDistance >= safeDistance) {
                ahead(moveDistance * swingDir);
            }
            swingDir = -swingDir;
            execute();
        }
    }

    private void aimByVelocity(ScannedRobotEvent e, double firePower) {
        double bulletVelocity = 20 - 3 * firePower;
        double escapeRange = Math.sin(e.getHeadingRadians() - getGunHeadingRadians()) * e.getVelocity();
        double escapeAngle = Math.asin(escapeRange / bulletVelocity);
        if (e.getDistance() < safeDistance) {
            escapeAngle /= 3;
        }
        double absoluteBearing = e.getBearingRadians() + getHeadingRadians();
        double turnGunAngle = Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians() + escapeAngle);
        setTurnGunRightRadians(turnGunAngle);
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        enemyDistance = e.getDistance();
        enemyBearing = e.getBearing();
        enemyEnergy = e.getEnergy();
        double firePower = reloadBullet();
        double absoluteBearing = e.getBearingRadians() + getHeadingRadians();
        aimByVelocity(e, firePower);
        setTurnRadarRightRadians(Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians()));
        waitFor(new GunTurnCompleteCondition(this));
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
