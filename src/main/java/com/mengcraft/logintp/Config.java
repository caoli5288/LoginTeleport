package com.mengcraft.logintp;

/**
 * Created on 16-3-24.
 */
public class Config {

    private final Main main;

    private boolean portalQuit;
    private boolean portalRespawn;

    public Config(Main main) {
        this.main = main;
    }

    public boolean isPortalQuit() {
        return portalQuit;
    }

    public void setPortalQuit(boolean portalQuit) {
        this.portalQuit = portalQuit;
    }

    public boolean isPortalRespawn() {
        return portalRespawn;
    }

    public void setPortalRespawn(boolean portalRespawn) {
        this.portalRespawn = portalRespawn;
    }

    public void load() {
        setPortalQuit(main.getConfig().getBoolean("portal.quit"));
        setPortalRespawn(main.getConfig().getBoolean("portal.respawn"));
    }

    public void save() {
        main.saveConfig();
    }

}
