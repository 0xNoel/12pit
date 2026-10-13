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
package pit12.platform.mixin.input;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pit12.runtime.input.InputBlockBinding;
import pit12.runtime.input.InputBlocker;

@Mixin(EntityPlayerSP.class)
public abstract class EntityPlayerSPMixin extends AbstractClientPlayer {
    @Shadow
    protected Minecraft mc;

    protected EntityPlayerSPMixin(World world, GameProfile profile) {
        super(world, profile);
    }

    @Unique
    private boolean pit12$inputLocked() {
        InputBlocker blocker = ((InputBlockBinding) mc).pit12$inputBlocker();
        return blocker != null && blocker.locked();
    }

    @Inject(method = "onLivingUpdate",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/util/MovementInput;updatePlayerMoveState()V",
                    shift = At.Shift.AFTER))
    private void pit12$suppressMovement(CallbackInfo callback) {
        InputBlocker blocker = ((InputBlockBinding) mc).pit12$inputBlocker();
        if (blocker != null)
            blocker.suppressMovement();
    }

    @Override
    protected boolean isMovementBlocked() {
        return pit12$inputLocked() || super.isMovementBlocked();
    }

    @Override
    public void jump() {
        if (!pit12$inputLocked())
            super.jump();
    }

    // Keep physics running while blocking acceleration from player actions.
    @Override
    public void moveFlying(float strafe, float forward, float friction) {
        if (!pit12$inputLocked())
            super.moveFlying(strafe, forward, friction);
    }

    @Override
    public void setJumping(boolean jumping) {
        super.setJumping(jumping && !pit12$inputLocked());
    }

    @Override
    public void setAngles(float yaw, float pitch) {
        if (!pit12$inputLocked())
            super.setAngles(yaw, pitch);
    }

    @Override
    public void attackTargetEntityWithCurrentItem(Entity target) {
        if (!pit12$inputLocked())
            super.attackTargetEntityWithCurrentItem(target);
    }

    @Override
    public void setItemInUse(ItemStack stack, int duration) {
        if (!pit12$inputLocked())
            super.setItemInUse(stack, duration);
    }

    @ModifyVariable(method = "setSprinting", at = @At("HEAD"), argsOnly = true)
    private boolean pit12$blockSprint(boolean sprinting) {
        return sprinting && !pit12$inputLocked();
    }

    @Inject(method = "isSneaking", at = @At("HEAD"), cancellable = true)
    private void pit12$blockSneak(CallbackInfoReturnable<Boolean> callback) {
        if (pit12$inputLocked())
            callback.setReturnValue(false);
    }

    @Inject(method = {"swingItem", "sendHorseJump"}, at = @At("HEAD"), cancellable = true)
    private void pit12$blockAction(CallbackInfo callback) {
        if (pit12$inputLocked())
            callback.cancel();
    }

    @Inject(method = "dropOneItem", at = @At("HEAD"), cancellable = true)
    private void pit12$blockDrop(CallbackInfoReturnable<EntityItem> callback) {
        if (pit12$inputLocked())
            callback.setReturnValue(null);
    }
}
