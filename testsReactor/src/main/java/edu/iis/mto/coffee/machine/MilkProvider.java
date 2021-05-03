package edu.iis.mto.coffee.machine;

public interface MilkProvider {

    void heat();

    /**
     * @param milkAmount
     *            - the amount of milk to be poured
     * @return real amount of poured milk <= milkAmount
     */
    int pour(int milkAmount);

}
