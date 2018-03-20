package be.ordina.fakecoin;

import org.junit.Test;

import java.security.Security;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class BlockchainTest {

    @Test
    public void blockchain(){
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncey castle as a Security Provider

        final Blockchain blockchain = new Blockchain(4);

        //Create wallets:
        final Wallet fakeCoin = blockchain.createWallet("FakeCoin");
        final Wallet rick = blockchain.createWallet("Rick");
        final Wallet morty = blockchain.createWallet("Morty");

        // Create genesis transaction
        final Transaction genesisTransaction = new Transaction(fakeCoin.getPublicKey(), rick.getPublicKey(), 100f, null);
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

        System.out.println("Creating and Mining Genesis block with " + genesisTransaction.getValue() + " coins... ");

        final Block genesis = new Block("0");
        genesis.addTransaction(blockchain, genesisTransaction);
        blockchain.setGenesisBlock(genesis);

        printBalances(rick, morty);

        System.out.println("Rick sending 40 coins to Morty");
        final Block block1 = new Block(genesis.getHash());
        block1.addTransaction(blockchain, rick.sendFunds(morty.getPublicKey(), 40f));
        blockchain.addBlock(block1);

        printBalances(rick, morty);

        final Block block2 = new Block(block1.getHash());
        System.out.println("Rick attempting to send more funds than he has");
        block2.addTransaction(blockchain, rick.sendFunds(morty.getPublicKey(), 1000f));
        blockchain.addBlock(block2);

        printBalances(rick, morty);

        System.out.println("Morty sending back 20 coins to Rick");
        final Block block3 = new Block(block2.getHash());
        block3.addTransaction(blockchain, morty.sendFunds(rick.getPublicKey(), 20));

        printBalances(rick, morty);

        assertThat(rick.getBalance()).isEqualTo(80f);
        assertThat(morty.getBalance()).isEqualTo(20f);

        assertThat(blockchain.isValid()).isTrue();
    }

    private void printBalances(Wallet rick, Wallet morty) {
        System.out.println("Rick's balance: " + rick.getBalance());
        System.out.println("Morty's balance: " + morty.getBalance());
    }

}