package sn.senthioonodiexna.service;

import sn.senthioonodiexna.dao.NotificationDao;
import sn.senthioonodiexna.dao.impl.NotificationDaoImpl;
import sn.senthioonodiexna.model.Notification;

import java.util.List;

public class NotificationService {

    private final NotificationDao notificationDao;

    public NotificationService() {
        this(new NotificationDaoImpl());
    }

    public NotificationService(NotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }

    public Notification notifyUser(int idUtilisateur, String titre, String message, Integer idDossier) {
        Notification notif = new Notification(titre, message, idUtilisateur, idDossier);
        return notificationDao.create(notif);
    }

    public List<Notification> getNotificationsForUser(int idUtilisateur) {
        return notificationDao.findByUtilisateur(idUtilisateur);
    }

    public int getUnreadCount(int idUtilisateur) {
        return notificationDao.countUnreadByUtilisateur(idUtilisateur);
    }

    public boolean markAsRead(int idNotification) {
        return notificationDao.markAsRead(idNotification);
    }

    public boolean markAllAsRead(int idUtilisateur) {
        return notificationDao.markAllAsRead(idUtilisateur);
    }

    public boolean deleteNotification(int idNotification) {
        return notificationDao.delete(idNotification);
    }
}
