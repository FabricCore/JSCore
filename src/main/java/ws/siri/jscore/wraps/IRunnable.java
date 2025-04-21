package ws.siri.jscore.wraps;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;

import ws.siri.jscore.Core;

public abstract class IRunnable {
    public Function f;

    public IRunnable(String ident, String function) {
        f = Core.rhino.compileFunction(Core.rhinoScope, function, ident, 1, null);
    }

    public Object runF(Object... args) {
        return f.call(Core.rhino, Core.rhinoScope, null, args);
    }

    public Object runFCtx(Context ctx, Object... args) {
        return f.call(ctx, Core.rhinoScope, null, args);
    }

    class RunnableDetached implements java.lang.Runnable {
        private final Object[] spawnArgs;
        private final IRunnable runnable;

        public RunnableDetached(IRunnable runnable, Object... spawnArgs) {
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
