package ws.siri.jscore.wraps;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStarted;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStopping;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndWorldTick;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.StartTick;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.StartWorldTick;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.world.ServerWorld;
import ws.siri.jscore.Core;

public class Runnable implements
    ClientStarted, ClientStopping, // ClientLifecycleEvents.java
    StartTick, EndTick, StartWorldTick, EndWorldTick // ClientTickEvents.java
     {
    String source;
    String code;

    public Runnable(String source) {
        this.source = source;
        this.code = String.format("%s();", source);
    }

    public static Runnable create(String source) {
        return new Runnable(source);
    }

    public void run() {
        Core.eval(code);
    }

    @Override
    public void onClientStarted(MinecraftClient client) {
        run();
    }

    @Override
    public void onClientStopping(MinecraftClient client) {
        run();
    }

    @Override
    public void onStartTick(MinecraftClient client) {
        run();
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        run();
    }

    @Override
    public void onStartTick(ServerWorld world) {
        run();
    }

    @Override
    public void onEndTick(ClientWorld world) {
        run();
    }
}
