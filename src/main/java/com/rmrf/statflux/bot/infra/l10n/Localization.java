package com.rmrf.statflux.bot.infra.l10n;

public class Localization {
    public static final String DOUBLE_CARRY = "\n\n";

    public Start start;
    public Stats stats;
    public Link link;
    public Common common;
    public CallbackQueries callbackQueries;
    public TimeFormat timeFormat;

    public static class Start {
        public String greeting;
    }

    public static class Stats {
        public String introduction;
        public String views;
        public String updatedAt;
        public String totalViews;
        public String totalLinkCount;
        public String noVideos;
        public String refresh;
        public String prev;
        public String next;
    }

    public static class Link {
        public String videoAddedSuccessfully;
        public String views;
        public String statsMotivationText;
        public String error;
    }

    public static class Common {
        public String useKnownCommands;
        public String uncaughtError;
    }

    public static class CallbackQueries {
        public String nextError;
        public String prevError;
        public String refreshError;
    }

    public static class TimeFormat {
        public String today;
        public String yesterday;
        public String at;
    }
}
