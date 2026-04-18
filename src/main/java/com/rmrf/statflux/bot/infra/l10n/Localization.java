package com.rmrf.statflux.bot.infra.l10n;

public class Localization {
    public Start start;
    public Stats stats;
    public Link link;
    public Common common;

    public static class Start {
        public String greeting;
    }

    public static class Stats {
        public String introduction;
        public String totalViews;
        public String totalLinkCount;
        public String refresh;
        public String noVideos;
    }

    public static class Link {
        public String videoStatistics;
        public String statsMotivationText;
        public String incorrect;
    }

    public static class Common {
        public String useKnownCommands;
    }
}
