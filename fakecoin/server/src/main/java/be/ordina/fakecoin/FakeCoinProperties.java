package be.ordina.fakecoin;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("fakecoin")
public class FakeCoinProperties {

    private int difficulty = 5;

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }
}
