/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.renCommon.interactionRouter;

/**
 * Author: Rinkako
 * Date  : 2018/3/5
 * Usage : Location of interaction.
 */
public class LocationContext {

    /**
     * Service URL for BO Engine Serialization BO.
     */
    public static final String URL_BOENGINE_SERIALIZEBO = "/gateway/serializeBO";

    /**
     * Service URL for BO Engine Serialization BO.
     */
    public static final String URL_BOENGINE_START = "/gateway/launchProcess";

    /**
     * Service URL for BO Engine event callback.
     */
    public static final String URL_BOENGINE_SPANTREE = "/gateway/getSpanTree";

    /**
     * Service URL for BO Engine event callback.
     */
    public static final String URL_BOENGINE_CALLBACK = "/gateway/callback";

    /**
     * Service URL for RS submit task.
     */
    public static final String URL_RS_SUBMITTASK = "/internal/submitTask";

    /**
     * Service URL for RS finish life cycle of BO.
     */
    public static final String URL_RS_FINISH = "/internal/finRtid";

    /**
     * Service URL gateway for RS workitem actions.
     */
    public static final String GATEWAY_RS_WORKITEM = "/workitem/";

    /**
     * Service URL gateway for RS workqueue actions.
     */
    public static final String GATEWAY_RS_QUEUE = "/queue/";
}
