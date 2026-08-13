package com.urgencesante.identity.internal.adapter.out.transaction;

import com.urgencesante.identity.internal.application.port.out.TransactionPort;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component("identitySpringTransactionAdapter")
class SpringTransactionAdapter implements TransactionPort {
    private final TransactionTemplate transactionTemplate;

    SpringTransactionAdapter(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public <T> T inTransaction(Supplier<T> work) {
        return transactionTemplate.execute(status -> work.get());
    }
}
