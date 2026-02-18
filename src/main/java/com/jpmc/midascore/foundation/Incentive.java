package com.jpmc.midascore.foundation;

public class Incentive {

    private Float amount;

    // Default constructor (required for JSON deserialization)
    public Incentive() {
    }

    public Incentive(Float amount) {
        this.amount = amount;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Incentive{" +
                "amount=" + amount +
                '}';
    }
}
