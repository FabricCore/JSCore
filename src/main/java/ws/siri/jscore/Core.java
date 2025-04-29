package ws.siri.jscore;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.mozilla.javascript.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ws.siri.jscore.wraps.CmdRunnableCore;

public class Core implements ModInitializer {
    public static final String MOD_ID = "jscore";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Context rhino;
    public static Scriptable rhinoScope;

    public static CmdRunnableCore runnable = new CmdRunnableCore();

    @Override
    public void onInitialize() {
        rhino = Context.enter();
        rhinoScope = rhino.initStandardObjects();

        Loader.init();

        LOGGER.info("JSCore powered up.");
    }

    public static void log(String s) {
        if(MinecraftClient.getInstance().inGameHud == null) LOGGER.info(s);
        else MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal(s).formatted(Formatting.GRAY));
    }

    public static void error(String s) {
        if(MinecraftClient.getInstance().inGameHud == null) LOGGER.info(s);
        else MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal(s).formatted(Formatting.RED));
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
}