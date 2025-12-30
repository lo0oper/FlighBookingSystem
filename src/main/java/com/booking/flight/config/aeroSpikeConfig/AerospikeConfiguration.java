package com.booking.flight.config.aeroSpikeConfig;

import com.aerospike.client.*;
import com.aerospike.client.policy.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AerospikeConfiguration {

    @Value("${aerospike.host}")
    private String aerospikeHost;

    @Value("${aerospike.port}")
    private int aerospikePort;

    @Value("${aerospike.namespace}")
    private String aerospikeNamespace;

    @Bean
    public AerospikeClient aerospikeClient() throws AerospikeException {
        // Production Ready: Using standard host/port. Add security/auth if needed.
        Host[] hosts = new Host[] { new Host(aerospikeHost, aerospikePort) };
        return new AerospikeClient(new ClientPolicy(), hosts);
    }

    @Bean(name = "aerospikeLockingPolicy")
    public WritePolicy aerospikeLockingPolicy() {
        WritePolicy policy = new WritePolicy();
        policy.recordExistsAction = RecordExistsAction.CREATE_ONLY;
        // Fail immediately if lock exists
        policy.totalTimeout = 100;      // ms

        policy.commitLevel = CommitLevel.COMMIT_ALL;


        return policy;
    }
    public String getNamespace() {
        return aerospikeNamespace;
    }
}
