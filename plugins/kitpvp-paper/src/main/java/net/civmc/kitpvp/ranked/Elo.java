package net.civmc.kitpvp.ranked;

public class Elo {

    private static final double K = 32;

    public static EloChange getChange(double player, double opponent) {
        double playerExpected = 1.0 / (1.0 + Math.pow(10, (opponent - player) / 400));

        return new EloChange(
            K * (1 - playerExpected),
            K * (0.5 - playerExpected),
            K * (0 - playerExpected)
        );
    }

    public record EloChange(double win, double draw, double loss) {

    }

}
