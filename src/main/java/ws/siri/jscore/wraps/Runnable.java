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
import net.minecraft.command.CommandRegistryAccess;
import ws.siri.jscore.Core;

public class Runnable implements
        ClientCommandRegistrationCallback, Command<FabricClientCommandSource> // brigadier
{
    public static HashMap<String, Runnable> runnables = new HashMap<>();
    public Function f;

    public Runnable(String ident, String function) {
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
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        runF(context);
        return 1;
    }

    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher,
            CommandRegistryAccess registryAccess) {
        runF(dispatcher, registryAccess);
    }
    
    class RunnableDetached implements java.lang.Runnable {
        private final Object[] spawnArgs;
        private final Runnable runnable;

        public RunnableDetached(Runnable runnable, Object... spawnArgs) {
            this.runnable = runnable;
            this.spawnArgs = spawnArgs;
        }

        @Override
        public void run() {
            Context ctx = Context.enter();
            runnable.runFCtx(ctx, spawnArgs);
        }
    }

    public void spawn(Object... spawnArgs) {
        new Thread(new RunnableDetached(this, spawnArgs)).start();
    }
}
