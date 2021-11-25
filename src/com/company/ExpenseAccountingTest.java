package com.company;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;



public class ExpenseAccountingTest {
    Main expenseAccounting = new Main();

    @Test
    public void CheckIntOrNotTestInt () {
        String strIn = "1";
        int expResult = 1;
        int actResult = expenseAccounting.checkIntOrNot(strIn);
        assertEquals(expResult,actResult);
    }

    @Test
    public void CheckIntOrNotTestChars () {
        assertEquals(-1,expenseAccounting.checkIntOrNot("aaa"));
    }

    @Test
    public void CheckIntOrNotTestNotExpected () {
        assertNotEquals("aaa",expenseAccounting.checkIntOrNot("aaa"));
    }

    @Test
    public void countSumTestAddition () {
        double currentBalance = 20.50;
        double transactionSum = 10.30;
        int flag = 1;
        double expResult = 30.80;
        double actResult = expenseAccounting.countSum(currentBalance, transactionSum, flag);
        assertEquals(expResult,actResult);
    }

    @Test
    public void countSumTestSubtraction () {
        assertEquals(10.20,expenseAccounting.countSum(20.50, 10.30, -1));
    }

    @Test
    public void CheckDoubleOrNotTestDouble () {
        assertTrue(expenseAccounting.checkDoubleOrNot("20.50"));
    }

    @Test
    public void CheckDoubleOrNotTestComma () {
        assertFalse(expenseAccounting.checkDoubleOrNot("20,50"));
    }

    @Test
    public void CheckDoubleOrNotTestChars () {
        assertFalse(expenseAccounting.checkDoubleOrNot("aa.bb"));
    }

    @Test
    public void checkDateFormatTestCorrectDate () {
        assertEquals("2021-11-23",expenseAccounting.checkDateFormat("2021-11-23"));
    }

    @Test
    public void checkDateFormatTestDotInDate () {
        assertEquals("error",expenseAccounting.checkDateFormat("2021.11.23"));
    }

    @Test
    public void checkDateFormatTestNotExpected () {
        assertNotEquals("2021.11.23",expenseAccounting.checkDateFormat("2021.11.23"));
    }

    @Test
    public void checkDateFormatTestDateEmpty () {
        LocalDate systemDate = LocalDate.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        assertEquals(dtf.format(systemDate),expenseAccounting.checkDateFormat(""));
    }

    @Test
    public void checkNameLengthTestCorrectStringLength () {
        assertTrue(expenseAccounting.checkNameLength("qwertyuioPasdfghjklZ"));
    }

    @Test
    public void checkNameLengthTestEmptyString () {
        assertFalse(expenseAccounting.checkNameLength(""));
    }

    @Test
    public void checkNameLengthTestIncorrectStringLength () {
        assertFalse(expenseAccounting.checkNameLength("qwertyuioPasdfghjklZx"));
    }

    @Test
    public void calcDoubleTestAddition () {
        assertEquals(22.10,expenseAccounting.calcDouble(20.10, 2.0, '+'));
    }

    @Test
    public void calcDoubleTestSubtraction () {
        assertEquals(18.10,expenseAccounting.calcDouble(20.10, 2.0, '-'));
    }

    @Test
    public void calcDoubleTestMultiplication () {
        assertEquals(40.20,expenseAccounting.calcDouble(20.10, 2.0, '*'));
    }

    @Test
    public void calcDoubleTestPercent () {
        assertEquals(10.0,expenseAccounting.calcDouble(50.0, 20.0, '%'));
    }

    @Test
    public void calcDoubleTestDivision () {
        assertEquals(10.05,expenseAccounting.calcDouble(20.10, 2.0, '/'));
    }
    @Test
    public void calcDoubleTestDivisionZero () {
        Throwable exception = assertThrows(ArithmeticException.class, () -> {expenseAccounting.calcDouble(20.10, 0, '/');});
    }

}