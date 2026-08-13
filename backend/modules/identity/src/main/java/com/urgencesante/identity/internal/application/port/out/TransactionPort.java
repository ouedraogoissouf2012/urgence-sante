package com.urgencesante.identity.internal.application.port.out;

import java.util.function.Supplier;

public interface TransactionPort {
    <T> T inTransaction(Supplier<T> work);
}
