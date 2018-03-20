package be.ordina.fakecoin.wallet;

import be.ordina.fakecoin.Wallet;

public class WalletResource {

    private String address;
    private String name;
    private String publicKey;
    private String privateKey;
    private float balance;

    public WalletResource(Wallet wallet){
        this.address = wallet.getAddress();
        this.name = wallet.getName();
        this.publicKey = wallet.getEncodedPublicKey();
        this.privateKey = wallet.getEncodedPrivateKey(); // For demo purposes, we store the private key and make it public
        this.balance = wallet.getBalance();
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public float getBalance() {
        return balance;
    }
}
