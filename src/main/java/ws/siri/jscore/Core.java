package ws.siri.jscore;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.mozilla.javascript.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Core implements ModInitializer {
    public static final String MOD_ID = "fabric-docs-reference";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static yarnwrap.client.MinecraftClient client;
    private static Context rhino;
    private static Scriptable rhinoScope;

    public static yarnwrap.client.MinecraftClient getClient() {
        return client;
    }

    private boolean playerOutdated;

    @Override
    public void onInitialize() {
        client = new yarnwrap.client.MinecraftClient(MinecraftClient.getInstance());

        rhino = Context.enter();
        rhinoScope = rhino.initStandardObjects();

        eval("const modcore = new Packages.ws.siri.jscore.Core;", CatchMode.THROW);
        eval("const client = modcore.getClient();", CatchMode.THROW);
        eval("var player, p;");
        eval("const updatePlayer = () => { player = client.player(); p = new Packages.yarnwrap.entity.Entity(player.wrapperContained); }",
                CatchMode.THROW);
        eval("const console = { log: modcore.log, error: modcore.error }");

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("jseval")
                    .then(ClientCommandManager.argument("code", StringArgumentType.greedyString())
                            .executes(context -> {
                                String code = StringArgumentType.getString(context, "code");
                                log(code, Formatting.GREEN);
                                Optional<Object> res = evalTyped(code);
                                if(res.isEmpty()) return 1;
                                String text = res.get().toString();

                                if(text.equals("undefined")) {
                                    log("undefined", Formatting.GRAY);
                                } else {
                                    Formatting color = Formatting.GRAY;
                                    if(!text.isBlank()) {
                                        color = Formatting.YELLOW;
                                    } else {
                                        text = "[empty]";
                                    }

                                    log(text, color);
                                }
                                return 1;
                            })));
        });

        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, world) -> {
            playerOutdated = true;
        });

        ClientTickEvents.START_CLIENT_TICK.register((mc) -> {
            if (mc.world == null)
                return;

            if (playerOutdated) {
                eval("updatePlayer()");
                playerOutdated = false;
            }
        });

        LOGGER.info("JSCore powered up.");
    }

    public static void log(String s, Formatting formatting) {
        client.wrapperContained.inGameHud.getChatHud().addMessage(Text.literal(s).formatted(formatting));
    }

    public static void log(String s) {
        client.wrapperContained.inGameHud.getChatHud().addMessage(Text.literal(s).formatted(Formatting.GRAY));
    }

    public static void error(String s) {
        client.wrapperContained.inGameHud.getChatHud().addMessage(Text.literal(s).formatted(Formatting.RED));
    }

    @Nullable
    public static Optional<Object> eval(String statement) {
        return eval(statement, CatchMode.PRINT);
    }

    public static Optional<Object> eval(String statement, CatchMode catchMode) {
        try {
            return Optional.of(rhino.evaluateString(rhinoScope, statement, "eval", 1, null));
        } catch (Exception e) {
            switch (catchMode) {
                case THROW:
                    throw new RuntimeException(e.getMessage());
                case PRINT:
                    error(e.getMessage());
                case QUIET:
                    LOGGER.error(e.getMessage());
            }
        }

        return Optional.empty();
    }

    @Nullable
    public static Optional<Object> evalTyped(String statement) {
        return evalTyped(statement, CatchMode.PRINT);
    }

    @Nullable
    public static Optional<Object> evalTyped(String statement, CatchMode catchMode) {
        Optional<Object> res = eval(statement, catchMode);

        if(res.isEmpty()) return res;

        return Optional.of(Context.jsToJava(res.get(), Object.class));
    }
}