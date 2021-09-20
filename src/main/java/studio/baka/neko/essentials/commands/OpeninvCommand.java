package studio.baka.neko.essentials.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldSaveHandler;
import org.jetbrains.annotations.Nullable;
import studio.baka.neko.essentials.mixin.MixinPlayerManagerAccessor;
import studio.baka.neko.essentials.mixinInterfaces.IMixinWorldSaveHandler;

import java.util.Arrays;

import static studio.baka.neko.essentials.NekoEssentials.logger;

public class OpeninvCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("openinv")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                        .executes((context) -> execute(context.getSource(), context.getSource().getPlayer(),
                                GameProfileArgumentType.getProfileArgument(context, "player").iterator().next()))));
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player, GameProfile profile) throws CommandSyntaxException {
        ServerPlayerEntity targerPlayer = source.getServer().getPlayerManager().getPlayer(profile.getId());
        if (targerPlayer != null) {
            return execute(source, player, targerPlayer);
        } else {
            WorldSaveHandler saveHandler = ((MixinPlayerManagerAccessor) source.getServer().getPlayerManager()).getSaveHandler();
            @Nullable NbtCompound playerData = ((IMixinWorldSaveHandler) saveHandler).loadPlayerData(profile);
            if (playerData == null)
                throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();

            PlayerEntity playerEntity = new PlayerEntity(source.getServer().getOverworld(), BlockPos.ORIGIN, 0, profile) {
                public boolean isSpectator() {
                    return false;
                }

                public boolean isCreative() {
                    return false;
                }
            };

            PlayerInventory playerInventory = new PlayerInventory(playerEntity);
            playerInventory.readNbt(playerData.getList("Inventory", 10));

            OpeninvInventory inventory = new OpeninvOfflineInventory(playerInventory, saveHandler, profile, source.getServer());
            openinv(player, playerEntity, inventory);
        }

        return 0;
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player, ServerPlayerEntity target) {

        OpeninvInventory inventory = new OpeninvOnlineInventory(target.getInventory(), target);
        openinv(player, target, inventory);

        return 0;
    }

    private static void openinv(ServerPlayerEntity player, PlayerEntity target, OpeninvInventory inventory) {
        logger.info(String.format("[openinv] %s -> %s", player, target));
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInv, playerT) ->
                new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X5, syncId, playerInv, inventory, 5),
                Text.of(target.getName().asString() + "'s inventory")));
    }
}

class OpeninvOfflineInventory extends OpeninvInventory {
    private final WorldSaveHandler saveHandler;
    private final GameProfile profile;
    private final MinecraftServer server;

    public OpeninvOfflineInventory(PlayerInventory playerInv, WorldSaveHandler saveHandler, GameProfile profile, MinecraftServer server) {
        super(playerInv);
        this.saveHandler = saveHandler;
        this.profile = profile;
        this.server = server;
    }

    @Override
    public void onClose(PlayerEntity player) {
        super.onClose(player);
        @Nullable NbtCompound playerData = ((IMixinWorldSaveHandler) saveHandler).loadPlayerData(profile);
        if (playerData == null) return;
        playerData.put("Inventory", this.playerInventory.writeNbt(new NbtList()));
        ((IMixinWorldSaveHandler) saveHandler).savePlayerData(profile, playerData);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return super.canPlayerUse(player) &&
                !Arrays.asList(server.getPlayerManager().getPlayerNames()).contains(profile.getName());
    }
}

class OpeninvOnlineInventory extends OpeninvInventory {
    private final ServerPlayerEntity owner;

    public OpeninvOnlineInventory(PlayerInventory playerInv, ServerPlayerEntity owner) {
        super(playerInv);
        this.owner = owner;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return super.canPlayerUse(player) && !owner.isDisconnected();
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
        return true;
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