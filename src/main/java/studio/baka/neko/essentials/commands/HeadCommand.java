package studio.baka.neko.essentials.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class HeadCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("head")
                .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                        .executes((context) -> execute(context.getSource(), context.getSource().getPlayer(),
                                GameProfileArgumentType.getProfileArgument(context, "player").iterator().next())))
                .executes((context) -> execute(context.getSource(), context.getSource().getPlayer(),
                        context.getSource().getPlayer().getGameProfile())));
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player, GameProfile profile) throws CommandSyntaxException {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("SkullOwner", profile.getName());
        ItemStackArgument item = new ItemStackArgument(Registry.ITEM.get(new Identifier("minecraft:player_head")), nbt);
        ItemStack itemStack = item.createStack(1, false);
        boolean bl = player.getInventory().insertStack(itemStack);
        if (bl && itemStack.isEmpty()) {
            itemStack.setCount(1);
            ItemEntity itemEntity = player.dropItem(itemStack, false);
            if (itemEntity != null) {
                itemEntity.setDespawnImmediately();
            }

            player.world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F,
                    ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
            player.currentScreenHandler.sendContentUpdates();
        } else {
            ItemEntity itemEntity = player.dropItem(itemStack, false);
            if (itemEntity != null) {
                itemEntity.resetPickupDelay();
                itemEntity.setOwner(player.getUuid());
            }
        }
        return 0;
    }
}
