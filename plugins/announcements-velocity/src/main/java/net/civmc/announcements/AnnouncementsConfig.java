package net.civmc.announcements;

import java.util.List;

/**
 * @implNote property must be named the same as in the config file, must also have getters and setters else snakeyaml doesn't like it
 */
public class AnnouncementsConfig {

    public static class ScheduledAnnouncement {
        private String cron;
        private String message;

        public String getMessage() {
            return message;
        }
        public void setMessage(String message) {
            this.message = message;
        }
        public String getCron() {
            return cron;
        }
        public void setCron(String cron) {
            this.cron = cron;
        }
    }

    private List<ScheduledAnnouncement> scheduledAnnouncements;

    public List<ScheduledAnnouncement> getScheduledAnnouncements() {
        return scheduledAnnouncements;
    }
    public void setScheduledAnnouncements(List<ScheduledAnnouncement> scheduledAnnouncements) {
        this.scheduledAnnouncements = scheduledAnnouncements;
    }
}
