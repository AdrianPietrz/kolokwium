package edu.iis.mto.coffee;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import edu.iis.mto.coffee.machine.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class CoffeeMachineTest {

    @Mock
    CoffeeGrinder coffeeGrinder;

    @Mock
    MilkProvider milkProvider;

    @Mock
    CoffeeReceipes coffeeReceipes;

    private CoffeeMachine coffeeMachine;
    private CoffeeOrder basicOrder;
    private CoffeeReceipe basicRecipe;
    private Map<CoffeeSize, Integer> waterAmounts = new HashMap<>();

    @BeforeEach
    void init() throws GrinderException {
        coffeeMachine = new CoffeeMachine(coffeeGrinder,milkProvider,coffeeReceipes);
        basicOrder = CoffeeOrder.builder().withSize(CoffeeSize.STANDARD).withType(CoffeeType.ESPRESSO).build();

        waterAmounts.put(CoffeeSize.STANDARD,10);
        basicRecipe = CoffeeReceipe.builder().withMilkAmount(0).withWaterAmounts(waterAmounts).build();

        lenient().when(coffeeReceipes.getReceipe(ArgumentMatchers.any())).thenReturn(basicRecipe);
        lenient().when(coffeeGrinder.grind(ArgumentMatchers.any())).thenReturn(true);
    }

    @Test
    void methodShouldReturnStatusErrorWhenThereIsNoRequiredRecipe() {

        lenient().when(coffeeReceipes.getReceipe(ArgumentMatchers.any())).thenReturn(null);

        assertEquals(Status.ERROR,coffeeMachine.make(basicOrder).getStatus());
    }

    @Test
    void methodShouldReturnMessageErrorWhenThereIsNotEnoughCoffeeBeans() throws GrinderException {

        when(coffeeReceipes.getReceipe(ArgumentMatchers.any())).thenReturn(basicRecipe);
        when(coffeeGrinder.grind(ArgumentMatchers.any())).thenReturn(false);

        Coffee coffee = coffeeMachine.make(basicOrder);

        assertEquals("no coffee beans available",coffee.getMessage());
    }

    @Test
    void methodShouldReturnStatusReadyWhenEverythingIsCorrect(){
        assertEquals(Status.READY,coffeeMachine.make(basicOrder).getStatus());
    }

    @Test
    void methodShouldNotSetAnyErrorMessageWhenEverythingIsCorrect(){
        assertEquals(null,coffeeMachine.make(basicOrder).getMessage());
    }

    @Test
    void methodShouldReturnCorrectlyMadeCoffee(){
        Coffee coffee = coffeeMachine.make(basicOrder);

        assertEquals(0,coffee.getMilkAmout());
        assertEquals(10,coffee.getWaterAmount());
    }

    @Test
    void methodShouldReturnStatusErrorWhenMilkProviderThrowsException() throws HeaterException {
        basicRecipe = CoffeeReceipe.builder().withWaterAmounts(waterAmounts).withMilkAmount(10).build();

        when(coffeeReceipes.getReceipe(ArgumentMatchers.any())).thenReturn(basicRecipe);
        doThrow(HeaterException.class).when(milkProvider).heat();

        assertEquals(Status.ERROR,coffeeMachine.make(basicOrder).getStatus());
    }

    @Test
    void methodShouldPourCorrectAmountOfMilkIfCoffeeIsWithMilk(){
        basicRecipe = CoffeeReceipe.builder().withWaterAmounts(waterAmounts).withMilkAmount(10).build();

        when(coffeeReceipes.getReceipe(any())).thenReturn(basicRecipe);
        when(milkProvider.pour(anyInt())).thenReturn(basicRecipe.getMilkAmount());

        Coffee coffee = coffeeMachine.make(basicOrder);

        assertEquals(basicRecipe.getMilkAmount(),coffee.getMilkAmout());
    }


    @Test
    void methodShouldTryToGrindCoffeeOnce() throws GrinderException {

        coffeeMachine.make(basicOrder);

        verify(coffeeGrinder,times(1)).grind(any());
    }

    @Test
    void methodShouldHeatMilkOnceIfCoffeeIsWithMilk() throws HeaterException {
        basicRecipe = CoffeeReceipe.builder().withWaterAmounts(waterAmounts).withMilkAmount(10).build();

        when(coffeeReceipes.getReceipe(ArgumentMatchers.any())).thenReturn(basicRecipe);

        coffeeMachine.make(basicOrder);

        verify(milkProvider,times(1)).heat();
    }

    @Test
    void methodShouldNotHeatMilkIfCoffeeIsWithoutMilk() throws HeaterException {

        coffeeMachine.make(basicOrder);

        verify(milkProvider,times(0)).heat();
    }

    @Test
    void methodShouldCallPourMilkOnceIfCoffeeIsWithMilk(){
        basicRecipe = CoffeeReceipe.builder().withWaterAmounts(waterAmounts).withMilkAmount(10).build();

        when(coffeeReceipes.getReceipe(any())).thenReturn(basicRecipe);

        coffeeMachine.make(basicOrder);

        verify(milkProvider,times(1)).pour(anyInt());
    }

    @Test
    void methodShouldNotCallPourMilkIfCoffeIsWithoutMilk(){
        coffeeMachine.make(basicOrder);

        verify(milkProvider,times(0)).pour(anyInt());
    }





}
