package icu.takeneko.interact.mixin;

import icu.takeneko.interact.network.NekoService;
import icu.takeneko.interact.server.CommandCompleteService;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "startServer", at = @At("RETURN"))
    static <S extends MinecraftServer> void onServerStart(Function<Thread, S> serverFactory, CallbackInfoReturnable<S> cir){
        NekoService.INSTANCE.registerService("server_suggest_command", new CommandCompleteService(cir.getReturnValue()));
    }
}
