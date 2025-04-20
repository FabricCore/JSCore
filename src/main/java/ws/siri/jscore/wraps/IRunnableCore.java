package ws.siri.jscore.wraps;

import java.util.HashMap;

import org.mozilla.javascript.Function;

import ws.siri.jscore.Core;

public abstract class IRunnableCore<R extends IRunnable> {
    public HashMap<String, R> runnables = new HashMap<>();
    private Class<R> c;

    public IRunnableCore(Class<R> c) {
        this.c = c;
    }

    public R runnable(String ident, String function) {
        try {
            if(runnables.containsKey(ident)) {
                Function f = Core.rhino.compileFunction(Core.rhinoScope, function, ident, 1, null);
                runnables.get(ident).f = f;
                return runnables.get(ident);
            } else {
                R created = c.getDeclaredConstructor(String.class, String.class).newInstance(ident, function);
                runnables.put(ident, created);
                return created;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}