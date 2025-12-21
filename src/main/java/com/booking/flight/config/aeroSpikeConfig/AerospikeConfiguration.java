package com.booking.flight.config.aeroSpikeConfig;

import com.aerospike.client.*;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.policy.GenerationPolicy;
import com.aerospike.client.policy.RecordExistsAction;
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

    @Bean(name = "aerospikeLockingPolicy")
    public WritePolicy aerospikeLockingPolicy() {
        WritePolicy policy = new WritePolicy();

        // 1. SET THE EXPIRATION (LOCK TTL)
        policy.expiration = 300;

        // 2. SET THE CRITICAL ATOMIC GENERATION CHECK
        policy.generationPolicy = com.aerospike.client.policy.GenerationPolicy.EXPECT_GEN_EQUAL;
        policy.generation = 0; // Check that the record does not exist

        // 3. Set the required action for CAS
        policy.recordExistsAction = com.aerospike.client.policy.RecordExistsAction.UPDATE;

        // 4. Set recommended properties
        policy.sendKey = true;

        // CommitLevel is fine at default (COMMIT_ALL)

        return policy;
    }
    public String getNamespace() {
        return aerospikeNamespace;
    }
}
