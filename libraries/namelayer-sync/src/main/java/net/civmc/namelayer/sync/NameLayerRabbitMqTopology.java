package net.civmc.namelayer.sync;

public final class NameLayerRabbitMqTopology {

    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String INVALIDATION_EXCHANGE = "namelayer.invalidations";
    public static final String INVALIDATION_EXCHANGE_TYPE = "fanout";
    public static final boolean INVALIDATION_EXCHANGE_DURABLE = true;
    public static final String PAPER_INVALIDATION_QUEUE_PREFIX = "namelayer.invalidations.";
    public static final String WRITE_REQUEST_QUEUE = "namelayer.write.requests";
    public static final boolean WRITE_REQUEST_QUEUE_DURABLE = true;
    public static final String WRITE_RESPONSE_QUEUE_PREFIX = "namelayer.write.responses.";

    private NameLayerRabbitMqTopology() {
    }
}
