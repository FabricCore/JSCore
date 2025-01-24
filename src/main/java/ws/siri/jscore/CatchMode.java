package ws.siri.jscore;

public enum CatchMode {
    THROW,
    PRINT,
    QUIET;

    public void handle(Object e) {
        switch (this) {
            case THROW:
                throw new RuntimeException(e.toString());
            case PRINT:
                Core.error(e.toString());
            case QUIET:
                Core.LOGGER.error(e.toString());
        }
    }
}