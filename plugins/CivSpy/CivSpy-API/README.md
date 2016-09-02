### CivSpy-API
--------------

Wrapper for HikariCP Connection Pool, common Database structure, and some nifty little insertion and batch creation functions.

See the javadoc for more details on those.

#### Connection Pools

What is a connection pool? Roughly, it's a set of pre-negotiated or grown-on-demand set of active, open, connections to a database. Requests to the Connection Pool for a connection (via `getConnection`) hand you one of these ready-to-go connections. No more waiting for connect, no checking for liveness; that's the job of the pool (usually). When you are done, just `close()` the connection and it gets returned to the "Pool" for use by the next asker.

There are four massively important configuration options when it comes to a connection pool.

1. `poolsize` - How many connections do you want to keep alive, at most, at any one time? Be careful that all your active pools together across all plugins won't exceed the `connection_limit` of your database, or some pools will begin to "starve" -- fail to pre-gen connections. 
2. `idleTimeout` - How long can a connection sit idle? Your database has a configuration option `wait_timeout` or similar that determines the upper limit of this configuration option on the pool. Otherwise, your pool could start handing out dead connections.
3. `maxLifetime` - Assuming a connection is kept relatively occupied its possible that its lifespan could exceed that of `idleTimeout`. How long is long enough in terms of total connection age? It's usually a good idea to periodically release and renew a connection; but this can be significantly longer then `idleTimeout`.
4. `connectionTimeout` - This one is tricky. _During_ a particular request / update / query on an active connection, how long should the connection wait to hear back from the database? Set this to be about twice as long as the longest query you can imagine happening, and be prepared to lengthen it if you start seeing a lot of connections timing out. This is arguably the hardest parameter to configure.

#### CivSpy Data Structure Theory

CivSpy is designed to store aggregate or regularly sampled data, not a ton of high-frequency point data. This philosophy governed its database design.

At present, CivSpy can store data with a maximum native granularity of:

`timestamp` / `data label` / `server` / `world` / `chunk X` / `chunk Z` / `player UUID`

In the backend, this is well-indexed so you can easily construct queries to "slice" this data at any new desired granularity. 

On top of this composite index are stored two "value" fields:

`Value String` / `Value Number`

The assumption is that `Value Number` is always a _measurement_ or _aggregation_. `Value String` describes that measurement in some way.

However, no restrictions are placed, so `Value String` could also be a "point data" label. Up to you.

Note that this key and value composite pairing lets you do cool stuff with slicing and re-aggregating data.

For instance if you run a cluster of servers and want to see all the data for a particular `data label` about all players over a particular timespan, you'd be able to construct a query like this:

    SELECT uuid, sum(numeric_value) AS spysum
      FROM spy_stats
      WHERE stat_key = 'data label' AND stat_time >= TIMESTAMPADD(WEEK, -1, now())
      GROUP BY uuid
      ORDER BY spysum DESC;

Using the data as laid out, it would be trivial to create a dashboard to show those kind of statistics.

#### CivSpy Data Structure Practice

Data in CivSpy is recorded based on the logical premise described above. It is realized as follows:

* `id` - Artificial key, unique globally. Not terribly useful.
* `stat_time` - SQL TIMESTAMP, defaults to NOW(), used to record either the time of sampling or the beginning of an aggregation time period. (`timestamp`)
* `stat_key` - 64 characters (max) used to delimit what kind of sample/aggregation this is. (`data label`)
* `server` - 64 characters (max) used to name the server that sourced this record. Can be empty. (`server`)
* `world` - 100 characters (max) used to name the world on the server that sourced this record. Can be empty (`world`)
* `chunk_x` - SQL INT (Integer) used to specify the chunk's x offset in the world on the server that sourced this record. Can be empty. (`chunk X`)
* `chunk_z` - SQL INT (Integer) used to specify the chunk's z offset in the world on the server that sourced this record. Can be empty. (`chunk Z`)
* `uuid` - 36 character UUID specifying the player this record describes. Can be empty (`player UUID`)
* `numeric_value` - SQL NUMERIC (Double) used to store the aggregate value or sampled value. Can be empty (`Value Number`)
* `string_value` - SQL TEXT (String) used to store the sampled value clarifier / subgrouping value. Can be empty (`Value String`)

There are three indexes:

* `pk_spy_stats` - Primary Key index against artificial `id` -- usually useless.
* `idx_spy_stats_time_key` - Binary Tree (ordered) around `stat_time` and `stat_key`
* `idx_spy_ext_key` - Binary Tree (ordered) around `server`, `world`, `chunk_x`, `chunk_z`, and `uuid`.

The last two indexes are hugely useful. Note that `string_value` is _not_ indexed so group or search against it with extreme care. Do not search against it without first limiting against something that can use either `idx_spy_stats_time_key` or `idx_spy_ext_key`.

#### CivSpy Inserting Data

The Database class is the only member of the shared API; it wraps the HikariCP pool, defines database creation and any migrations, and exposes convenient methods to use to insert the data that Bukkit and Bungee will capture.

Check the [JavaDoc](apidocs/index.html) for more details on those methods.

Having trouble loading the javadocs? Clone this repository locally and you'll have them available -- just navigate to the index.html file inside `apidocs` folder.
