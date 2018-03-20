package be.ordina.fakecoin;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class Transaction {

    private String transactionId; // this is also the hash of the transaction.
    private PublicKey sender; // senders address/public key.
    private PublicKey recipient; // Recipients address/public key.
    private float value;
    private byte[] signature; // this is to prevent anybody else from spending funds in our wallet.

    private List<TransactionInput> inputs;
    private List<TransactionOutput> outputs = new ArrayList<>();

    private static int sequence = 0; // a rough count of how many transactions have been generated.

    public Transaction(PublicKey from, PublicKey to, float value,  List<TransactionInput> inputs) {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;
    }

    public Transaction(PublicKey from, PublicKey to, float value,  List<TransactionInput> inputs, byte[] signature) {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;
        this.signature = signature;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public PublicKey getSender() {
        return sender;
    }

    public PublicKey getRecipient() {
        return recipient;
    }

    public float getValue() {
        return value;
    }

    public byte[] getSignature() {
        return signature;
    }

    public static int getSequence() {
        return sequence;
    }

    /**
     * This Calculates the transaction hash (which will be used as its Id)
     */
    private String calulateHash() {
        sequence++; //increase the sequence to avoid 2 identical transactions having the same hash
        return BlockUtil.applySha256(
                BlockUtil.getStringFromKey(sender) +
                        BlockUtil.getStringFromKey(recipient) +
                        Float.toString(value) + sequence
        );
    }

    /**
     * Signs all the data we dont wish to be tampered with.
     */
    public void generateSignature(PrivateKey privateKey) {
        String data = BlockUtil.getStringFromKey(sender) + BlockUtil.getStringFromKey(recipient) + Float.toString(value);
        signature = BlockUtil.applyECDSASig(privateKey,data);
    }

    /**
     * Verifies the data we signed hasnt been tampered with
     */
    public boolean verifySignature() {
        String data = BlockUtil.getStringFromKey(sender) + BlockUtil.getStringFromKey(recipient) + Float.toString(value);
        return BlockUtil.verifyECDSASig(sender, data, signature);
    }

    //returns sum of inputs(UTXOs) values
    public float getInputsValue() {
        float total = 0;
        for(TransactionInput i : inputs) {
            if(i.getUnspentTransactionOutput() == null) continue; //if Transaction can't be found skip it
            total += i.getUnspentTransactionOutput().getValue();
        }
        return total;
    }

    //returns sum of outputs:
    public float getOutputsValue() {
        float total = 0;
        for(TransactionOutput o : outputs) {
            total += o.getValue();
        }
        return total;
    }

    public List<TransactionInput> getInputs() {
        return inputs;
    }

    public List<TransactionOutput> getOutputs() {
        return outputs;
    }

    public void generateOutputs(){
        float leftOver = getInputsValue() - value; //get value of inputs then the left over change:
        transactionId = calulateHash();
        outputs.add(new TransactionOutput( this.recipient, value,transactionId)); //send value to recipient
        outputs.add(new TransactionOutput( this.sender, leftOver,transactionId)); //send the left over 'change' back to sender
    }

}
