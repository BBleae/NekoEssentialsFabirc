package studio.baka.neko.essentials.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.WorldSaveHandler;
import org.jetbrains.annotations.Nullable;
import studio.baka.neko.essentials.mixin.MixinPlayerManagerAccessor;
import studio.baka.neko.essentials.mixinInterfaces.IMixinWorldSaveHandler;

import java.util.Arrays;

import static studio.baka.neko.essentials.NekoEssentials.logger;

public class OpenenderCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("openender")
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

            EnderChestInventory enderInventory = new EnderChestInventory();
            enderInventory.readNbtList(playerData.getList("EnderItems", 10));

            OpenenderOfflineInventory inventory = new OpenenderOfflineInventory(enderInventory, saveHandler, profile, source.getServer());
            openender(player, profile, inventory);
        }

        return 0;
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity player, ServerPlayerEntity target) {

        OpenenderOnlineInventory inventory = new OpenenderOnlineInventory(target.getEnderChestInventory(), target);
        openender(player, target.getGameProfile(), inventory);

        return 0;
    }

    private static void openender(ServerPlayerEntity player, GameProfile target, OpenenderInventory inventory) {
        logger.info(String.format("[openender] %s -> %s", player, target));
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInv, playerT) ->
                new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, syncId, playerInv, inventory, 3),
                Text.of(target.getName() + "'s ender chest")));
    }
}

class OpenenderOfflineInventory extends OpenenderInventory {
    private final WorldSaveHandler saveHandler;
    private final GameProfile profile;
    private final MinecraftServer server;

    public OpenenderOfflineInventory(EnderChestInventory playerInv, WorldSaveHandler saveHandler, GameProfile profile, MinecraftServer server) {
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
        playerData.put("EnderItems", this.enderInventory.toNbtList());
        ((IMixinWorldSaveHandler) saveHandler).savePlayerData(profile, playerData);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return super.canPlayerUse(player) &&
                !Arrays.asList(server.getPlayerManager().getPlayerNames()).contains(profile.getName());
    }
}

class OpenenderOnlineInventory extends OpenenderInventory {
    private final ServerPlayerEntity owner;

    public OpenenderOnlineInventory(EnderChestInventory playerInv, ServerPlayerEntity owner) {
        super(playerInv);
        this.owner = owner;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return super.canPlayerUse(player) && !owner.isDisconnected();
    }
}

class OpenenderInventory implements Inventory {
    public EnderChestInventory enderInventory;

    public OpenenderInventory(EnderChestInventory playerInv) {
        enderInventory = playerInv;
    }

    @Override
    public int size() {
        return 27;
    }

    @Override
    public boolean isEmpty() {
        return enderInventory.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return enderInventory.getStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return enderInventory.removeStack(slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return enderInventory.removeStack(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        enderInventory.setStack(slot, stack);
    }

    @Override
    public void markDirty() {
        enderInventory.markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        enderInventory.clear();
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return true;
    }
}