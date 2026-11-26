package sn.senthioonodiexna.config;

import sn.senthioonodiexna.model.Role;
import sn.senthioonodiexna.model.Utilisateur;

public class AppSession {

    private static AppSession instance;
    private Utilisateur currentUser;

    private AppSession() {}

    public static synchronized AppSession getInstance() {
        if (instance == null) {
            instance = new AppSession();
        }
        return instance;
    }

    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;
    }

    public Utilisateur getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean hasRole(Role role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    public boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    public boolean isResponsable() {
        return hasRole(Role.RESPONSABLE);
    }

    public boolean isAgent() {
        return hasRole(Role.AGENT);
    }

    public boolean isCitoyen() {
        return hasRole(Role.CITOYEN);
    }

    public boolean isAgentOrAbove() {
        return isLoggedIn() && (currentUser.getRole() == Role.AGENT
                || currentUser.getRole() == Role.RESPONSABLE
                || currentUser.getRole() == Role.ADMIN);
    }

    public boolean isResponsableOrAdmin() {
        return isLoggedIn() && (currentUser.getRole() == Role.RESPONSABLE
                || currentUser.getRole() == Role.ADMIN);
    }

    public void clear() {
        this.currentUser = null;
    }
}
