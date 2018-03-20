package be.ordina.fakecoin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Block {

    private static final Logger LOGGER = LoggerFactory.getLogger(Block.class);

    private String hash;
    private String previousHash;
    private long timeStamp; //as number of milliseconds since 1/1/1970.
    private int nonce;

    private String merkleRoot;
    private List<Transaction> transactions = new ArrayList<>(); //our data will be a simple message.

    //Block Constructor.
    public Block(final String previousHash ) {
        this.previousHash = previousHash;
        this.timeStamp = System.currentTimeMillis();
        this.hash = calculateHash();
    }

    public String getHash() {
        return hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public String calculateHash() {
        return BlockUtil.applySha256(
                previousHash + Long.toString(timeStamp) + Integer.toString(nonce) + merkleRoot);
    }

    public void mineBlock(int difficulty) {
        LOGGER.info("Mining block...");
        merkleRoot = BlockUtil.getMerkleRoot(transactions);
        String target = new String(new char[difficulty]).replace('\0', '0'); //Create a string with difficulty * "0"
        while(!hash.substring( 0, difficulty).equals(target)) {
            nonce ++;
            hash = calculateHash();
        }
        LOGGER.info("Block mined: {}", hash);

    }

    //Add transactions to this block
    public boolean addTransaction(Blockchain blockchain, Transaction transaction) {
        if (transaction == null) {
            return false;
        }
        if ((!previousHash.equals("0"))) {
            final boolean processed = blockchain.processTransaction(transaction);
            if (!processed) {
                return false;
            }
        }
        transactions.add(transaction);
        LOGGER.info("Transaction Successfully added to Block");
        return true;
    }
}
