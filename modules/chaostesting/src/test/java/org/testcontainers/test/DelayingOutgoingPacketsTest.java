package org.testcontainers.test;

import org.junit.Before;
import org.junit.Test;
import org.testcontainers.executables.PumbaExecutables;
import org.testcontainers.client.PumbaClient;
import org.testcontainers.client.PumbaClients;
import org.testcontainers.client.commandparts.SupportedTimeUnit;
import org.testcontainers.test.Pinger.PingResponse;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.client.actions.networkactions.NetworkActions.networkAction;
import static org.testcontainers.client.actions.networkactions.NetworkSubCommands.delayOutgoingPackets;
import static org.testcontainers.client.executionmodes.PumbaExecutionModes.onlyOnce;
import static org.testcontainers.client.targets.PumbaTargets.containers;

/**
 * Created by novy on 14.01.17.
 */
public class DelayingOutgoingPacketsTest implements CanSpawnContainers {

    private PumbaClient pumba;
    private Pinger pinger;

    @Before
    public void setUp() throws Exception {
        pumba = PumbaClients.forExecutable(PumbaExecutables.dockerized());
        pinger = startedPinger();
    }

    @Test
    public void should_be_able_to_delay_outgoing_packets_from_container() throws Exception {
        // given
        final Container aContainer = startedContainer();

        // when
        pumba
                .performNetworkChaos(networkAction()
                        .lastingFor(30, SupportedTimeUnit.SECONDS)
                        .executeSubCommand(
                                delayOutgoingPackets()
                                        .delayFor(500, SupportedTimeUnit.MILLISECONDS)
                        )
                )
                .affect(containers(aContainer.getContainerName()))
                .execute(onlyOnce().onAllChosenContainers());

        // then
        await().atMost(30, SECONDS).until(() -> {
            final PingResponse ping = pinger.ping(aContainer);
            assertThat(ping.latencyInMilliseconds()).isGreaterThanOrEqualTo(450);
        });
    }
}
