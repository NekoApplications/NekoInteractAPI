package icu.takeneko.interact.test.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import icu.takeneko.interact.api.mcdreforged.MCDReforged;
import icu.takeneko.interact.mcdr.PermissionLevel;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CommandManager.class)
public abstract class CommandManagerMixin {
    @Shadow @Final private CommandDispatcher<ServerCommandSource> dispatcher;

    @Shadow
    public static LiteralArgumentBuilder<ServerCommandSource> literal(String literal) {
        return null;
    }

    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "<init>", at = @At("RETURN"))
    void inj(CommandManager.RegistrationEnvironment environment, CommandRegistryAccess commandRegistryAccess, CallbackInfo ci){
        this.dispatcher.register(literal("test").executes(ctx -> {
            try {
                String commandString = "!!MCDR ";
                List<String> suggestions = MCDReforged.completeCommand(commandString, commandString.length());
                LOGGER.info("SUGGESTIONS:");
                suggestions.forEach(s -> LOGGER.info("\t- " + s));
                LOGGER.info(MCDReforged.executeCommand("!!MCDR plugin list"));
                MCDReforged.Permission.listPermissions().forEach((permissionLevel, strings) -> {
                    LOGGER.info("Permission Level: " + permissionLevel);
                    strings.forEach(s -> LOGGER.info("\t- " + s));
                });
                MCDReforged.Permission.setPlayerPermission("ZhuRuoLing", PermissionLevel.OWNER);
                LOGGER.info("Permission Level of ZhuRuoLing is " + MCDReforged.Permission.getPlayerPermission("ZhuRuoLing"));
                MCDReforged.Permission.setPlayerPermission("ZhuRuoLing", PermissionLevel.USER);
                LOGGER.info("Permission Level of ZhuRuoLing is " + MCDReforged.Permission.getPlayerPermission("ZhuRuoLing"));
            }catch (Throwable e){

            }
            return 0;
        }));
    }
}
