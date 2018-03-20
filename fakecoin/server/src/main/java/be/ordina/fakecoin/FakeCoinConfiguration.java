package be.ordina.fakecoin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Security;

@Configuration
public class FakeCoinConfiguration {

    @Bean
    public Blockchain blockchain(){
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncey castle as a Security Provider

        final Blockchain blockchain = new Blockchain(4);

        //Create wallets:
        final Wallet fakeCoin = blockchain.createWallet("FakeCoin");
        final Wallet rick = blockchain.createWallet("rick");
        final Wallet morty = blockchain.createWallet("morty");

        // Create genesis transaction
        final Transaction genesisTransaction = new Transaction(fakeCoin.getPublicKey(), rick.getPublicKey(), 1_000_000f, null);
        genesisTransaction.generateSignature(fakeCoin.getPrivateKey());
        genesisTransaction.setTransactionId("0");
        genesisTransaction.getOutputs().add(
                new TransactionOutput(
                        genesisTransaction.getRecipient(),
                        genesisTransaction.getValue(),
                        genesisTransaction.getTransactionId()));

        // Store genesis transaction in unspent output list
        blockchain.getUnspentTransactionOutputs().put(
                genesisTransaction.getOutputs().get(0).getId(),
                genesisTransaction.getOutputs().get(0));

        final Block genesis = new Block("0");
        genesis.addTransaction(blockchain, genesisTransaction);
        blockchain.setGenesisBlock(genesis);

        return blockchain;
    }

}
