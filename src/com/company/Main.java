package com.company;

import java.sql.*;
import java.util.HashSet;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * this is the central class in which the body of the program is located
 * */
class ExpenseAccounting {
	/**
	 * import java.util.Scanner to get information from the console.
	 */
	public static Scanner scanner = new Scanner(System.in);
	/**
	 * this is the central method inside it, other methods are created and used.
	 * */
	public static void main(String[] args) {

		//Connection with DB MS ACCESS. Using drawer UCanAccess. Input correct path to our DB (.accdb)
		String databaseURL = "jdbc:ucanaccess://d://Database_Accounting.accdb";
		int userChoice = -1;
		boolean closeProg = false;
		String menuMsg =
				"\nг====================================¬" +
						"\n¦                Menu                ¦" +
						"\n¦===T================================¦" +
						"\n¦ 1 ¦ Create account                 ¦" +
						"\n¦ 2 ¦ Create category                ¦" +
						"\n¦ 3 ¦ Top up account                 ¦" +
						"\n¦ 4 ¦ Enter expense on the account   ¦" +
						"\n¦ 5 ¦ Operations history             ¦" +
						"\n¦ 6 ¦ Delete account                 ¦" +
						"\n¦ 7 ¦ Delete category                ¦" +
						"\n¦ 8 ¦ Delete operation               ¦" +
						"\n¦ 9 ¦ Category statistics            ¦" +
						"\n¦===+================================¦" +
						"\n¦ 0 ¦ Exit                           ¦" +
						"\nL===¦================================-" +
						"\nSelect Menu item: ";


		do {
			try {
				Connection connection = DriverManager.getConnection(databaseURL);
				userChoice = checkMenuUserChoice(connection, "mainmenu", menuMsg);

				switch (userChoice) {

					case 1 -> addAccount(connection);

					case 2 -> addCategory(connection);

					case 3 -> addActivity2Account(connection, 1);

					case 4 -> addActivity2Account(connection, -1);

					case 5 -> outputAccountActivity(connection, -1, 5);

					case 6 -> {
						deleteAccount(connection);
						outputAllAccountsInfo(connection);
					}

					case 7 -> {
						deleteCategory(connection);
						outputAllAccountsInfo(connection);
					}

					case 8 -> {
						deleteAccountActivity(connection, -1);
						outputAllAccountsInfo(connection);
					}

					case 9 -> categoryStatistics(connection);

					case 0 -> closeProg = true;

					default -> {
						System.err.println("Некорректный ввод! Введите число из списка!");
						closeProg = false;
					}
				}

				//   connection.close();
			}
			catch (SQLException e) {
				//вывести сообщение ошибке в консоль
				System.err.println("При работе с базой данных произошла ошибка! Проверьте информацию в логе и состояние базы данных");
				System.err.println("Error Message: " + e.getMessage());
				System.err.println("Error Code: " + e.getErrorCode());
				System.err.println("SQLState: " + e.getSQLState());
				//System.err.println("Trace: ");
				//e.printStackTrace();
				userChoice = 0; //не выводить "backmenu"
				closeProg = true; //выйти из программы
			}

			if (userChoice !=0 ) {
				closeProg = back2menu();
			}

		} while (!closeProg);

		System.out.print("\nЗавершение программы... ");

	}

	/**
	 * This method check user input
	 * @param connection it is the link between project and DB
	 * @param menuType it if our work space (account, category and other)
	 * @param msg contains menu of this project
	 * @throws SQLException in case of errors in working with DB.
	 * @return correct user input else ask new value
	 */
	static int checkMenuUserChoice(Connection connection, String menuType, String msg) throws SQLException {
		boolean isCorrect = false;
		Integer choice = null;
		System.out.print(msg);
		while (!isCorrect) {
			String consoleInput = scanner.nextLine();
			choice = checkIntOrNot(consoleInput);
			if (choice != -1) {
				isCorrect = checkNumberInRange(connection, choice, menuType);
			}
			if (!isCorrect) {
				System.err.print("Некорректный ввод! Введите число из списка: ");
			}
		}
		return choice;
	}

	/**
	 * This method check what user want exit the program or continue working
	 * @return false if user want to stop work(input 1), true if user want to continue work(input 0), else ask new value
	 */
	static boolean back2menu() {
		Integer backMenuChoice = null;
		boolean isCorrect = false;
		boolean rezult = false;
		System.out.print("\nДля возврата в \"Меню\" введите 1, для выхода из программы введите 0: ");

		do {
			String consoleInput = scanner.nextLine();
			backMenuChoice = checkIntOrNot(consoleInput);

			switch (backMenuChoice) {
				//выход
				case 0 -> {
					rezult = true;
					isCorrect = false;
				}
				//в меню
				case 1 -> {
					rezult = false;
					isCorrect = false;
				}
				//если пользователь сделал некорректный выбор
				default -> {
					System.err.print("Некорректный ввод! Введите число из списка!");
					isCorrect = true;
				}
			}
		} while (isCorrect);
		return rezult;
	}

	/**
	 * This method check user input, integer  this input or not
	 * @param consoleInput checking value
	 * @return start value if it is integer else changes it to -1
	 */
	public static int checkIntOrNot(String consoleInput) {
		Integer choice = null;
		try {
			choice = Integer.parseInt(consoleInput);
		} catch (NumberFormatException e) {
			choice = -1;
		}
		return choice;
	}

	/**
	 * This method check user input, double this input or not
	 * @param consoleInput checking value
	 * @return true if checking value is correct else return false
	 * */
	public static boolean checkDoubleOrNot(String consoleInput) {
		boolean isCorrect = false;
		try {
			double userSum = Double.parseDouble(consoleInput);
			isCorrect = true;
		} catch (NumberFormatException e) {
			isCorrect = false;
		}
		return  isCorrect;
	}

	/**
	 * this method calculates new balance amount
	 * @param currentBalance this is the start amount on the balance
	 * @param transactionSum this is the sum of the balance change.
	 * @param flag shows what we do increase balance or decrease.
	 * @return this method returns the changed balance
	 * */
	static double countSum(double currentBalance, double transactionSum, int flag ) {
		return  Math.round((Math.abs(currentBalance) + Math.abs(transactionSum) * flag) * 100) / 100.0;
	}
