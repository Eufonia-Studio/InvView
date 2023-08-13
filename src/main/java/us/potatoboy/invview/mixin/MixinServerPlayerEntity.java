package us.potatoboy.invview.mixin;

import eu.pb4.sgui.virtual.inventory.VirtualScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayerEntity {

    @Inject(method = "changeGameMode", at = @At(value = "HEAD"))
    public void onChangeGameMode(GameMode gameMode, CallbackInfoReturnable<Boolean> cir) {
        final ServerPlayerEntity PLAYER = (ServerPlayerEntity) (Object) this;
        if (!PLAYER.isSpectator()) {
            return;
        }

        if (gameMode.isSurvivalLike()) {
            if (PLAYER.currentScreenHandler instanceof VirtualScreenHandler virtualScreenHandler) {
                virtualScreenHandler.close(PLAYER);
            }
        }
    }
}
