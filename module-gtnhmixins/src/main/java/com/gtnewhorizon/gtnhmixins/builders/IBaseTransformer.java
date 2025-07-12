package com.gtnewhorizon.gtnhmixins.builders;

public interface IBaseTransformer {

    /**
     * Phase is only used for early and late mixins from gtnh mixins
     */
    enum Phase {
        EARLY,
        LATE
    }

    enum Side {
        COMMON,
        CLIENT,
        SERVER
    }
}
