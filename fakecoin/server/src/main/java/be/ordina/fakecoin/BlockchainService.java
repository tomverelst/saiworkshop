package be.ordina.fakecoin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BlockchainService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockchainService.class);

    private Blockchain blockchain;

    public BlockchainService(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    public List<Wallet> getWallets(){
        return blockchain.getWallets();
    }

    public Optional<Wallet> getWallet(String id){
        return blockchain.getWallet(id);
    }

    public void createTransaction(
            final String senderId,
            final String recipientId,
            final float funds,
            final byte[] signature){
        LOGGER.info("Creating transaction");

        final Wallet sender = blockchain.getWallet(senderId)
                .orElseThrow(WalletNotFoundException::new);

        final Wallet recipient = blockchain.getWallet(recipientId)
                .orElseThrow(WalletNotFoundException::new);
        try {
            final Block lastBlock = blockchain.last();
            final Block newBlock = new Block(lastBlock.getHash());
            final Transaction transaction = sender.sendFunds(recipient.getPublicKey(), funds, signature);
            transaction.generateSignature(sender.getPrivateKey());
            newBlock.addTransaction(blockchain, transaction);
            blockchain.addBlock(newBlock);

        } catch(SignatureVerificationException ex){
            LOGGER.error("Could not verify signature", ex);
        }
    }
}
