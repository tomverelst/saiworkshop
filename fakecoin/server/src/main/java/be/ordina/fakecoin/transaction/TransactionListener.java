package be.ordina.fakecoin.transaction;

import be.ordina.fakecoin.BlockchainService;
import be.ordina.fakecoin.WalletNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Base64;

@Component
public class TransactionListener {

    private static Logger LOGGER = LoggerFactory.getLogger(TransactionListener.class);

    private final BlockchainService blockchainService;
    private final ObjectMapper objectMapper;

    public TransactionListener(
            final TransactionInputChannels inputChannels,
            final ObjectMapper objectMapper,
            final BlockchainService blockchainService) {
        this.objectMapper = objectMapper;
        this.blockchainService = blockchainService;
        inputChannels.transactions().subscribe(message -> {
            try {
                if (message.getPayload() instanceof byte[]) {
                    byte[] content = (byte[]) message.getPayload();
                    final TransactionEvent event = objectMapper.readValue(content, TransactionEvent.class);
                    handle(event);

                } else if (message.getPayload() instanceof TransactionEvent) {
                    final TransactionEvent event = (TransactionEvent) message.getPayload();
                    handle(event);

                }
            } catch(IOException ex){
                LOGGER.error("Could not read transaction event", ex);
            }
        });
    }

    void handle(TransactionEvent event){
        try {
            blockchainService.createTransaction(
                    event.getSender(),
                    event.getRecipient(),
                    event.getFunds(),
                    Base64.getDecoder().decode(event.getSignature()));
        } catch(WalletNotFoundException ex){
            LOGGER.info("Could not find wallet");
        }
    }

}
