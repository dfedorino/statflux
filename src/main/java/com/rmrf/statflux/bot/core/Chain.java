package com.rmrf.statflux.bot.core;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class Chain<T> implements Consumer<T> {
    private final List<Node<T>> nodes;

    public static <T> Chain<T> of(Node<T>... nodes) {
        return new Chain<>(Arrays.asList(nodes));
    }

    public static <T> Chain<T> of(List<Node<T>> nodes) {
        return new Chain<>(nodes);
    }

    private Chain(List<Node<T>> nodes) {
        this.nodes = nodes;
    }

    @Override
    public void accept(T ctx) {
        run(ctx, 0);
    }

    private void run(T ctx, int index) {
        if (index >= nodes.size()) {
            return;
        }

        nodes.get(index).handle(ctx, next -> {
            run(next, index + 1);
        });
    }

    public interface Node<T> {
        void handle(T ctx, Consumer<T> next);
    }
}
