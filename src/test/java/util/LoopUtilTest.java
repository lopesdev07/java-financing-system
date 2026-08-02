package util;

import org.junit.jupiter.api.Test;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static util.LoopUtil.getValidOption;

public class LoopUtilTest {

    @Test
    void optionMustNotEnterTheLoop() {
        Scanner scanner = new Scanner("");
        int option = 3;
        int result = getValidOption(scanner, option, 1, 4);

        assertEquals(3, result);

    }

    @Test
    void mustLoopUntilValidOptionIsEntered() {
        Scanner scanner = new Scanner("3\n1\n");
        int option = 3;
        int result = getValidOption(scanner, option, 1, 2);

        assertEquals(1, result);
    }

    @Test
    void mustAcceptMinBoundaryWithoutLooping() {
        Scanner scanner = new Scanner("");
        int option = 1;

        int result = getValidOption(scanner, option, 1, 2);

        assertEquals(1, result);
    }

    @Test
    void mustAcceptMaxBoundaryWithoutLooping() {
        Scanner scanner = new Scanner("");
        int option = 2;

        int result = getValidOption(scanner, option, 1, 2);

        assertEquals(2, result);
    }
}
