package com.x4yi.x4ui.common.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.function.Predicate;

@FunctionalInterface
public interface ActionGuard {
    boolean allow(EntityPlayerMP player, @Nullable NBTTagCompound data);

    default ActionGuard and(ActionGuard other) {
        if (other == null) return this;
        return (player, data) -> this.allow(player, data) && other.allow(player, data);
    }

    static ActionGuard allowAll() {
        return (player, data) -> true;
    }

    static ActionGuard opLevel(int requiredLevel) {
        return (player, data) -> player != null && player.canUseCommand(requiredLevel, "");
    }

    static ActionGuard require(Predicate<EntityPlayerMP> predicate) {
        return (player, data) -> player != null && predicate != null && predicate.test(player);
    }

    static ActionGuard withinDistance(BlockPos pos, double maxDistance) {
        if (pos == null || maxDistance < 0.0 || !Double.isFinite(maxDistance)) {
            return (player, data) -> false;
        }
        final double maxSq = maxDistance * maxDistance;
        return (player, data) -> player != null
                && player.world != null
                && player.world.isBlockLoaded(pos)
                && player.getDistanceSqToCenter(pos) <= maxSq;
    }

    static ActionGuard withinDistance(int dimensionId, BlockPos pos, double maxDistance) {
        if (pos == null || maxDistance < 0.0 || !Double.isFinite(maxDistance)) {
            return (player, data) -> false;
        }
        final double maxSq = maxDistance * maxDistance;
        return (player, data) -> player != null
                && player.world != null
                && player.dimension == dimensionId
                && player.world.isBlockLoaded(pos)
                && player.getDistanceSqToCenter(pos) <= maxSq;
    }

    static ActionGuard withinEntityDistance(int entityId, double maxDistance) {
        if (maxDistance < 0.0 || !Double.isFinite(maxDistance)) {
            return (player, data) -> false;
        }
        final double maxSq = maxDistance * maxDistance;
        return (player, data) -> {
            if (player == null || player.world == null) return false;
            Entity target = player.world.getEntityByID(entityId);
            return target != null && !target.isDead && player.getDistanceSq(target) <= maxSq;
        };
    }
}

