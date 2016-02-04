package edu.unc.cs.niograderserver.graderHandler.util;

/**
 *
 * @author Andrew Vitkus
 */
public class GradingData implements IGradingData {

    private final String onyen, first, last;
    private final double score, possible;

    public GradingData(String onyen, String first, String last, double score, double possible) {
        this.onyen = onyen;
        this.first = first;
        this.last = last;
        this.score = score;
        this.possible = possible;
    }

    @Override
    public String getOnyen() {
        return onyen;
    }

    @Override
    public String getFirstName() {
        return first;
    }

    @Override
    public String getLastName() {
        return last;
    }

    @Override
    public double getScore() {
        return score;
    }

    @Override
    public double getPossible() {
        return possible;
    }

}
