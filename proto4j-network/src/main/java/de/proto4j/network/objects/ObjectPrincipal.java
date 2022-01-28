package de.proto4j.network.objects; //@date 28.01.2022

import java.security.Principal;

public class ObjectPrincipal implements Principal {
    private final String username, context;

    public ObjectPrincipal(String username, String context) {
        if (username == null || context == null) {
            throw new NullPointerException();
        }
        this.username = username;
        this.context  = context;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ObjectPrincipal)) {
            return false;
        }
        ObjectPrincipal other = (ObjectPrincipal) o;
        return username.equals(other.username) && context.equals(other.context);
    }

    @Override
    public String getName() {
        return String.join(":", context, username);
    }

    public String getContext() {
        return context;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public int hashCode() {
        return (username + context).hashCode();
    }

    @Override
    public String toString() {
        return username;
    }
}
