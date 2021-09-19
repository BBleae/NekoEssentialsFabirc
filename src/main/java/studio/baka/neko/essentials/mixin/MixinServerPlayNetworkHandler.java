package studio.baka.neko.essentials.mixin;

import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import studio.baka.neko.essentials.mixinInterfaces.IMixinServerPlayerEntity;

import java.util.Objects;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetworkHandler {
    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    public abstract void requestTeleport(double x, double y, double z, float yaw, float pitch);

    @Shadow
    public abstract void sendPacket(Packet<?> packet);

    @Inject(method = "onPlayerMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V", shift = At.Shift.AFTER), cancellable = true)
    public void beforePlayerMove(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if (!((IMixinServerPlayerEntity) player).getAcceptedRules() &&
                (packet.getX(player.getX()) != player.getX() ||
                        packet.getY(player.getY()) != player.getY() ||
                        packet.getZ(player.getZ()) != player.getZ())) {
            this.requestTeleport(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
            ci.cancel();
        }
    }

    @Inject(method = "onPlayerAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V", shift = At.Shift.AFTER), cancellable = true)
    public void beforePlayerAction(PlayerActionC2SPacket packet, CallbackInfo ci) {
        if (!((IMixinServerPlayerEntity) player).getAcceptedRules()) {
            player.currentScreenHandler.syncState();
            player.playerScreenHandler.syncState();
            ci.cancel();
        }
    }

    @Inject(method = "onClickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V", shift = At.Shift.AFTER), cancellable = true)
    public void beforeClickSlot(ClickSlotC2SPacket packet, CallbackInfo ci) {
        if (!((IMixinServerPlayerEntity) player).getAcceptedRules()) {
            player.currentScreenHandler.syncState();
            player.playerScreenHandler.syncState();
            ci.cancel();
        }
    }

    @Inject(method = "onGameMessage", at = @At(value = "INVOKE", target = "Ljava/lang/String;startsWith(Ljava/lang/String;)Z", shift = At.Shift.BEFORE), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    public void beforePlayerSendMessage(ChatMessageC2SPacket packet, CallbackInfo ci, String string) {
        if (!((IMixinServerPlayerEntity) player).getAcceptedRules() &&
                !Objects.equals(string, "/acceptrule") && !Objects.equals(string, "/denyrule")) {
            this.sendPacket(new OverlayMessageS2CPacket(Text.of("§c你还没有打开聊天框点击§a[同意服务器规定]§c!")));
            ci.cancel();
        }
    }
}
