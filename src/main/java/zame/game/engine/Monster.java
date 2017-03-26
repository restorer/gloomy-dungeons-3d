package zame.game.engine;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import zame.game.Common;
import zame.game.SoundManager;

@SuppressWarnings("WeakerAccess")
public class Monster implements Externalizable {
    private static final long serialVersionUID = 0L;

    public int index;
    public int cellX;
    public int cellY;
    public float x;
    public float y;
    public int texture;
    public int dir; // 0 - right, 1 - up, 2 - left, 3 - down
    public int maxStep;
    public int health;
    public int hits;
    public float visibleDistSq;
    public float attackDistSq;
    public int shootSoundIdx;
    public int ammoType;

    public int step;
    public int prevX;
    public int prevY;
    public int hitTimeout; // hero hits monster
    public int attackTimeout; // monster hits hero
    public int removeTimeout;
    public long dieTime;
    public int aroundReqDir;
    public boolean inverseRotation;
    public int prevAroundX;
    public int prevAroundY;
    public int shootAngle;
    public int hitHeroTimeout;
    public int hitHeroHits;
    public boolean chaseMode;
    public boolean waitForDoor;

    public boolean isInAttackState;
    public boolean isAimedOnHero;

    @SuppressWarnings("MagicNumber")
    public void init() {
        step = 0;
        maxStep = 50;
        hitTimeout = 0;
        attackTimeout = 0;
        removeTimeout = 5000;
        dieTime = 0;
        aroundReqDir = -1;
        inverseRotation = false;
        prevAroundX = -1;
        prevAroundY = -1;
        visibleDistSq = 15.0f * 15.0f;
        hitHeroTimeout = 0;
        chaseMode = false;
        waitForDoor = false;
    }

    @Override
    public void writeExternal(ObjectOutput os) throws IOException {
        os.writeInt(cellX);
        os.writeInt(cellY);
        os.writeFloat(x);
        os.writeFloat(y);
        os.writeInt(texture);
        os.writeInt(dir);
        os.writeInt(maxStep);
        os.writeInt(health);
        os.writeInt(hits);
        os.writeFloat(visibleDistSq);
        os.writeFloat(attackDistSq);
        os.writeInt(shootSoundIdx);
        os.writeInt(ammoType);

        os.writeInt(step);
        os.writeInt(prevX);
        os.writeInt(prevY);
        os.writeInt(hitTimeout);
        os.writeInt(attackTimeout);
        os.writeInt(removeTimeout);
        os.writeInt(aroundReqDir);
        os.writeBoolean(inverseRotation);
        os.writeInt(prevAroundX);
        os.writeInt(prevAroundY);
        os.writeInt(shootAngle);
        os.writeInt(hitHeroTimeout);
        os.writeInt(hitHeroHits);
        os.writeBoolean(chaseMode);
        os.writeBoolean(waitForDoor);
    }

    @Override
    public void readExternal(ObjectInput is) throws IOException {
        cellX = is.readInt();
        cellY = is.readInt();
        x = is.readFloat();
        y = is.readFloat();
        texture = is.readInt();
        dir = is.readInt();
        maxStep = is.readInt();
        health = is.readInt();
        hits = is.readInt();
        visibleDistSq = is.readFloat();
        attackDistSq = is.readFloat();
        shootSoundIdx = is.readInt();
        ammoType = is.readInt();

        step = is.readInt();
        prevX = is.readInt();
        prevY = is.readInt();
        hitTimeout = is.readInt();
        attackTimeout = is.readInt();
        removeTimeout = is.readInt();
        aroundReqDir = is.readInt();
        inverseRotation = is.readBoolean();
        prevAroundX = is.readInt();
        prevAroundY = is.readInt();
        shootAngle = is.readInt();
        hitHeroTimeout = is.readInt();
        hitHeroHits = is.readInt();
        chaseMode = is.readBoolean();
        waitForDoor = is.readBoolean();

        isInAttackState = false;
        isAimedOnHero = false;
        dieTime = ((health <= 0) ? -1 : 0);
    }

    @SuppressWarnings("MagicNumber")
    public void setAttackDist(boolean longAttackDist) {
        attackDistSq = (longAttackDist ? (10.0f * 10.0f) : (1.8f * 1.8f));
    }

    public void copyFrom(Monster mon) {
        cellX = mon.cellX;
        cellY = mon.cellY;
        x = mon.x;
        y = mon.y;
        texture = mon.texture;
        dir = mon.dir;
        maxStep = mon.maxStep;
        health = mon.health;
        hits = mon.hits;
        visibleDistSq = mon.visibleDistSq;
        attackDistSq = mon.attackDistSq;
        shootSoundIdx = mon.shootSoundIdx;
        ammoType = mon.ammoType;

        step = mon.step;
        prevX = mon.prevX;
        prevY = mon.prevY;
        hitTimeout = mon.hitTimeout;
        attackTimeout = mon.attackTimeout;
        removeTimeout = mon.removeTimeout;
        dieTime = mon.dieTime;
        aroundReqDir = mon.aroundReqDir;
        inverseRotation = mon.inverseRotation;
        prevAroundX = mon.prevAroundX;
        prevAroundY = mon.prevAroundY;
        shootAngle = mon.shootAngle;
        hitHeroTimeout = mon.hitHeroTimeout;
        hitHeroHits = mon.hitHeroHits;
        chaseMode = mon.chaseMode;

        isInAttackState = mon.isInAttackState;
        isAimedOnHero = mon.isAimedOnHero;
    }

    public void hit(int amt, int hitTm) {
        hitTimeout = hitTm;
        health -= amt;
        aroundReqDir = -1;

        if (health <= 0) {
            SoundManager.playSound(SoundManager.SOUND_DETH_MON);
            State.passableMap[cellY][cellX] &= ~Level.PASSABLE_IS_MONSTER;
            State.passableMap[cellY][cellX] |= Level.PASSABLE_IS_DEAD_CORPSE;

            if (ammoType > 0) {
                if ((State.passableMap[cellY][cellX] & Level.PASSABLE_MASK_OBJECT_DROP) == 0) {
                    State.objectsMap[cellY][cellX] = ammoType;
                    State.passableMap[cellY][cellX] |= Level.PASSABLE_IS_OBJECT;
                } else {
                    outer:
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            if (((dy != 0) || (dx != 0)) && ((State.passableMap[cellY + dy][cellX + dx]
                                    & Level.PASSABLE_MASK_OBJECT_DROP) == 0)) {

                                State.objectsMap[cellY + dy][cellX + dx] = ammoType;
                                State.passableMap[cellY + dy][cellX + dx] |= Level.PASSABLE_IS_OBJECT;
                                break outer;
                            }
                        }
                    }
                }
            }

            State.killedMonsters++;
        }
    }

    public void remove() {
        State.passableMap[cellY][cellX] &= ~Level.PASSABLE_IS_DEAD_CORPSE;
        State.monstersCount--;

        for (int i = index; i < State.monstersCount; i++) {
            State.monsters[i].copyFrom(State.monsters[i + 1]);

            if (State.monsters[i].health <= 0) {
                State.passableMap[State.monsters[i].cellY][State.monsters[i].cellX] |= Level.PASSABLE_IS_DEAD_CORPSE;
            }
        }
    }

    @SuppressWarnings("MagicNumber")
    public void update() {
        if (health <= 0) {
            // removeTimeout--;	// do not remove dead corpses
            return;
        }

        if (hitHeroTimeout > 0) {
            hitHeroTimeout--;

            if (hitHeroTimeout <= 0) {
                Game.hitHero(hitHeroHits, shootSoundIdx, this);
            }
        }

        if (step == 0) {
            boolean tryAround = false;

            isInAttackState = false;
            isAimedOnHero = false;
            prevX = cellX;
            prevY = cellY;

            float dx = State.heroX - ((float)cellX + 0.5f);
            float dy = State.heroY - ((float)cellY + 0.5f);
            float distSq = (dx * dx) + (dy * dy);

            if (aroundReqDir >= 0) {
                if (!waitForDoor) {
                    dir = (dir + (inverseRotation ? 3 : 1)) % 4;
                }
            } else if (distSq <= visibleDistSq) {
                if (Math.abs(dy) <= 1.0f) {
                    dir = ((dx < 0) ? 2 : 0);
                } else {
                    dir = ((dy < 0) ? 1 : 3);
                }

                tryAround = true;
            }

            State.passableMap[cellY][cellX] &= ~Level.PASSABLE_IS_MONSTER;
            boolean vis = false;

            if ((distSq <= visibleDistSq) && Common.traceLine((float)cellX + 0.5f,
                    (float)cellY + 0.5f,
                    State.heroX,
                    State.heroY,
                    Level.PASSABLE_MASK_SHOOT_WM)) {

                chaseMode = true;
                vis = true;
            }

            if (vis && (distSq <= attackDistSq)) {
                int angleToHero = (int)(PortalTracer.getAngle(dx, dy) * Common.RAD2G_F);
                int angleDiff = angleToHero - shootAngle;

                if (angleDiff > 180) {
                    angleDiff -= 360;
                } else if (angleDiff < -180) {
                    angleDiff += 360;
                }

                angleDiff = ((angleDiff < 0) ? -angleDiff : angleDiff);
                shootAngle = angleToHero;
                float dist = (float)Math.sqrt(distSq);

                int minAngle = Math.max(1, 15 - (int)(dist * 3.0f));

                if (angleDiff <= minAngle) {
                    isAimedOnHero = true;
                    hitHeroHits = Common.getRealHits(hits, dist);
                    hitHeroTimeout = 2;
                    attackTimeout = 15;
                    step = 50;
                } else {
                    step = 8 + (angleDiff / 5);
                }

                isInAttackState = true;
                dir = ((shootAngle + 45) % 360) / 90;
                aroundReqDir = -1;
            } else {
                waitForDoor = false;

                for (int i = 0; i < 4; i++) {
                    switch (dir) {
                        case 0:
                            cellX++;
                            break;

                        case 1:
                            cellY--;
                            break;

                        case 2:
                            cellX--;
                            break;

                        case 3:
                            cellY++;
                            break;
                    }

                    if ((State.passableMap[cellY][cellX] & Level.PASSABLE_MASK_MONSTER) == 0) {
                        if (dir == aroundReqDir) {
                            aroundReqDir = -1;
                        }

                        step = maxStep;
                        break;
                    }

                    if (chaseMode
                            && ((State.passableMap[cellY][cellX] & Level.PASSABLE_IS_DOOR) != 0)
                            && ((State.passableMap[cellY][cellX] & Level.PASSABLE_IS_DOOR_OPENED_BY_HERO) != 0)) {

                        Door door = Level.doorsMap[cellY][cellX];

                        if (!door.sticked) {
                            door.open();

                            waitForDoor = true;
                            cellX = prevX;
                            cellY = prevY;
                            step = 10;
                            break;
                        }
                    }

                    cellX = prevX;
                    cellY = prevY;

                    if (tryAround) {
                        if ((prevAroundX == cellX) && (prevAroundY == cellY)) {
                            inverseRotation = !inverseRotation;
                        }

                        aroundReqDir = dir;
                        prevAroundX = cellX;
                        prevAroundY = cellY;
                        tryAround = false;
                    }

                    dir = (dir + (inverseRotation ? 1 : 3)) % 4;
                }

                if (step == 0) {
                    step = maxStep / 2;
                }

                shootAngle = dir * 90;
            }

            State.passableMap[cellY][cellX] |= Level.PASSABLE_IS_MONSTER;
        }

        x = (float)cellX + (((float)(prevX - cellX) * (float)step) / (float)maxStep) + 0.5f;
        y = (float)cellY + (((float)(prevY - cellY) * (float)step) / (float)maxStep) + 0.5f;

        if (attackTimeout > 0) {
            attackTimeout--;
        }

        if (hitTimeout > 0) {
            hitTimeout--;
        } else if (step > 0) {
            step--;
        }
    }
}
