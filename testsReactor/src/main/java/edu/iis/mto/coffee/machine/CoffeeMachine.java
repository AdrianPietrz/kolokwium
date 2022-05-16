package edu.iis.mto.coffee.machine;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import edu.iis.mto.coffee.CoffeeReceipe;
import edu.iis.mto.coffee.CoffeeReceipes;
import edu.iis.mto.coffee.CoffeeSize;
import edu.iis.mto.coffee.CoffeeType;
import edu.iis.mto.coffee.Status;

public class CoffeeMachine {

    private final CoffeeGrinder grinder;
    private final MilkProvider milkProvider;
    private final CoffeeReceipes receipes;

    public CoffeeMachine(CoffeeGrinder grinder, MilkProvider milkProvider, CoffeeReceipes receipes) {
        this.grinder = requireNonNull(grinder, "ginder == null");
        this.milkProvider = requireNonNull(milkProvider, "milkProvider == null");
        this.receipes = requireNonNull(receipes, "receipes == null");
    }

    public Coffee make(CoffeeOrder order) {
        Coffee coffee = new Coffee();
        if (isNull(receipes.getReceipe(order.getType()))) {
            coffee.setStatus(Status.ERROR);
            return coffee;
        }
        try {
            grindCoffee(order.getSize());
            coffee = create(order);
            if (isMilkCoffee(order.getType())) {
                addMilk(order, coffee);
            }
        } catch (Exception e) {
            coffee.setStatus(Status.ERROR);
            coffee.setMessage(e.getMessage());
            return coffee;
        }
        coffee.setStatus(Status.READY);
        return coffee;
    }

    private void addMilk(CoffeeOrder order, Coffee coffee) throws HeaterException {
        int milkAmount = receipes.getReceipe(order.getType())
                                 .getMilkAmount();
        milkProvider.heat();
        int poured = milkProvider.pour(milkAmount);
        coffee.setMilkAmout(poured);
    }

    private boolean isMilkCoffee(CoffeeType type) {
        return receipes.getReceipe(type)
                       .withMilk();
    }

    private void grindCoffee(CoffeeSize coffeeSize) throws GrinderException {
        if (!grinder.grind(coffeeSize)) {
            throw new CoffeeMachineException("no coffee beans available");
        }
    }

    private Coffee create(CoffeeOrder order) {
        Coffee coffee = new Coffee();
        CoffeeReceipe receipe = receipes.getReceipe(order.getType());
        Integer waterAmount = receipe.getWaterAmount(order.getSize());
        coffee.setWaterAmount(waterAmount);
        return coffee;
    }
}
