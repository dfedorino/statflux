package com.rmrf.statflux.bot.core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChainTest {

    @Test
    void shouldCallAllNodesInOrder() {
        List<String> calls = new ArrayList<>();

        Chain.Node<String> n1 = (ctx, next) -> {
            calls.add("n1");
            next.accept(ctx);
        };

        Chain.Node<String> n2 = (ctx, next) -> {
            calls.add("n2");
            next.accept(ctx);
        };

        Chain.Node<String> n3 = (ctx, next) -> {
            calls.add("n3");
            next.accept(ctx);
        };

        Chain<String> chain = Chain.of(n1, n2, n3);

        chain.accept("test");

        assertEquals(List.of("n1", "n2", "n3"), calls);
    }

    @Test
    void shouldStopChainIfNextNotCalled() {
        List<String> calls = new ArrayList<>();

        Chain.Node<String> n1 = (ctx, next) -> {
            calls.add("n1");
            // next не вызываем
        };

        Chain.Node<String> n2 = (ctx, next) -> {
            calls.add("n2");
            next.accept(ctx);
        };

        Chain<String> chain = Chain.of(n1, n2);

        chain.accept("test");

        assertEquals(List.of("n1"), calls);
    }

    @Test
    void shouldPassModifiedContext() {
        Chain.Node<StringBuilder> n1 = (ctx, next) -> {
            ctx.append("A");
            next.accept(ctx);
        };

        Chain.Node<StringBuilder> n2 = (ctx, next) -> {
            ctx.append("B");
            next.accept(ctx);
        };

        StringBuilder ctx = new StringBuilder();

        Chain<StringBuilder> chain = Chain.of(n1, n2);
        chain.accept(ctx);

        assertEquals("AB", ctx.toString());
    }

    @Test
    void shouldHandleEmptyChain() {
        Chain<String> chain = Chain.of(List.of());

        // просто не должно упасть
        chain.accept("test");
    }

    @Test
    void shouldCallSingleNode() {
        List<String> calls = new ArrayList<>();

        Chain.Node<String> n1 = (ctx, next) -> {
            calls.add("n1");
            next.accept(ctx);
        };

        Chain<String> chain = Chain.of(n1);
        chain.accept("test");

        assertEquals(List.of("n1"), calls);
    }

    @Test
    void shouldMaintainExecutionOrder() {
        List<Integer> order = new ArrayList<>();

        Chain.Node<Integer> n1 = (ctx, next) -> {
            order.add(1);
            next.accept(ctx);
        };

        Chain.Node<Integer> n2 = (ctx, next) -> {
            order.add(2);
            next.accept(ctx);
        };

        Chain.Node<Integer> n3 = (ctx, next) -> {
            order.add(3);
            next.accept(ctx);
        };

        Chain.of(n1, n2, n3).accept(0);

        assertEquals(List.of(1, 2, 3), order);
    }
}
