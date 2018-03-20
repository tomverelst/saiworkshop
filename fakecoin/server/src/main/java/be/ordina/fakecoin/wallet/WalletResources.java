package be.ordina.fakecoin.wallet;

import java.util.List;

public class WalletResources {

    private List<WalletResource> wallets;

    public WalletResources(final List<WalletResource> wallets){
        this.wallets = wallets;
    }

    public List<WalletResource> getWallets() {
        return wallets;
    }

    public void setWallets(List<WalletResource> wallets) {
        this.wallets = wallets;
    }
}
