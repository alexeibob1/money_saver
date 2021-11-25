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
public class Main {
    public static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        //Подключение к БД MS ACCESS. Используем драйвер UCanAccess. Указан полный путь к файлу БД (.accdb)
        String databaseURL = "jdbc:ucanaccess://D:/Documents/ОПИ/Проект/Версия6/Project_Accounting/db/DatabaseTemplate.accdb";
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

    //Проверка даты введенной пользователем (соответствие формату ГГГГ-ММ-ДД)
    static String checkDateFormat(String consoleInput) {
        boolean flag = true;
        boolean isFormatCorrect = true;
        int numTmp;
        int partLength;

        //если пользователь не вводил дату, а нажал ввод, то присвоить текущую дату
        if (consoleInput.isEmpty()) { //проверяем что строка пустая. при этом символ \n (enter) не учитывается
            LocalDate systemDate = LocalDate.now(); //получаем текущую дату
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd"); //подготавливаем шаблон для форматирования даты в строку
            consoleInput = dtf.format(systemDate); //применяем шаблон к дате при конвертации строку
        }

        //проверка строки на формат ГГГГ-ММ-ДД
        String[] splitDate = consoleInput.split("-"); //разделяем строку на части, где разделитель символ тире

        for (int i = 0; i < splitDate.length; i++) {
            String datePart = splitDate[i];
            partLength = datePart.length();

            if (i == 0 && partLength != 4) { //проверяем что первая часть строки имеет длину 4 символа, что соответствует записи года (ГГГГ)
                isFormatCorrect = false;
                flag = false;
            } else if (i > 0 && datePart.length() != 2) { //проверяем что 2-я и 3-я часть строки имеет длину 2 символа, что соответствует записи месяца (ММ) и дня (ДД)
                isFormatCorrect = false;
                flag = false;
            }

            //если количество символов соответствует части строки, то проверяем что это цифры
            if (isFormatCorrect) {
                try {
                    numTmp = Integer.parseInt(datePart); //парсинг части строки в число
                } catch (NumberFormatException e) {
                    flag = false;
                }
            }
        }

        if (!isFormatCorrect || !flag) {
            consoleInput = "error";
        }
        return consoleInput;
    }


    //Проверка длины строки, вводимой пользователем (для нового номера счета и новой категории. формат: 1-20 символов)
    static boolean checkNameLength(String consoleInput) {
        boolean isFormatCorrect = true;

        if (consoleInput.isEmpty() || consoleInput.length() > 20) { //проверяем на пустую строку и на ограничение в 20 символов
            isFormatCorrect = false;
        }
        return isFormatCorrect;
    }


    //Проверка строки, вводимой пользователем (для нового номера счета и новой категории. формат: 1-20 символов)
    static String checkNameString(Connection connection, String consoleInput, String menuType) throws SQLException {
        boolean isCorrect = false;
        HashSet<String> nameSet = new HashSet<>(); //объявляем коллекцию строк
        //подготавливаем запрос для получения названий. LCASE() - приводит текст к нижнему регистру. "as NAME" указано для универсальности выборки из ResultSet.
        String sql = "SELECT LCASE(ACCOUNT_NAME) as NAME FROM accounts";
        switch (menuType) {
            case "account":
                sql = "SELECT LCASE(ACCOUNT_NUMBER) as NAME FROM accounts";
                break;
            case "category":
                sql = "SELECT LCASE(CATEGORY_INFO) as NAME FROM categories";
                break;
        }

        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(sql);
        nameSet.clear(); //очищаем (обнуляем) коллекцию

        while (result.next()) { // пока ResultSet не станет пустым извлекаем данные
            nameSet.add(result.getString("NAME")); //заполняем коллекцию. именно для этой строки необходимо в запросах заменять имя выбираемого столбца на "NAME"
        }

        while (!isCorrect) {
            if (!checkNameLength(consoleInput)) { //проверяем на пустую строку и на ограничение в 20 символов
                System.err.print("Введенные данные не соответствуют условию \"1-20 символов\"! Повторите ввод (1-20 символов): ");
                isCorrect = false;
                consoleInput = scanner.nextLine();
            } else if (nameSet.contains(consoleInput.toLowerCase())) { //приводим к нижнему регистру и проверяем вхождение в коллекцию
                switch (menuType) {
                    case "account" -> System.err.print("\nВведенный номер счета уже присутствует в базе данных! Введите новый счет (1-20 символов): ");
                    case "category" -> System.err.print("\nВведенное имя категории уже присутствует в базе данных! Введите новую категорию (1-20 символов): ");
                }
                consoleInput = scanner.nextLine();
                isCorrect = false;
            } else {
                isCorrect = true;
            }
        }
        return consoleInput;
    }

    //Заполнение множества для проверки пунктов меню
    private static boolean checkNumberInRange(Connection connection, int userChoice, String menuType) throws SQLException {

        HashSet<Integer> menuSet = new HashSet<>(); //объявляем коллекцию типа HashSet<Integer>

        String sql = "SELECT ID FROM menu_items WHERE ID = 0";
        int menuNum = 0;

        // формируем sql текст запрос в зависимости от того, для какого меню на экране необходимо проверить значение вводимое пользователем
        // запрос вернет список значений поля уникального ключа для соответствующей таблицы (счета, транзакции, категории).
        // для основного и меню возврата, данные формируются из статической таблицы main_menu

        switch (menuType) {
            case "mainmenu":
                sql = "SELECT ID FROM menu_items ORDER by ID";
                break;
            case "backmenu":
                sql = "SELECT ID FROM menu_items WHERE ID in (0 , 1)";
                break;
            case "accounts":
                sql = "SELECT ID FROM accounts ORDER by ID";
                break;
            case "categories":
                // запись вида CATEGORY_ID as ID позволяет поменять в получаемом результате запросе название колонки с CATEGORY_ID на ID
                // чтобы далее однотипно выполнять извлечение данных из ResultSet командой result.getInt("ID")
                sql = "SELECT CATEGORY_ID as ID FROM categories ORDER by CATEGORY_ID";
                break;
            case "activities":
                sql = "SELECT TRANSACTION_ID as ID FROM account_activity ORDER by TRANSACTION_ID";
                break;
        }
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(sql);
        menuSet.clear(); //очищаем (обнуляем) коллекцию

        while (result.next()) { // пока ResultSet не станет пустым извлекаем данные
            menuNum = result.getInt("ID"); // тип извлекаемых данных должен соответствовать типу данных в таблице
            menuSet.add(menuNum); //заполняем коллекцию
        }

        return menuSet.contains(userChoice); //проверяем есть ли такое число в коллекции
    }

    static double calcDouble(double double1, double double2, char operation) {
        double result = 0d;
        switch (operation) {
            case '+':
                result = double1 + double2;
                break;
            case '-':
                result = double1 - double2;
                break;
            case '*':
                result = double1 * double2;
                break;
            case '%':
                result = (double2 * double1) / 100;
                break;
            case '/':
                if (double2 == 0) {
                    throw new ArithmeticException("division by zero");
                } else {
                    result = double1 / double2;
                }
                break;
            default:
                System.out.println("Ошибка! Знак арифметической операции не распознан.");
        }
        return result;
    }

    //добавить новый счет
    private static void addAccount(Connection connection) throws SQLException {

        outputAllAccountsInfo(connection); //показать все счета на текущий момент
        System.out.print("\nВведите новый номер банковского счета (1-20 символов): ");
        String accountNumber = scanner.nextLine();
        accountNumber = checkNameString(connection, accountNumber, "account");

        String sql = "INSERT into accounts (ACCOUNT_NUMBER, CURRENT_BALANCE) VALUES (?, 0)"; //подготовка текста параметризованного sql запроса на вставку строки данных в таблицу
        PreparedStatement preparedStatement = connection.prepareStatement(sql); //форматируем/подготавливаем запрос к подстановке параметров
        preparedStatement.setString(1, accountNumber); // подставляем параметр

        int qRow = preparedStatement.executeUpdate(); //выполняет SQL-команды вида INSERT, UPDATE, DELETE, CREATE и возвращает количество измененных строк

        if (qRow > 0) {
            System.out.print("\nНовый счет успешно добавлен.");
            System.out.println();
        }

        outputAllAccountsInfo(connection);
    }

    //добавить новую категорию
    private static void addCategory(Connection connection) throws SQLException {
        outputAllCategories(connection); //показать все категории на текущий момент
        System.out.print("\nВведите новую категорию (1-20 символов): ");
        String categoryInfo = scanner.nextLine();
        categoryInfo = checkNameString(connection, categoryInfo, "category");

        String sql = "INSERT into categories (CATEGORY_INFO) VALUES (?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, categoryInfo);

        int qRow = preparedStatement.executeUpdate();

        if (qRow > 0) {
            System.out.print("\nНовая категория успешно добавлена.");
            System.out.println();
        }
        outputAllCategories(connection); //показать все категории после ввода новой категории
    }

    //Вывести список всех категорий, кроме категории "Пополнение счета", у которой код (CATEGORY_ID) = 0. Скрываем ее наличие от пользователя.
    private static void outputAllCategories(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("SELECT CATEGORY_ID, CATEGORY_INFO FROM categories WHERE CATEGORY_ID > 0 ORDER by CATEGORY_ID");//выполняем команду SELECT, которая возвращает данные в виде ResultSet

        System.out.println("\nКатегории расходов:");
        System.out.println("\n╔═══════╦══════════════════════╗");
        System.out.format("║ %-5s ║ %-20s ║", "  №", "     Категория");

        while (result.next()) { // пока ResultSet не станет пустым извлекаем данные
            int categoryId = result.getInt("CATEGORY_ID"); // тип извлекаемых данных соответствует типу данных в таблице для каждого поля
            String categoryInfo = result.getString("CATEGORY_INFO");

            System.out.print("\n╠═══════╬══════════════════════╣");
            System.out.format("\n║ %-5d ║ %-20s ║", categoryId, categoryInfo);
        }
        System.out.println("\n╚═══════╩══════════════════════╝");
    }

    //вывести список ВСЕХ счетов
    private static void outputAllAccountsInfo(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("SELECT ID, ACCOUNT_NUMBER, CURRENT_BALANCE FROM accounts ORDER by ID");

        System.out.println("\nТекущий баланс по счетам.");

        System.out.println("\n╔═══════╦══════════════════════╦════════════╗");
        System.out.format("║ %-5s ║ %-20s ║ %-10s ║", "  №", "        Счет", "  Баланс");
        while (result.next()) {
            int accountId = result.getInt("ID");
            String accountNumber = result.getString("ACCOUNT_NUMBER");
            double currentBalance = result.getDouble("CURRENT_BALANCE");
            System.out.print("\n╠═══════╬══════════════════════╬════════════╣");
            System.out.format("\n║ %-5d ║ %-20s ║ %10.2f ║", accountId, accountNumber, currentBalance);
        }
        System.out.println("\n╚═══════╩══════════════════════╩════════════╝");
    }

    //вывести список ОДНОГО счета
    private static void outputAccountInfo(Connection connection, int accountID) throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement("SELECT ID, ACCOUNT_NUMBER, CURRENT_BALANCE FROM accounts WHERE ID = ?");
        preparedStatement.setInt(1, accountID);
        ResultSet result = preparedStatement.executeQuery(); //т.к. запрос параметризованный, подготавливаем его классом preparedStatement

        System.out.println("\nТекущий баланс по счету:");
        System.out.println("\n╔═══════╦══════════════════════╦════════════╗");
        System.out.format("║ %-5s ║ %-20s ║ %-10s ║", "  №", "        Счет", "  Баланс");
        while (result.next()) {
            int accountId = result.getInt("ID");
            String accountNumber = result.getString("ACCOUNT_NUMBER");
            double currentBalance = result.getDouble("CURRENT_BALANCE");
            System.out.print("\n╠═══════╬══════════════════════╬════════════╣");
            System.out.format("\n║ %-5d ║ %-20s ║ %10.2f ║", accountId, accountNumber, currentBalance);
        }
        System.out.println("\n╚═══════╩══════════════════════╩════════════╝");
    }

    //изменение баланса счету
    private static void changeAccountBalance(Connection connection, int accountID, double transactionAmount, int flag) throws SQLException {
        double currentBalance = 0;

        String sql = "SELECT CURRENT_BALANCE FROM accounts WHERE ID = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, accountID);
        ResultSet result = preparedStatement.executeQuery(); // получаем текущий баланс по указанному счету

        while (result.next()) {
            currentBalance = result.getDouble("CURRENT_BALANCE");
        }

        currentBalance = countSum(currentBalance, transactionAmount, flag);

        //текст параметризованного sql-запроса для обновления значения поля CURRENT_BALANCE в таблице accounts
        sql = "UPDATE accounts SET CURRENT_BALANCE = ? WHERE ID = ?";

        preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setDouble(1, currentBalance);
        preparedStatement.setInt(2, accountID);
        int qRow = preparedStatement.executeUpdate();

        if (qRow > 0) {
            System.out.println("Баланс счета успешно обновлен.");
            System.out.println();
        }
    }


    //ввод транзакции (доход/расход) по счету
    // flag = 1 - пополнение счета (доход), flag = 0 - списание со счета (расход)
    private static void addActivity2Account(Connection connection, int flag) throws SQLException {
        double transactionAmount = 0d;
        String transactionDate = "error";
        boolean isCorrect = false;
        String consoleDateInput = "";
        outputAllAccountsInfo(connection); //показываем информацию по всем счетам

        String subMenuMsg = "\nВыберите счет, указав порядковый номер счета из списка: ";
        int accountID = checkMenuUserChoice(connection, "accounts", subMenuMsg);
        int categoryID = 0;

        switch (flag) {
            case 1:
                System.out.print("\nВведите сумму пополнения счета (формат: рублей.копеек): ");
                categoryID = 0; //в таблице категорий для пополнения счета доступна только одна категория "Пополнение счета", которая имеет код 0. Лишний раз не читаем таблицу.
                break;
            case -1:
                outputAllCategories(connection); //отображаем все категории (выводит список всех категорий кроме категории "Пополнение счета" с кодом 0)
                subMenuMsg = "\nУкажите порядковый номер категории расходов из списка: ";
                categoryID = checkMenuUserChoice(connection, "categories", subMenuMsg);
                System.out.print("\nВведите сумму расходов (формат: рублей.копеек): ");
                break;
        }

        transactionAmount = checkTransactionAmount();

        System.out.print("\nУкажите дату в формате ГГГГ-ММ-ДД. Для ввода текущей даты оставьте строку пустую и нажмите Enter: ");

        while (!isCorrect) {
            consoleDateInput = scanner.nextLine();
            transactionDate = checkDateFormat(consoleDateInput);
            if (transactionDate == "error") {
                System.err.print("Некорректный ввод даты! Повторите ввод, используя формат (ГГГГ-ММ-ДД): ");
                isCorrect = false;
            } else {
                isCorrect = true;
            }
        }

        //текст параметризованного запроса на вставку данных в 4-ре поля талицы account_activity
        //конструкция format(CDate(?),"yyyy-mm-dd") принудительно заставляет БД преобразовать (CDate) значение параметра ? в соответствии с шаблоном (format) ГГГГ-ММ-ДД
        String sql = "INSERT into account_activity (ACCOUNT_ID, TRANSACTION_AMOUNT, CATEGORY_ID, TRANSACTION_DATE) VALUES (?, ?, ?, format(CDate(?),\"yyyy-mm-dd\") )";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, accountID);
        preparedStatement.setDouble(2, Math.abs(transactionAmount) * flag);
        preparedStatement.setInt(3, categoryID);
        preparedStatement.setString(4, transactionDate);
        int qRow = preparedStatement.executeUpdate();

        if (qRow > 0) {
            System.out.print("\nТранзакция успешно внесена.");
            System.out.println();
        }

        //Выполняем обновление баланса.
        // Math.abs(Х)*flag призвана исключить ситуацию когда пользователь пополняет баланс, но при этом вводит отрицательную сумму пополнения (и обратная ситуация).
        changeAccountBalance(connection, accountID, Math.abs(transactionAmount), flag);

        outputAccountInfo(connection, accountID); //выводим список всех счетов
        outputAccountActivity(connection, accountID, 5); //выводим список 5-ти последних транзакций по обновленному счету

    }

    // Вывести информацию о движении средств по указанному счету.
    // flag = 0 - все операции из account_activity, flag = 5 - последние 5 операций из account_activity
    // accountID = -1 - запросить счет у пользователя по которому отобразить информацию,
    // 0 - по всем счетам, n - только по указанному счету (для использования в методах, где номер счета до вызова уже был определен).
    private static void outputAccountActivity(Connection connection, int accountID, int flag) throws SQLException {

        // Запрос будет формироваться из трех частей. Сперва подготавливаем неизменяемый текст СЕРЕДИНЫ запроса.
        // Ведущая часть запроса (в части где должна быть команда SELECT) будет принимать две формы:
        // 1. SELECT TOP 5 -когда нам требуется получить только пять строк (последние пять операций)
        // 2. SELECT -когда нам требуется получить все операции
        // условие WHERE также будет принимать два вида:
        // 1. WHERE accounts.ID = ? -когда нам необходимо получить транзакции по заданному счету
        // 2. WHERE accounts.ID > ? -когда нам необходимо получить транзакции по всем счетам (подставляем в параметр 0, а нумерация счетов идет с 1)
        // оконечная часть запроса (условие сортировки) будет иметь неизменяемый вид и выполнять сортировку по номеру транзакции по убыванию (обратная сортировка)

        String sql = "TRANSACTION_ID, ACCOUNT_NUMBER, TRANSACTION_AMOUNT, CATEGORY_INFO, TRANSACTION_DATE " +
                "from (account_activity INNER JOIN accounts ON account_activity.ACCOUNT_ID = accounts.ID) " +
                "INNER JOIN categories ON account_activity.CATEGORY_ID = categories.CATEGORY_ID ";
        String sqlWhere = " WHERE accounts.ID = ? "; // первый вариант условия WHERE
        String sqlOrder = " ORDER by TRANSACTION_ID DESC"; // сортировка

        switch (accountID) {
            case -1:
                outputAllAccountsInfo(connection);
                String subMenuMsg = "\nДля получения истории операций по счету введите порядковый номер счета: ";
                accountID = checkMenuUserChoice(connection, "accounts", subMenuMsg);
                break;
            case 0:
                System.out.println("\nИстория операций по всем счетам.");
                sqlWhere = " WHERE accounts.ID > ? "; // второй вариант условия WHERE
                break;
            default:
                System.out.println("\nИстория последних 5-ти операций по счету.");
        }

        sql = sql + sqlWhere + sqlOrder; //собираем правую часть текст из трех частей

        if (flag == 5) {
            sql = "SELECT TOP " + flag + " " + sql; //добавляем левую (начальную) часть. вариант 1.
        } else {
            sql = "SELECT " + sql; //вариант 2.
        }

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, accountID);
        ResultSet result = preparedStatement.executeQuery();

        System.out.println("\n╔═══════╦══════════════════════╦════════════╦══════════════════════╦═════════════╗");
        System.out.format("║ %-5s ║ %-20s ║ %-10s ║ %-20s ║ %-11s ║", "  №", "        Счет", "  Сумма", "     Категория", "    Дата");

        while (result.next()) {
            int transactionId = result.getInt("TRANSACTION_ID");
            String accountNumber = result.getString("ACCOUNT_NUMBER");
            double transactionAmount = result.getDouble("TRANSACTION_AMOUNT");
            String categoryInfo = result.getString("CATEGORY_INFO");
            Date transactionDate = result.getDate("TRANSACTION_DATE");
            System.out.print("\n╠═══════╬══════════════════════╬════════════╬══════════════════════╬═════════════╣");
            System.out.format("\n║ %-5d ║ %-20s ║ %10.2f ║ %-20s ║ %-11s ║", transactionId, accountNumber, transactionAmount, categoryInfo, transactionDate);
        }
        System.out.println("\n╚═══════╩══════════════════════╩════════════╩══════════════════════╩═════════════╝");
    }

    // удаление транзакции
    // если transactionID = -1, то запросить пользователя номер транзакции для удаления
    private static void deleteAccountActivity(Connection connection, int transactionID) throws SQLException {

        int categoryId = 0;
        int accountId = 0;
        double transactionAmount = 0;
        int flag = 1;

        if (transactionID == -1) {
            outputAccountActivity(connection, 0, 0);
            String subMenuMsg = "\nДля удаления операции, укажите ее порядковый номер из списка: ";
            transactionID = checkMenuUserChoice(connection, "activities", subMenuMsg);
        }

        PreparedStatement preparedStatement = connection.prepareStatement("SELECT ACCOUNT_ID, TRANSACTION_AMOUNT, CATEGORY_ID FROM account_activity WHERE TRANSACTION_ID = ?");
        preparedStatement.setInt(1, transactionID);
        ResultSet result = preparedStatement.executeQuery();

        while (result.next()) {
            accountId = result.getInt("ACCOUNT_ID");
            transactionAmount = result.getDouble("TRANSACTION_AMOUNT");
            categoryId = result.getInt("CATEGORY_ID");
        }

        if (categoryId == 0) { // если пользователь удаляет ранее введенную транзакцию ПОПОЛНЕНИЯ счета (код категории 0), необходимо СПИСАТЬ со счета  (отрицательная сумма)
            flag = -1;
        } else {  // если пользователь удаляет ранее введенную транзакцию СПИСАНИЯ со счета (код отличный от 0), необходимо ПОПОЛНИТЬ счет (положительная сумма)
            flag = 1;
        }
        // Math.abs(Х)*flag призвана изменить знак суммы транзакции (со списания на пополнение и наоборот)

        preparedStatement = connection.prepareStatement("DELETE FROM account_activity WHERE TRANSACTION_ID = ?");
        preparedStatement.setInt(1, transactionID);
        int qRow = preparedStatement.executeUpdate();
        if (qRow > 0) {
            System.out.println("Транзакция успешно удалена.");
        }

        changeAccountBalance(connection, accountId, Math.abs(transactionAmount), flag); //передаем данные на удаление
    }

    //удаление категории
    private static void deleteCategory(Connection connection) throws SQLException {

        int categoryId = 0;
        int transactionId = 0;

        outputAllCategories(connection); //вывести список всех категорий (выводит список всех категорий кроме категории "Пополнение счета" с кодом 0)
        String subMenuMsg = "\nДля удаления категории, укажите ее порядковый номер из списка: ";
        categoryId = checkMenuUserChoice(connection, "categories", subMenuMsg);

        PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM categories WHERE CATEGORY_ID = ?"); //запрос на удаление записи из таблицы категорий
        preparedStatement.setInt(1, categoryId);
        int qRow = preparedStatement.executeUpdate();
        if (qRow > 0) {
            System.out.println("Категория успешно удалена.");
        }

        outputAllCategories(connection);

        //запрос на список всех транзакций у которых была указана удаленная категория (с возвратом средств на соответствующие счета)
        preparedStatement = connection.prepareStatement("SELECT TRANSACTION_ID FROM account_activity WHERE CATEGORY_ID = ?");

        preparedStatement.setInt(1, categoryId);
        ResultSet result = preparedStatement.executeQuery(); // получаем ResultSet со списком идентификатора транзакций

        //пока ResultSet не пустой выполняем процедуру удаления транзакции и возврата денег на счет (по одной за проход, т.е. если список из 10 транзакций = 10 проходов)
        while (result.next()) {
            transactionId = result.getInt("TRANSACTION_ID");
            deleteAccountActivity(connection, transactionId);
        }
    }

    //удаление счета c удалением всех транзакций по этому счету!!!
    private static void deleteAccount(Connection connection) throws SQLException {

        int accountId = 0;
        int transactionId = 0;

        outputAllAccountsInfo(connection); //вывести список всех счетов

        String subMenuMsg = "\nДля удаления счета, укажите его порядковый номер из списка: ";
        accountId = checkMenuUserChoice(connection, "accounts", subMenuMsg);

        PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM accounts WHERE ID = ?"); //запрос на удаление записи из таблицы счетов
        preparedStatement.setInt(1, accountId);
        int qRow = preparedStatement.executeUpdate();
        if (qRow > 0) {
            System.out.println("Счет успешно удален.");
        }

        //запрос на список всех транзакций у которых был указан удаленный счет
        preparedStatement = connection.prepareStatement("SELECT TRANSACTION_ID FROM account_activity WHERE ACCOUNT_ID = ?");

        preparedStatement.setInt(1, accountId);
        ResultSet result = preparedStatement.executeQuery(); // получаем ResultSet со списком идентификатора транзакций

        //пока ResultSet не пустой выполняем процедуру удаления транзакции и возврата денег на счет (по одной за проход, т.е. если список из 10 транзакций = 10 проходов)
        while (result.next()) {
            transactionId = result.getInt("TRANSACTION_ID");

            deleteAccountActivity(connection, transactionId);
        }
    }

    //вывод статистики по категориям
    private static void categoryStatistics(Connection connection) throws SQLException {
        // Запрос состоит из двух запросов:
        // (SELECT CATEGORY_ID, ROUND(SUM(TRANSACTION_AMOUNT), 2) as TRANSACTION_SUM FROM account_activity GROUP by CATEGORY_ID) q
        // выбирает данные из таблицы account_activity, группирует по CATEGORY_ID и для каждой группы подсчитывает сумму внутри группы ROUND(SUM(TRANSACTION_AMOUNT), 2)
        // далее результат этого запроса в виде "код (группа) - сумма" через INNER JOIN присоединяется к таблице categories, с целью выбрать из таблицы categories
        // для каждого кода (группы) соответсвующее текстовое поле описания (CATEGORY_INFO).
        //
        String sql = "SELECT categories.CATEGORY_INFO, q.TRANSACTION_SUM FROM categories " +
                "INNER JOIN (SELECT CATEGORY_ID, ROUND(SUM(TRANSACTION_AMOUNT), 2) as TRANSACTION_SUM FROM account_activity GROUP by CATEGORY_ID) q " +
                "ON categories.CATEGORY_ID = q.CATEGORY_ID " +
                "ORDER BY categories.CATEGORY_ID";

        String categoryInfo;
        double transactionSum;

        System.out.println("\nСтатистика по категориям.\n");
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(sql);

        System.out.println("\n╔══════════════════════╦════════════╗");
        System.out.format("║ %-20s ║ %-10s ║", "     Категория", "  Сумма");

        while (result.next()) {
            categoryInfo = result.getString("CATEGORY_INFO");
            transactionSum = result.getDouble("TRANSACTION_SUM");

            System.out.print("\n╠══════════════════════╬════════════╣");
            System.out.format("\n║ %-20s ║ %10.2f ║", categoryInfo, transactionSum);
        }
        System.out.println("\n╚══════════════════════╩════════════╝");
    }

}