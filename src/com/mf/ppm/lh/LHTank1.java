package com.mf.ppm.lh;

import robocode.*;
import robocode.util.Utils;

import java.awt.*;

public class LHTank1 extends AdvancedRobot {

    double enemyBearing = 0;

    double enemyDistance = 1000;

    double dogFightDistance = 250;

    double safeDistance = 300;

    int swingDir = 1;

    int hitByBullet = 0;

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
        } else if (enemyDistance <= dogFightDistance) {
            return 3;
        } else {
            return 2;
        }
    }

    public void run() {
        setBodyColor(Color.blue);
        setGunColor(Color.blue);
        setRadarColor(Color.green);

        setAdjustRadarForGunTurn(true);
        setAdjustGunForRobotTurn(true);
        long count = 0;
        while (true) {
            count++;
            setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
            if (enemyDistance > dogFightDistance) {
                double deltaAngle = 0;
                double deltaDistance = Math.max(50, Math.random() * 100);
                if (enemyDistance > 600) {
                    deltaAngle = 15D * swingDir;
                    deltaDistance += 20;
                }
                ahead((100 + deltaDistance) * swingDir);
                turnHeading(deltaAngle);
                if (enemyDistance >= safeDistance) {
                    ahead(100 * swingDir);
                }
                if (count % 2 == 0) {
                    swingDir = -swingDir;
                }
            }
            execute();
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

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        enemyDistance = e.getDistance();
        enemyBearing = e.getBearing();
        double firePower = reloadBullet();
        double absoluteBearing = e.getBearingRadians() + getHeadingRadians();
        aimByVelocity(e, firePower);
        setTurnRadarRightRadians(Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians()));
        waitFor(new GunTurnCompleteCondition(this));
        setFire(firePower);
        if (enemyDistance <= dogFightDistance) {
            double turn = absoluteBearing + Math.PI / 2;
            setTurnRightRadians(Utils.normalRelativeAngle(turn - getHeadingRadians()));
            double deltaDistance = Math.max(50, Math.random() * 100);
            setAhead((100 + deltaDistance) * swingDir);
        }
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
        enemyDistance = 0;
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        if (enemyDistance > safeDistance) {
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
        } else {
            swingDir = -swingDir;
        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        hitByBullet++;
        if (hitByBullet % 2 == 0 && enemyDistance > safeDistance) {
            swingDir = -swingDir;
        }
    }

    public void onWin(WinEvent e) {
        for (int i = 0; i < 50; i++) {
            turnRight(30);
            turnLeft(30);
        }
    }

}
