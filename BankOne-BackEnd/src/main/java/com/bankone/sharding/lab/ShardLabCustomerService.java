package com.bankone.sharding.lab;

import com.bankone.sharding.ShardContext;
import com.bankone.sharding.ShardId;
import com.bankone.sharding.ShardRouter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
@ConditionalOnProperty(prefix = "app.sharding", name = "enabled", havingValue = "true")
public class ShardLabCustomerService {

    private final ShardRouter shardRouter;
    private final ShardLabCustomerRepository repository;
    private final TransactionTemplate shardTx;

    public ShardLabCustomerService(
            ShardRouter shardRouter,
            ShardLabCustomerRepository repository,
            @Qualifier("shardTransactionTemplate") TransactionTemplate shardTx
    ) {
        this.shardRouter = shardRouter;
        this.repository = repository;
        this.shardTx = shardTx;
    }

    public ShardLabCustomerResponse create(CreateShardLabCustomerRequest request) {
        UUID id = UUID.randomUUID();
        ShardId shard = shardRouter.forKey(id);
        ShardContext.set(shard);
        try {
            ShardLabCustomer saved = shardTx.execute(status ->
                    repository.save(new ShardLabCustomer(
                            id,
                            request.fullName().trim(),
                            request.email().trim().toLowerCase(),
                            Instant.now()
                    ))
            );
            return ShardLabCustomerResponse.from(saved, shard.code());
        } finally {
            ShardContext.clear();
        }
    }

    public ShardLabCustomerResponse getById(UUID id) {
        ShardId shard = shardRouter.forKey(id);
        ShardContext.set(shard);
        try {
            ShardLabCustomer found = shardTx.execute(status ->
                    repository.findById(id).orElse(null)
            );
            if (found == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lab customer not found on " + shard.code());
            }
            return ShardLabCustomerResponse.from(found, shard.code());
        } finally {
            ShardContext.clear();
        }
    }

    public ShardWhichResponse whichShard(UUID id) {
        return new ShardWhichResponse(id.toString(), shardRouter.forKey(id).code());
    }
}
