package com.optum.sourcehawk.core.utils;

import lombok.experimental.UtilityClass;

import java.util.function.Function;

/**
 * Utility class for try/catch handling
 *
 * @author Brian Wyka
 */
@UtilityClass
public class Try {

    /**
     * Surround the {@link CheckedSupplier} with a try/catch
     *
     * @param supplier the supplier of the return value
     * @param defaultFunction the default value function if an exception is thrown
     * @param <T> the type of the return value
     * @return the result of the supplier if successful, otherwise the result of the default function
     */
    public <T> T attemptOrDefault(final CheckedSupplier<T> supplier, final Function<Throwable, T> defaultFunction) {
        try {
            return supplier.get();
        } catch (final Throwable e) {
            return defaultFunction.apply(e);
        }
    }

    /**
     * A checked supplier functional interface
     *
     * @param <T> the type of the supplier's return value
     * @author Brian Wyka
     */
    @FunctionalInterface
    public interface CheckedSupplier<T> {

        /**
         * Get the result
         *
         * @return the result
         * @throws Throwable if any exception / error occurs
         */
        @SuppressWarnings("squid:S112")
        T get() throws Throwable;

    }

}
