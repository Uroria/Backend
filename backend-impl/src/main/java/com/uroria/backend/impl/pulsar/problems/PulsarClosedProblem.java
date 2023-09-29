package com.uroria.backend.impl.pulsar.problems;

import com.uroria.problemo.AbstractProblem;

import java.util.Optional;

public final class PulsarClosedProblem extends AbstractProblem {

    public PulsarClosedProblem() {
        super(500, "connections.closed");
    }

    @Override
    public Optional<Throwable> getError() {
        return Optional.empty();
    }
}
