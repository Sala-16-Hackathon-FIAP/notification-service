package br.com.fiapx.notification.infrastructure.email;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNoException;

class SimulatedEmailAdapterTest {

    @Test
    void send_shouldLogWithoutThrowing() {
        SimulatedEmailAdapter adapter = new SimulatedEmailAdapter();

        assertThatNoException().isThrownBy(() ->
                adapter.send("user@test.com", "Test Subject", "Test body content"));
    }
}
