package com.mengcraft.logintp;

/**
 * Created on 16-3-24.
 */
public enum Mgr {

    INSTANCE;

    private boolean portalVoid;
    private boolean portalQuit;
    private boolean portalPortal;
    private boolean portalSpawn;
    private boolean portalFalling;

    private Main main;

    public boolean isPortalQuit() {
        return portalQuit;
    }

    public void setPortalQuit(boolean portalQuit) {
        this.portalQuit = portalQuit;
    }

    public void load(Main main) {
        this.main = main;
        setPortalQuit(main.getConfig().getBoolean("portal.quit"));
        setPortalVoid(main.getConfig().getBoolean("portal.void"));
        portalPortal = main.getConfig().getBoolean("portal.portal");
        portalSpawn = main.getConfig().getBoolean("portal.spawn");
        portalFalling = main.getConfig().getBoolean("portal.falling");
    }

    public boolean isPortalVoid() {
        return portalVoid;
    }

    public void setPortalVoid(boolean portalVoid) {
        this.portalVoid = portalVoid;
    }

    public boolean isPortalPortal() {
        return portalPortal;
    }

    public void setPortalPortal(boolean portalPortal) {
        this.portalPortal = portalPortal;
    }

    public boolean isPortalSpawn() {
        return portalSpawn;
    }

    public void setPortalSpawn(boolean portalSpawn) {
        this.portalSpawn = portalSpawn;
    }

    public boolean isPortalFalling() {
        return portalFalling;
    }

    public void setPortalFalling(boolean portalFalling) {
        this.portalFalling = portalFalling;
    }

    public void save() {
        main.saveConfig();
    }

}
