package com.skyline.control;

interface Control {
	boolean isForbidden(String permissionName, int UID);
    int getForbiddenStatus(String permissionName);
    boolean setForbiden(String permissionName);
    int setPermission(String permissionName);
    
    boolean isEnhanced(String permissionName, int UID);
    int getEnhancedStatus(String permissionName);
    int setEnhanced(String permissionName);
    int setNoEnhanced(String permissionName);
    
    void addToWhiteList(int UID);
}