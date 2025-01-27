package ws.siri.jscore.wraps;

import java.util.HashMap;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStarted;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStopping;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndWorldTick;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.StartTick;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.StartWorldTick;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.world.ServerWorld;
import ws.siri.jscore.Core;

public class Runnable implements
        ClientStarted, ClientStopping, // ClientLifecycleEvents.java
        StartTick, EndTick, StartWorldTick, EndWorldTick, // ClientTickEvents.java

        ClientCommandRegistrationCallback, Command<FabricClientCommandSource>, // brigadier
        java.lang.Runnable
{
    public static HashMap<String, Runnable> runnables = new HashMap<>();
    private Function f;
    private Object[] spawnArgs;

    private Runnable(String ident, String function) {
        f = Core.rhino.compileFunction(Core.rhinoScope, function, ident, 1, null);
        runnables.put(ident, this);
    }

    public static Runnable create(String ident, String function) {
        if (runnables.containsKey(ident)) {
            Function f = Core.rhino.compileFunction(Core.rhinoScope, function, ident, 1, null);
            runnables.get(ident).f = f;
            return runnables.get(ident);
        } else {
            return new Runnable(ident, function);
        }
    }

    public void runF(Object... args) {
        f.call(Core.rhino, Core.rhinoScope, null, args);
    }

    public void runFCtx(Context ctx, Object... args) {
        f.call(ctx, Core.rhinoScope, null, args);
    }

    @Override
    public void onClientStarted(MinecraftClient client) {
        runF(new yarnwrap.client.MinecraftClient(client));
    }

    @Override
    public void onClientStopping(MinecraftClient client) {
        runF(new yarnwrap.client.MinecraftClient(client));
    }

    @Override
    public void onStartTick(MinecraftClient client) {
        runF(new yarnwrap.client.MinecraftClient(client));
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        runF(new yarnwrap.client.MinecraftClient(client));
    }

    @Override
    public void onStartTick(ServerWorld world) {
        runF(new yarnwrap.server.world.ServerWorld(world));
    }

    @Override
    public void onEndTick(ClientWorld world) {
        runF(new yarnwrap.client.world.ClientWorld(world));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        runF(context);
        return 1;
    }

    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher,
            CommandRegistryAccess registryAccess) {
        runF(dispatcher, registryAccess);
    }

    @Override
    public void run() {
        Context ctx = Context.enter();
        runFCtx(ctx, spawnArgs);
    }

    public void spawn(Object... spawnArgs) {
        this.spawnArgs = spawnArgs;
        new Thread(this).start();
    }
}
