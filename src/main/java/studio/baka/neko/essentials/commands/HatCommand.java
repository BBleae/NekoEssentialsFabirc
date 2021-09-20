package studio.baka.neko.essentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import static studio.baka.neko.essentials.NekoEssentials.logger;

public class HatCommand {
    private static final SimpleCommandExceptionType NO_ITEM_EXCEPTION =
            new SimpleCommandExceptionType(Text.of("请将要穿戴的物品放在主手"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("hat")
                .executes((context) -> execute(context.getSource(), context.getSource().getPlayer())));
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player) throws CommandSyntaxException {
        ItemStack itemStack = player.getMainHandStack();
        if (itemStack.isEmpty())
            throw NO_ITEM_EXCEPTION.create();

        ItemStack hat = player.getEquippedStack(EquipmentSlot.HEAD);
        ItemStack itemStackCopy = itemStack.copy();
        itemStackCopy.setCount(1);
        player.equipStack(EquipmentSlot.HEAD, itemStackCopy);
        itemStack.decrement(1);
        if (!hat.isEmpty()) {
            boolean bl = player.getInventory().insertStack(hat);
            if (bl && hat.isEmpty()) {
                hat.setCount(1);
                ItemEntity itemEntity = player.dropItem(hat, false);
                if (itemEntity != null) {
                    itemEntity.setDespawnImmediately();
                }

                player.world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F,
                        ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                player.currentScreenHandler.sendContentUpdates();
            } else {
                ItemEntity itemEntity = player.dropItem(hat, false);
                if (itemEntity != null) {
                    itemEntity.resetPickupDelay();
                    itemEntity.setOwner(player.getUuid());
                }
            }
        }

        logger.info(String.format("[hat] %s with %s", player, itemStackCopy));
        source.sendFeedback(new LiteralText("已穿戴 ").append(itemStackCopy.toHoverableText()), false);

        return 0;
    }
}
