package be.ordina.fakecoin;

import be.ordina.fakecoin.transaction.TransactionInputChannels;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;

@SpringBootApplication
@EnableBinding(TransactionInputChannels.class)
public class FakeCoinApplication {

	public static void main(String[] args) {
		SpringApplication.run(FakeCoinApplication.class, args);
	}
}
