package com.mf.ppm.lh;

import robocode.*;
import robocode.util.Utils;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

public class LHTank3 extends AdvancedRobot {

    double dogFightDistance = 200;

    double safeDistance = 300;

    int dir = 1;

    double oldEnemyHeading = 0;

    Map<Boolean, Double> hitRateMap = new HashMap<>();

    double predictHit = 1;

    double predictMiss = 0;

    double nonePredictHit = 1;

    double nonePredictMiss = 4;

    boolean predict = true;

    @Override
    public void run() {
        setAdjustRadarForGunTurn(true);
        setAdjustGunForRobotTurn(true);

        hitRateMap.put(true, 1D);
        hitRateMap.put(false, .2);

        while (true) {
            if (getRadarTurnRemainingRadians() == 0) {
                setTurnRadarRightRadians(Math.PI / 6);
            }
            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        double absBearing = e.getBearingRadians() + getHeadingRadians();
        double enemyDistance = e.getDistance();
        double bulletPower = enemyDistance < dogFightDistance ? 3 : 2;
        bulletPower = getEnergy() < 10 ? getEnergy() / 10 : bulletPower;
        double bulletSpeed = 20 - 3 * bulletPower;//Formula for bullet speed.

        // lock radar
        double radarOffset = Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians());
        setTurnRadarRightRadians(radarOffset * 1.1);

        // calculate gun turning angle
        // finding the heading and heading change.
        double enemyHeading = e.getHeadingRadians();
        double enemyHeadingChange = enemyHeading - oldEnemyHeading;
        oldEnemyHeading = enemyHeading;
        /*This method of targeting is known as circular targeting; you assume your enemy will
         *keep moving with the same speed and turn rate that he is using at fire time.The
         *base code comes from the wiki.
         */
        double deltaTime = 0;
        double predictedX = getX() + e.getDistance() * Math.sin(absBearing);
        double predictedY = getY() + e.getDistance() * Math.cos(absBearing);
        while ((++deltaTime) * bulletSpeed < Point2D.Double.distance(getX(), getY(), predictedX, predictedY)) {

            if (hitRateMap.get(true) >= hitRateMap.get(false)) {
                //Add the movement we think our enemy will make to our enemy's current X and Y
                predictedX += Math.sin(enemyHeading) * e.getVelocity();
                predictedY += Math.cos(enemyHeading) * e.getVelocity();
                predict = true;
            } else {
                predict = false;
            }
            //Find our enemy's heading changes.
            enemyHeading += enemyHeadingChange;

            //If our predicted coordinates are outside the walls, put them 18 distance units away from the walls as we know
            //that that is the closest they can get to the wall (Bots are non-rotating 36*36 squares).
            predictedX = Math.max(Math.min(predictedX, getBattleFieldWidth() - 18), 18);
            predictedY = Math.max(Math.min(predictedY, getBattleFieldHeight() - 18), 18);
        }
        // find the bearing of our predicted coordinates from us.
        double aim = Utils.normalAbsoluteAngle(Math.atan2(predictedX - getX(), predictedY - getY()));
        // aim and fire.
        setTurnGunRightRadians(Utils.normalRelativeAngle(aim - getGunHeadingRadians()));
        waitFor(new GunTurnCompleteCondition(this));
        setFire(bulletPower);

        // move
        // This makes the amount we want to turn be perpendicular to the enemy.
        double turn = absBearing + Math.PI / 2;
        double deltaAngle = Math.max(0.5, (1 / e.getDistance()) * 100) * dir;
        if (enemyDistance > safeDistance) {
            turn -= deltaAngle;
        } else if (enemyDistance < dogFightDistance) {
            turn += deltaAngle;
        }
        setTurnRightRadians(Utils.normalRelativeAngle(turn - getHeadingRadians()));
        //This line makes us slow down when we need to turn sharply.
        setMaxVelocity(400 / getTurnRemaining());
        setAhead(100 * dir);
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        dir = -dir;
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        dir = -dir;
    }

    @Override
    public void onBulletHit(BulletHitEvent e) {
        if (predict) {
            predictHit++;
            hitRateMap.put(true, predictHit / (predictHit + predictMiss));
        } else {
            nonePredictHit++;
            hitRateMap.put(false, nonePredictHit / (nonePredictHit + nonePredictMiss));
        }

    }

    @Override
    public void onBulletMissed(BulletMissedEvent e) {
        if (predict) {
            predictMiss++;
            hitRateMap.put(true, predictHit / (predictHit + predictMiss));
        } else {
            nonePredictMiss++;
            hitRateMap.put(false, nonePredictHit / (nonePredictHit + nonePredictMiss));
        }
    }
}
