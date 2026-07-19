package com.urgencesante.availability.internal.adapter.out.transaction;

import com.urgencesante.availability.internal.application.port.out.TransactionPort;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Adaptateur de la frontière transactionnelle applicative : délègue au
 * gestionnaire de transactions Spring (commit au retour, rollback sur
 * exception), sans exposer Spring à la couche application.
 */
@Component
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
