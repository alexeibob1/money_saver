package com.company;

import java.sql.*;
import java.util.HashSet;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Подключение к БД Microsoft Access (accdb) используя драйвер UCanAccess JDBC
 * Установка драйвера UCanAccess в Intellij IDEA
 * https://www.youtube.com/watch?v=xM1KNbRkF3A
 */
public class ExpenseAccounting {
	public static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {

		//Подключение к БД MS ACCESS. Используем драйвер UCanAccess. Указан полный путь к файлу БД (.accdb)
		String databaseURL = "jdbc:ucanaccess://d://Database_Accounting.accdb";
		int userChoice = -1;
		boolean closeProg = false;
		String menuMsg = "\n╔════════════════════════════════════╗" +
				"\n║                Меню                ║" +
				"\n╠═══╦════════════════════════════════╣" +
				"\n║ 1 ║ Создать счет                   ║" +
				"\n║ 2 ║ Создать категорию              ║" +
				"\n║ 3 ║ Пополнить счет                 ║" +
				"\n║ 4 ║ Внести расход средств по счету ║" +
				"\n║ 5 ║ История операций               ║" +
				"\n║ 6 ║ Удалить счет                   ║" +
				"\n║ 7 ║ Удалить категорию              ║" +
				"\n║ 8 ║ Удалить операцию               ║" +
				"\n║ 9 ║ Статистика по категориям       ║" +
				"\n╠═══╬════════════════════════════════╣" +
				"\n║ 0 ║ Выйти                          ║" +
				"\n╚═══╩════════════════════════════════╝" +
				"\nВыберите пункт Меню: ";
		do {
			try {
				Connection connection = DriverManager.getConnection(databaseURL);
				userChoice = checkMenuUserChoice(connection, "mainmenu", menuMsg);

				switch (userChoice) {
					//Создать счет
					case 1 -> addAccount(connection);
					//Создать категорию
					case 2 -> addCategory(connection);
					//Пополнить счет
					case 3 -> addActivity2Account(connection, 1);
					//Внести расход средств по счету
					case 4 -> addActivity2Account(connection, -1);
					//История операций
					case 5 -> outputAccountActivity(connection, -1, 5);
					//Удалить счет
					case 6 -> {
						deleteAccount(connection);
						outputAllAccountsInfo(connection);
					}
					//Удалить категорию
					case 7 -> {
						deleteCategory(connection);
						outputAllAccountsInfo(connection);
					}
					//Удалить операцию
					case 8 -> {
						deleteAccountActivity(connection, -1);
						outputAllAccountsInfo(connection);
					}
					//Статистика по категориям
					case 9 -> categoryStatistics(connection);
					//выход из программы
					case 0 -> closeProg = true;
					//если пользователь сделал некорректный выбор
					default -> {
						System.err.println("Некорректный ввод! Введите число из списка!");
						closeProg = false;
					}
				}

				//   connection.close();
			} catch (SQLException e) {
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

			if (userChoice != 0) {
				closeProg = back2menu();
			}

		} while (!closeProg);

		System.out.print("\nЗавершение программы... ");

	}

	//Проверка пользовательского ввода
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

	//Возврат в основное меню (1) или выход (0)
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

	//Проверка данных введенных пользователем (integer or not)
	static int checkIntOrNot(String consoleInput) {
		Integer choice = null;
		try {
			choice = Integer.parseInt(consoleInput);
		} catch (NumberFormatException e) {
			choice = -1;
		}
		return choice;
	}

	//Проверка суммы введенной пользователем (double or not)
	static boolean checkDoubleOrNot(String consoleInput) {
		boolean isCorrect = false;
		try {
			double userSum = Double.parseDouble(consoleInput);
			isCorrect = true;
		} catch (NumberFormatException e) {
			isCorrect = false;
		}
		return isCorrect;
	}

	//Подсчет суммы
	static double countSum(double currentBalance, double transactionSum, int flag) {
		return Math.round((Math.abs(currentBalance) + Math.abs(transactionSum) * flag) * 100) / 100.0;
	}

	//Проверка пользовательского ввода суммы
	static double checkTransactionAmount() {
		double transactionAmount = 0d;
		boolean isCorrect = false;
		while (!isCorrect) {
			String consoleInput = scanner.nextLine();
			isCorrect = checkDoubleOrNot(consoleInput);
			if (isCorrect) {
				transactionAmount = Double.parseDouble(consoleInput);
			} else {
				System.err.print("Некорректный ввод суммы! Используйте формат (рублей.копеек): ");
			}
		}
		return Math.round(transactionAmount * 100.0) / 100.0;
	}
}
