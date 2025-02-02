package us.potatoboy.invview;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Dynamic;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.dimension.DimensionType;
import us.potatoboy.invview.gui.SavingPlayerDataGui;

public class ViewCommand {

    private static final MinecraftServer minecraftServer = InvView.getMinecraftServer();
    private static final String msgProtected = "Requested inventory is protected";

    public static int inv(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ServerPlayerEntity requestedPlayer = getRequestedPlayer(context);

        if (player == null) {
            return 1;
        }

        if (player.hasPermissionLevel(4)) {
            SimpleGui gui = new SavingPlayerDataGui(ScreenHandlerType.GENERIC_9X5, player, requestedPlayer);
            gui.setTitle(requestedPlayer.getName());
            for (int i = 0; i < requestedPlayer.getInventory().size(); i++) {
                gui.setSlotRedirect(i, new Slot(requestedPlayer.getInventory(), i, 0, 0));
            }

            gui.open();
        } else {
            context.getSource().sendError(Text.literal(msgProtected));
        }

        return 1;
    }

    public static void inv(ServerPlayerEntity player, ServerPlayerEntity playerToView) {
        SimpleGui gui = new SavingPlayerDataGui(ScreenHandlerType.GENERIC_9X5, player, playerToView);
        gui.setTitle(playerToView.getName());
        for (int i = 0; i < playerToView.getInventory().size(); i++) {
            gui.setSlotRedirect(i, new Slot(playerToView.getInventory(), i, 0, 0));
        }

        gui.open();
    }

    public static int eChest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ServerPlayerEntity requestedPlayer = getRequestedPlayer(context);
        EnderChestInventory requestedEchest = requestedPlayer.getEnderChestInventory();

        if (player == null) {
            return 1;
        }

        if (player.hasPermissionLevel(4)) {
            ScreenHandlerType<?> screenHandlerType = switch (requestedEchest.size()) {
                case 9 -> ScreenHandlerType.GENERIC_9X1;
                case 18 -> ScreenHandlerType.GENERIC_9X2;
                case 36 -> ScreenHandlerType.GENERIC_9X4;
                case 45 -> ScreenHandlerType.GENERIC_9X5;
                case 54 -> ScreenHandlerType.GENERIC_9X6;
                default -> ScreenHandlerType.GENERIC_9X3;
            };
            SimpleGui gui = new SavingPlayerDataGui(screenHandlerType, player, requestedPlayer);
            gui.setTitle(requestedPlayer.getName());
            for (int i = 0; i < requestedEchest.size(); i++) {
                gui.setSlotRedirect(i, new Slot(requestedEchest, i, 0, 0));
            }

            gui.open();
        } else {
            context.getSource().sendError(Text.literal(msgProtected));
        }
        return 1;
    }

    public static int origin(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
//        ServerPlayerEntity player = context.getSource().getPlayer();
//        ServerPlayerEntity requestedPlayer = getRequestedPlayer(context);
//
//        boolean canModify = Permissions.check(context.getSource(), permModify, true);
//
//        Permissions.check(requestedPlayer.getUuid(), permProtected, false).thenAcceptAsync(isProtected -> {
//            if (isProtected) {
//                context.getSource().sendError(Text.literal(msgProtected));
//            } else {
//                List<InventoryPower> inventories = PowerHolderComponent.getPowers(requestedPlayer,
//                        InventoryPower.class);
//                if (inventories.isEmpty()) {
//                    context.getSource().sendError(Text.literal("Requested player has no inventory power"));
//                } else {
//                    SimpleGui gui = new SavingPlayerDataGui(ScreenHandlerType.GENERIC_9X5, player, requestedPlayer);
//                    gui.setTitle(requestedPlayer.getName());
//                    int index = 0;
//                    for (InventoryPower inventory : inventories) {
//                        for (int i = 0; i < inventory.size(); i++) {
//                            gui.setSlotRedirect(index, canModify ? new Slot(inventory, i, 0, 0) : new UnmodifiableSlot(inventory, i));
//                            index += 1;
//                        }
//                    }
//
//                    gui.open();
//                }
//            }
//        });

        return 1;
    }

    private static ServerPlayerEntity getRequestedPlayer(CommandContext<ServerCommandSource> context)
            throws CommandSyntaxException {
        GameProfile requestedProfile = GameProfileArgumentType.getProfileArgument(context, "target").iterator().next();
        ServerPlayerEntity requestedPlayer = minecraftServer.getPlayerManager().getPlayer(requestedProfile.getName());

        if (requestedPlayer == null) {
            requestedPlayer = minecraftServer.getPlayerManager().createPlayer(requestedProfile);
            NbtCompound compound = minecraftServer.getPlayerManager().loadPlayerData(requestedPlayer);
            if (compound != null) {
                ServerWorld world = minecraftServer.getWorld(
                        DimensionType.worldFromDimensionNbt(new Dynamic<>(NbtOps.INSTANCE, compound.get("Dimension")))
                                .result().get());

                if (world != null) {
                    requestedPlayer.setWorld(world);
                }
            }
        }

        return requestedPlayer;
    }
}
