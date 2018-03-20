package be.ordina.fakecoin.wallet;

import be.ordina.fakecoin.BlockchainService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
public class WalletController {

    private BlockchainService blockchainService;

    public WalletController(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
    }

    @GetMapping("/wallets")
    public ResponseEntity<WalletResources> getWallets(){
        return ResponseEntity.ok(new WalletResources(blockchainService.getWallets().stream()
                .map(WalletResource::new)
                .collect(Collectors.toList())));
    }

    @GetMapping("/wallets/{walletId}")
    public ResponseEntity getWallet(
            @PathVariable final String walletId) {
        return blockchainService.getWallet(walletId)
                .map(WalletResource::new)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
