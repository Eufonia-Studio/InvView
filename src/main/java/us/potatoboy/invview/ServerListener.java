package us.potatoboy.invview;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ServerListener {

    public static void register(){
        UseEntityCallback.EVENT.register(ServerListener::onPlayerInteractEntity);
    }

    private static ActionResult onPlayerInteractEntity(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult entityHitResult) {
        if (!(player instanceof ServerPlayerEntity playerObserver)) {
            return ActionResult.PASS;
        }

        if(!playerObserver.isSpectator()){
            return ActionResult.PASS;
        }

        if (!(entity instanceof ServerPlayerEntity playerToSee)) {
            return ActionResult.PASS;
        }

        ViewCommand.inv(playerObserver, playerToSee);
        return ActionResult.SUCCESS;
    }
}
