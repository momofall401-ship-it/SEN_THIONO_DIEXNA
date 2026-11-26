package sn.senthioonodiexna.dao;

import sn.senthioonodiexna.model.Notification;

import java.util.List;

public interface NotificationDao {
    Notification create(Notification notification);
    List<Notification> findByUtilisateur(int idUtilisateur);
    int countUnreadByUtilisateur(int idUtilisateur);
    boolean markAsRead(int idNotification);
    boolean markAllAsRead(int idUtilisateur);
    boolean delete(int idNotification);
}
