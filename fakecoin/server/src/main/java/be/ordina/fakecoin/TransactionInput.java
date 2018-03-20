package be.ordina.fakecoin;

public class TransactionInput {

    private String transactionOutputId; //Reference to TransactionOutputs -> transactionId
    private TransactionOutput unspentTransactionOutput; //Contains the Unspent transaction output

    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }

    public String getTransactionOutputId() {
        return transactionOutputId;
    }

    public TransactionOutput getUnspentTransactionOutput() {
        return unspentTransactionOutput;
    }

    public void setUnspentTransactionOutput(TransactionOutput unspentTransactionOutput) {
        this.unspentTransactionOutput = unspentTransactionOutput;
    }
}
