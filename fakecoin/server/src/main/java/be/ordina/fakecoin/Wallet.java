package be.ordina.fakecoin;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Wallet {

    private final Blockchain blockchain;
    private final String name;
    private String address;

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private HashMap<String,TransactionOutput> unspentTransactionOutputs = new HashMap<>(); //only unspent transactions owned by this wallet.

    public Wallet(String name, Blockchain blockchain){
        this.name = name;
        this.blockchain = blockchain;
        generateKeyPair();
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public String getEncodedPublicKey(){
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public String getEncodedPrivateKey(){
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void generateKeyPair() {
        try {
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
            final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            final ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
            // Initialize the key generator and generate a KeyPair
            keyGen.initialize(ecSpec, random);   //256 bytes provides an acceptable security level
            final KeyPair keyPair = keyGen.generateKeyPair();
            // Set the public and private keys from the keyPair
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
            address = BlockUtil.applySha256(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public float getBalance() {
        float total = 0;
        for (Map.Entry<String, TransactionOutput> item: blockchain.getUnspentTransactionOutputs().entrySet()){
            TransactionOutput output = item.getValue();
            if(output.isMine(publicKey)) { //if output belongs to me ( if coins belong to me )
                unspentTransactionOutputs.put(output.getId(),output); //add it to our list of unspent transactions.
                total += output.getValue();
            }
        }
        return total;
    }
    //Generates and returns a new transaction from this wallet.
    public Transaction sendFunds(PublicKey recipient, float value ) {
        if(getBalance() < value) { //gather balance and check funds.
            return null;
        }

        final List<TransactionInput> inputs = getTransactionInputs(value);

        final Transaction newTransaction = new Transaction(publicKey, recipient , value, inputs);
        newTransaction.generateSignature(privateKey);
        inputs.forEach(i -> unspentTransactionOutputs.remove(i.getTransactionOutputId()));

        return newTransaction;
    }

    public Transaction sendFunds(PublicKey recipient, float value, byte[] signature){
        if(getBalance() < value) { //gather balance and check funds.
            return null;
        }
        final List<TransactionInput> inputs = getTransactionInputs(value);
        final Transaction newTransaction = new Transaction(publicKey, recipient , value, inputs, signature);
        inputs.forEach(i -> unspentTransactionOutputs.remove(i.getTransactionOutputId()));
        return newTransaction;
    }

    private List<TransactionInput> getTransactionInputs(float value) {
        final List<TransactionInput> inputs = new ArrayList<>();

        float total = 0;
        for (Map.Entry<String, TransactionOutput> item: unspentTransactionOutputs.entrySet()){
            final TransactionOutput output = item.getValue();
            total += output.getValue();
            inputs.add(new TransactionInput(output.getId()));
            if(total > value) break;
        }
        return inputs;
    }



}