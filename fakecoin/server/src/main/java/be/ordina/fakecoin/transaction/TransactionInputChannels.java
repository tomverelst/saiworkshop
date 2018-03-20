package be.ordina.fakecoin.transaction;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface TransactionInputChannels {

    @Input
    SubscribableChannel transactions();

}
