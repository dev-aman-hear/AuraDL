package com.auradl.api;

public class AccountInfo {
    private String id;
    private String storefront;
    private boolean activeSubscription;

    public AccountInfo(String id, String storefront, boolean activeSubscription) {
        this.id = id;
        this.storefront = storefront;
        this.activeSubscription = activeSubscription;
    }

    public String getId() {
        return id;
    }

    public String getStorefront() {
        return storefront;
    }

    public boolean isActiveSubscription() {
        return activeSubscription;
    }

    @Override
    public String toString() {
        return "AccountInfo{" +
                "id='" + id + '\'' +
                ", storefront='" + storefront + '\'' +
                ", activeSubscription=" + activeSubscription +
                '}';
    }
}
