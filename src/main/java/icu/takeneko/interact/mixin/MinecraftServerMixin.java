package icu.takeneko.interact.mixin;

import icu.takeneko.interact.network.NekoService;
import icu.takeneko.interact.server.CommandCompleteService;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow public abstract boolean isRunning();

    @Inject(method = "startServer", at = @At("RETURN"))
    private static <S extends MinecraftServer> void onServerStart(Function<Thread, S> serverFactory, CallbackInfoReturnable<S> cir){
        NekoService.INSTANCE.registerService("server_suggest_command", new CommandCompleteService(cir.getReturnValue()));
    }

    @Inject(method = "stop",at = @At("HEAD"))
    void onServerStop(boolean waitForShutdown, CallbackInfo ci){
        if (this.isRunning()) {
            NekoService.INSTANCE.shutdown();
        }
    }
}
