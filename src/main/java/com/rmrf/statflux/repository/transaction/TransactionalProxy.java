package com.rmrf.statflux.repository.transaction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class TransactionalProxy implements InvocationHandler {

    private final Object target;
    private final TransactionManager txManager;

    public TransactionalProxy(Object target, TransactionManager txManager) {
        this.target = target;
        this.txManager = txManager;
    }

    @SuppressWarnings("unchecked")
    public static <T> T create(T target, TransactionManager txManager) {
        return (T) Proxy.newProxyInstance(
            target.getClass().getClassLoader(),
            target.getClass().getInterfaces(),
            new TransactionalProxy(target, txManager)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Method targetMethod = target.getClass()
            .getMethod(method.getName(), method.getParameterTypes());

        if (targetMethod.isAnnotationPresent(Transactional.class)) {
            return txManager.execute(() -> {
                try {
                    return method.invoke(target, args);
                } catch (InvocationTargetException e) {
                    Throwable cause = e.getTargetException();

                    if (cause instanceof RuntimeException) {
                        throw (RuntimeException) cause;
                    }
                    if (cause instanceof Error) {
                        throw (Error) cause;
                    }

                    throw new RuntimeException(cause);
                }
            });
        }

        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}
