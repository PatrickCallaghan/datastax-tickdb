\[[Return to README][1]\]

# Create Schema – Terminal Sample

    dPro:~ danielcohen$ ccm status
    Cluster: 'TickData'
    -------------------
    node1: UP
    dPro:~ danielcohen$ ccm node1 cqlsh
    Connected to TickData at 127.0.0.1:9042.
    [cqlsh 5.0.1 | Cassandra 2.1.8.689 | DSE 4.7.3 | CQL spec 3.2.0 | Native protocol v3]
    Use HELP for help.
    cqlsh> SOURCE '~/datastax-tickdb/src/main/resources/cql/create_schema.cql'
    cqlsh> DESCRIBE KEYSPACES;
    
    system_traces  system  dse_system  datastax_tickdb
    
    cqlsh> DESCRIBE KEYSPACE datastax_tickdb;
    
    CREATE KEYSPACE datastax_tickdb WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;
    
    CREATE TABLE datastax_tickdb.tick_data (
        symbol text,
        date timestamp,
        value double,
        PRIMARY KEY (symbol, date)
    ) WITH CLUSTERING ORDER BY (date DESC)
        AND bloom_filter_fp_chance = 0.01
        AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
        AND comment = ''
        AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
        AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
        AND dclocal_read_repair_chance = 0.1
        AND default_time_to_live = 0
        AND gc_grace_seconds = 864000
        AND max_index_interval = 2048
        AND memtable_flush_period_in_ms = 0
        AND min_index_interval = 128
        AND read_repair_chance = 0.0
        AND speculative_retry = '99.0PERCENTILE';
    
    cqlsh> 

[1]: ../README.md