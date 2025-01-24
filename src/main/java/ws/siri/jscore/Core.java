package ws.siri.jscore;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Optional;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.Nullable;
import org.mozilla.javascript.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.arguments.StringArgumentType;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ws.siri.jscore.wraps.Runnable;

public class Core implements ModInitializer {
    public static final String MOD_ID = "jscore";
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

        Loader.init();

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

        // java.util.function.Function f = (mc) -> {};

        // ClientTickEvents.END_CLIENT_TICK.register(f);

        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, world) -> {
            playerOutdated = true;
        });

        ClientTickEvents.START_CLIENT_TICK.register((mc) -> {
            if (mc.world == null)
                return;

            if (playerOutdated) {
                eval("updatePlayer();");
                playerOutdated = false;
            }
        });

        LOGGER.info("JSCore powered up.");
    }

    public static void log(String s, Formatting formatting) {
        if(client.wrapperContained.inGameHud == null) LOGGER.info(s);
        else client.wrapperContained.inGameHud.getChatHud().addMessage(Text.literal(s).formatted(formatting));
    }

    public static void log(String s) {
        if(client.wrapperContained.inGameHud == null) LOGGER.info(s);
        else client.wrapperContained.inGameHud.getChatHud().addMessage(Text.literal(s).formatted(Formatting.GRAY));
    }

    public static void error(String s) {
        if(client.wrapperContained.inGameHud == null) LOGGER.info(s);
        else client.wrapperContained.inGameHud.getChatHud().addMessage(Text.literal(s).formatted(Formatting.RED));
    }

    public static Object evalUncatched(String statement, String file) throws Exception {
        return rhino.evaluateString(rhinoScope, statement, file, 1, null);
    }

    public static Optional<Object> eval(String statement) {
        return eval(statement, CatchMode.PRINT);
    }

    public static Optional<Object> eval(String statement, CatchMode catchMode, String name) {
        try {
            return Optional.of(evalUncatched(statement, name));
        } catch (Exception e) {
            catchMode.handle(e);
        }

        return Optional.empty();
    }

    public static Optional<Object> eval(String statement, CatchMode catchMode) {
        return eval(statement, catchMode, "unamed");
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

    public static Runnable runnable(String source) {
        return new Runnable(source);
    }
}