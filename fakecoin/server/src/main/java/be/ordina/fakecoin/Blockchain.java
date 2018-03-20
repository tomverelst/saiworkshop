package be.ordina.fakecoin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Blockchain {

    private static final Logger LOGGER = LoggerFactory.getLogger(Blockchain.class);

    private Block genesis;
    private final int difficulty;
    private final List<Block> blocks = new ArrayList<>();
    private final Map<String,TransactionOutput> unspentTransactionOutputs = new HashMap<>();

    private final Map<String,Wallet> walletsByName = new HashMap<>();
    private final Map<String,Wallet> walletsByAddress = new HashMap<>();

    public Blockchain(final int difficulty){
        this.difficulty = difficulty;
    }

    public void setGenesisBlock(Block genesis){
        if(this.blocks.isEmpty()){
            this.genesis = genesis;
            this.genesis.mineBlock(difficulty);
            this.blocks.add(genesis);
        }
    }

    public synchronized void addBlock(Block block){
        block.mineBlock(difficulty);
        this.blocks.add(block);
    }

    public synchronized Block last(){
        return this.blocks.get(this.blocks.size()-1);
    }

    /**
     * Creates a new wallet with the given name.
     * If the name equals a public key or the name of an existing wallet,
     * the existing wallet is returned.
     */
    public synchronized Wallet createWallet(String name){
        if(walletsByAddress.containsKey(name)){
            return walletsByAddress.get(name);
        }
        if(walletsByName.containsKey(name)){
            return walletsByName.get(name);
        } else {
            Wallet wallet = new Wallet(name, this);

            // Avoid collisions
            while(walletsByAddress.containsKey(wallet.getEncodedPublicKey())){
                wallet = new Wallet(name, this);
            }

            this.walletsByName.put(wallet.getName(), wallet);
            this.walletsByAddress.put(wallet.getAddress(), wallet);
            return wallet;
        }
    }

    public synchronized Optional<Wallet> getWallet(String id){
        Wallet wallet = walletsByName.get(id);
        if(wallet == null){
            wallet = walletsByAddress.get(id);
        }
        return Optional.ofNullable(wallet);
    }

    public List<Wallet> getWallets(){
        return new ArrayList<>(walletsByName.values());
    }

    public Boolean isValid() {
        Block currentBlock;
        Block previousBlock;

        //loop through blockchain to check hashes:
        for(int i=1; i < blocks.size(); i++) {
            currentBlock = blocks.get(i);
            previousBlock = blocks.get(i-1);

            final String hashTarget = new String(new char[difficulty]).replace('\0', '0');
            final Map<String,TransactionOutput> temp = new HashMap<>(); //a temporary working list of unspent transactions at a given block state.
            for (Transaction genesisTransaction : genesis.getTransactions()) {
                for(TransactionOutput output : genesisTransaction.getOutputs()){
                    temp.put(output.getId(), output);
                }
            }

            //compare registered hash and calculated hash:
            if(!currentBlock.getHash().equals(currentBlock.calculateHash()) ){
                return false;
            }
            //compare previous hash and registered previous hash
            if(!previousBlock.getHash().equals(currentBlock.getPreviousHash()) ) {
                return false;
            }
            //check if hash is solved (block is mined)
            if(!currentBlock.getHash().substring( 0, difficulty).equals(hashTarget)) {
                return false;
            }

            //loop through blockchains transactions
            TransactionOutput tempOutput;
            for(int t=0; t <currentBlock.getTransactions().size(); t++) {
                Transaction currentTransaction = currentBlock.getTransactions().get(t);

                if(!currentTransaction.verifySignature()) {
                    return false;
                }
                if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                    return false;
                }

                for(TransactionInput input: currentTransaction.getInputs()) {
                    tempOutput = temp.get(input.getTransactionOutputId());

                    if(tempOutput == null) {
                        return false;
                    }

                    if(input.getUnspentTransactionOutput().getValue() != tempOutput.getValue()) {
                        return false;
                    }

                    temp.remove(input.getTransactionOutputId());
                }

                for(TransactionOutput output: currentTransaction.getOutputs()) {
                    temp.put(output.getId(), output);
                }

                if( currentTransaction.getOutputs().get(0).getRecipient() != currentTransaction.getRecipient()) {
                    return false;
                }
                if( currentTransaction.getOutputs().get(1).getRecipient() != currentTransaction.getSender()) {
                    return false;
                }

            }

        }
        return true;
    }

    public synchronized boolean processTransaction(Transaction transaction) {

        LOGGER.info("Attempting to process transaction of {} coins", transaction.getValue());

        if(!transaction.verifySignature()) {
            LOGGER.info("Transaction Signature failed to verify");
            return false;
        }

        //gather transaction inputs (Make sure they are unspent)
        for(TransactionInput input : transaction.getInputs()) {
            input.setUnspentTransactionOutput(unspentTransactionOutputs.get(input.getTransactionOutputId()));
        }

        //check if transaction is valid
        if(transaction.getInputsValue() < 1) {
            LOGGER.info("Transaction inputs too small: " +  transaction.getInputsValue());
            return false;
        }

        //generate transaction outputs:
        transaction.generateOutputs();

        //add outputs to Unspent list
        transaction.getOutputs().forEach(o -> unspentTransactionOutputs.put(o.getId(), o));

        // remove transaction inputs from unspent outputs lists as spent:
        transaction.getInputs().stream()
                .filter(i -> i.getUnspentTransactionOutput() != null)
                .forEach(i -> unspentTransactionOutputs.remove(i.getUnspentTransactionOutput().getId()));

        LOGGER.info("Transaction processed");

        return true;
    }

    public Map<String, TransactionOutput> getUnspentTransactionOutputs() {
        return unspentTransactionOutputs;
    }
}
