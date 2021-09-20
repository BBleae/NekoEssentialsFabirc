package studio.baka.neko.essentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static studio.baka.neko.essentials.NekoEssentials.logger;

public class OpeninvCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("openinv")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("target", EntityArgumentType.player())
                        .executes((context) -> execute(context.getSource(), context.getSource().getPlayer(),
                                EntityArgumentType.getPlayer(context, "target")))));
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player, ServerPlayerEntity target) {

        Inventory inventory = new OpeninvInventory(target.getInventory());

        logger.info(String.format("[openinv] %s -> %s", player, target));
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInv, playerT) ->
                new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X5, syncId, playerInv, inventory, 5),
                Text.of(target.getName().asString() + "'s inventory")));
        return 0;
    }
}

class OpeninvInventory implements Inventory {
    public PlayerInventory playerInventory;

    public OpeninvInventory(PlayerInventory playerInv) {
        playerInventory = playerInv;
    }

    @Override
    public int size() {
        return 45;
    }

    @Override
    public boolean isEmpty() {
        return playerInventory.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        slot = mapSlot(slot);
        if (slot == -1) return new ItemStack(Items.BARRIER);
        return playerInventory.getStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        slot = mapSlot(slot);
        if (slot == -1) return ItemStack.EMPTY;
        return playerInventory.removeStack(slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        slot = mapSlot(slot);
        if (slot == -1) return ItemStack.EMPTY;
        return playerInventory.removeStack(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        slot = mapSlot(slot);
        if (slot == -1) return;
        playerInventory.setStack(slot, stack);
    }

    @Override
    public void markDirty() {
        playerInventory.markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return playerInventory.canPlayerUse(player);
    }

    @Override
    public void clear() {
        playerInventory.clear();
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return playerInventory.isValid(slot, stack) && mapSlot(slot) != -1;
    }

    private int mapSlot(int slot) {
        if (slot < 5) {
            switch (slot) {
                case 0 -> {
                    return 39;
                }
                case 1 -> {
                    return 38;
                }
                case 2 -> {
                    return 37;
                }
                case 3 -> {
                    return 36;
                }
                case 4 -> {
                    return 40;
                }
                default -> {
                    return -1;
                }
            }
        } else if (slot < 9) {
            return -1;
        } else if (slot < 36) {
            return slot;
        } else {
            return slot - 36;
        }
    }
}