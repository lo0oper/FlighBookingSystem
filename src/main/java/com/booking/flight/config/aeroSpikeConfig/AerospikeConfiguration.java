package com.booking.flight.config.aeroSpikeConfig;

import com.aerospike.client.*;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.policy.WritePolicy;
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

    @Bean
    public WritePolicy aerospikeWritePolicy() {
        WritePolicy policy = new WritePolicy();
        // Set TTL to 5 minutes (300 seconds). This acts as a lock timeout/cleanup.
        policy.expiration = 300;
        // This is crucial for the Optimistic Locking logic (CREATE_ONLY = CAS)
        policy.recordExistsAction = com.aerospike.client.policy.RecordExistsAction.CREATE_ONLY;
        return policy;
    }

    public String getNamespace() {
        return aerospikeNamespace;
    }
}
