/*
 * This file is part of 12pit.
 *
 * Copyright (C) 2026 The 12pit Authors and contributors <https://github.com/12src/12pit>
 *
 * 12pit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * 12pit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with 12pit. If not, see <https://www.gnu.org/licenses/>.
 */
package pit12.feature.telebow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import pit12.runtime.arrow.ArrowAccess;

final class TelebowPath {
    private static final int BATCH_STEPS = 16;
    final EntityArrow arrow;
    final EntityPlayer shooter;
    final String displayName;
    final double[] points = new double[161 * 3];
    int color;
    double[] trail = new double[48];
    int trailCount;
    int count = 1;
    int futureStart;
    boolean hasImpact;
    AxisAlignedBB bounds;
    boolean dirty;
    long endedAt;
    long revision;
    private double progress;
    private double observedProgress;

    TelebowPath(EntityArrow arrow, EntityPlayer shooter, int color) {
        this.arrow = arrow;
        this.shooter = shooter;
        this.color = color;
        displayName = shooter.getDisplayName().getFormattedText();
    }

    void recordPosition() {
        double closest = Double.MAX_VALUE;
        observedProgress = 0;
        for (int index = 0; index + 1 < count; index++) {
            int offset = index * 3;
            double x = points[offset];
            double y = points[offset + 1];
            double z = points[offset + 2];
            double dx = points[offset + 3] - x;
            double dy = points[offset + 4] - y;
            double dz = points[offset + 5] - z;
            double length = dx * dx + dy * dy + dz * dz;
            if (length == 0)
                continue;
            double projection =
                    ((arrow.posX - x) * dx + (arrow.posY - y) * dy + (arrow.posZ - z) * dz)
                            / length;
            double fraction = Math.max(0, Math.min(1, projection));
            double errorX = arrow.posX - x - dx * fraction;
            double errorY = arrow.posY - y - dy * fraction;
            double errorZ = arrow.posZ - z - dz * fraction;
            double distance = errorX * errorX + errorY * errorY + errorZ * errorZ;
            if (distance < closest) {
                closest = distance;
                observedProgress = index == 0 && projection < 0 ? projection : index + fraction;
            }
        }
        if (observedProgress <= progress)
            return;
        int next = (int) observedProgress;
        for (int index = futureStart; index < next; index++) {
            recordSegment(index, 1);
        }
        // These segments are already in the mesh, so freezing them needs no rebuild.
        futureStart = next;
        progress = observedProgress;
    }

    private void recordSegment(int index, double fraction) {
        int offset = trailCount * 3;
        if (offset + 6 > trail.length)
            trail = Arrays.copyOf(trail, trail.length * 2);
        int start = index * 3;
        for (int axis = 0; axis < 3; axis++) {
            trail[offset + axis] = points[start + axis];
            trail[offset + 3 + axis] = points[start + axis]
                    + (points[start + 3 + axis] - points[start + axis]) * fraction;
        }
        trailCount += 2;
    }

    void finish(long now) {
        if (endedAt != 0)
            return;
        recordPosition();
        if (((ArrowAccess) arrow).pit12$isInGround()) {
            if (progress > futureStart)
                recordSegment(futureStart, progress - futureStart);
            points[0] = arrow.posX;
            points[1] = arrow.posY;
            points[2] = arrow.posZ;
            count = 1;
            futureStart = 0;
            hasImpact = true;
        }
        endedAt = now;
        dirty = false;
        revision++;
    }

    float opacity(long now, long hold, long fade) {
        if (endedAt == 0)
            return 1;
        long elapsed = now - endedAt - hold;
        return elapsed <= 0 ? 1 : Math.max(0, 1 - (float) elapsed / fade);
    }

    boolean affectedBy(BlockPos position) {
        int x = position.getX();
        int y = position.getY();
        int z = position.getZ();
        if (x + 1 < bounds.minX || x > bounds.maxX || y + 1 < bounds.minY || y > bounds.maxY
                || z + 1 < bounds.minZ || z > bounds.maxZ)
            return false;
        if (count < 2)
            return true;
        for (int index = Math.min(futureStart, count - 2); index + 1 < count; index++) {
            int offset = index * 3;
            if (x + 1 >= Math.min(points[offset], points[offset + 3]) - 1
                    && x <= Math.max(points[offset], points[offset + 3]) + 1
                    && y + 1 >= Math.min(points[offset + 1], points[offset + 4]) - 1
                    && y <= Math.max(points[offset + 1], points[offset + 4]) + 1
                    && z + 1 >= Math.min(points[offset + 2], points[offset + 5]) - 1
                    && z <= Math.max(points[offset + 2], points[offset + 5]) + 1)
                return true;
        }
        return false;
    }

    boolean predict() {
        // Wait for position corrections to catch up with the trail we already drew.
        if (observedProgress < progress)
            return false;
        if (progress > futureStart)
            recordSegment(futureStart, progress - futureStart);
        revision++;
        progress = 0;
        observedProgress = 0;
        futureStart = 0;
        count = 1;
        hasImpact = false;
        double x = arrow.posX;
        double y = arrow.posY;
        double z = arrow.posZ;
        double motionX = arrow.motionX;
        double motionY = arrow.motionY;
        double motionZ = arrow.motionZ;
        points[0] = x;
        points[1] = y;
        points[2] = z;
        Vec3 from = new Vec3(x, y, z);
        BlockPos.MutableBlockPos block = new BlockPos.MutableBlockPos();
        int chunkX = MathHelper.floor_double(x) >> 4;
        int chunkZ = MathHelper.floor_double(z) >> 4;
        boolean stopped = !arrow.worldObj.isBlockLoaded(
                block.set(MathHelper.floor_double(x), 64, MathHelper.floor_double(z)));
        List<AxisAlignedBB> boxes = new ArrayList<>();
        // Short batches keep entity queries narrow and stop simulation soon after an entity hit.
        for (int first = 0; first < 160 && !stopped; first += BATCH_STEPS) {
            double minX = x, minY = y, minZ = z;
            double maxX = x, maxY = y, maxZ = z;
            for (int step = first; step < Math.min(first + BATCH_STEPS, 160); step++) {
                Vec3 to = new Vec3(x + motionX, y + motionY, z + motionZ);
                int nextX = MathHelper.floor_double(to.xCoord);
                int nextZ = MathHelper.floor_double(to.zCoord);
                if (to.yCoord < 0 || ((nextX >> 4) != chunkX || (nextZ >> 4) != chunkZ)
                        && !arrow.worldObj.isBlockLoaded(block.set(nextX, 64, nextZ))) {
                    stopped = true;
                    break;
                }
                chunkX = nextX >> 4;
                chunkZ = nextZ >> 4;
                MovingObjectPosition hit =
                        arrow.worldObj.rayTraceBlocks(from, to, false, true, false);
                if (hit != null) {
                    to = hit.hitVec;
                    hasImpact = true;
                    stopped = true;
                }
                int offset = count++ * 3;
                points[offset] = to.xCoord;
                points[offset + 1] = to.yCoord;
                points[offset + 2] = to.zCoord;
                minX = Math.min(minX, to.xCoord);
                minY = Math.min(minY, to.yCoord);
                minZ = Math.min(minZ, to.zCoord);
                maxX = Math.max(maxX, to.xCoord);
                maxY = Math.max(maxY, to.yCoord);
                maxZ = Math.max(maxZ, to.zCoord);
                if (stopped)
                    break;
                float drag = arrow.worldObj.isMaterialInBB(
                        new AxisAlignedBB(x - 0.25, y, z - 0.25, x + 0.25, y + 0.5, z + 0.25),
                        Material.water) ? 0.6F : 0.99F;
                x = to.xCoord;
                y = to.yCoord;
                z = to.zCoord;
                from = to;
                motionX *= drag;
                motionY = motionY * drag - 0.05F;
                motionZ *= drag;
            }
            if (count == first + 1)
                break;
            List<Entity> entities = arrow.worldObj.getEntitiesWithinAABBExcludingEntity(arrow,
                    new AxisAlignedBB(minX - 1, minY - 1, minZ - 1, maxX + 1, maxY + 1, maxZ + 1));
            boxes.clear();
            int shooterIndex = -1;
            for (Entity entity : entities) {
                if (entity.isDead || !entity.canBeCollidedWith())
                    continue;
                if (entity instanceof EntityPlayer
                        && (((EntityPlayer) entity).capabilities.disableDamage
                                || arrow.shootingEntity instanceof EntityPlayer
                                        && !((EntityPlayer) arrow.shootingEntity)
                                                .canAttackPlayer((EntityPlayer) entity)))
                    continue;
                if (entity == arrow.shootingEntity)
                    shooterIndex = boxes.size();
                boxes.add(entity.getEntityBoundingBox().expand(0.3, 0.3, 0.3));
            }
            if (boxes.isEmpty())
                continue;
            int startOffset = first * 3;
            Vec3 start =
                    new Vec3(points[startOffset], points[startOffset + 1], points[startOffset + 2]);
            for (int segment = first + 1; segment < count; segment++) {
                int offset = segment * 3;
                Vec3 end = new Vec3(points[offset], points[offset + 1], points[offset + 2]);
                double segmentMinX = Math.min(start.xCoord, end.xCoord);
                double segmentMinY = Math.min(start.yCoord, end.yCoord);
                double segmentMinZ = Math.min(start.zCoord, end.zCoord);
                double segmentMaxX = Math.max(start.xCoord, end.xCoord);
                double segmentMaxY = Math.max(start.yCoord, end.yCoord);
                double segmentMaxZ = Math.max(start.zCoord, end.zCoord);
                Vec3 closest = null;
                double distance = Double.MAX_VALUE;
                for (int index = 0; index < boxes.size(); index++) {
                    if (index == shooterIndex && arrow.ticksExisted + segment < 5)
                        continue;
                    AxisAlignedBB box = boxes.get(index);
                    if (segmentMaxX < box.minX || segmentMinX > box.maxX || segmentMaxY < box.minY
                            || segmentMinY > box.maxY || segmentMaxZ < box.minZ
                            || segmentMinZ > box.maxZ)
                        continue;
                    MovingObjectPosition hit = box.calculateIntercept(start, end);
                    if (hit != null) {
                        double next = start.squareDistanceTo(hit.hitVec);
                        if (next < distance) {
                            closest = hit.hitVec;
                            distance = next;
                        }
                    }
                }
                if (closest != null) {
                    points[offset] = closest.xCoord;
                    points[offset + 1] = closest.yCoord;
                    points[offset + 2] = closest.zCoord;
                    count = segment + 1;
                    hasImpact = true;
                    stopped = true;
                    break;
                }
                start = end;
            }
        }
        double minX = points[0], minY = points[1], minZ = points[2];
        double maxX = minX, maxY = minY, maxZ = minZ;
        for (int index = 1; index < count; index++) {
            int offset = index * 3;
            minX = Math.min(minX, points[offset]);
            minY = Math.min(minY, points[offset + 1]);
            minZ = Math.min(minZ, points[offset + 2]);
            maxX = Math.max(maxX, points[offset]);
            maxY = Math.max(maxY, points[offset + 1]);
            maxZ = Math.max(maxZ, points[offset + 2]);
        }
        bounds = new AxisAlignedBB(minX - 1, minY - 1, minZ - 1, maxX + 1, maxY + 1, maxZ + 1);
        return true;
    }
}
