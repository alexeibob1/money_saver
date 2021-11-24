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
        String databaseURL = "jdbc:ucanaccess://D:/Download//DatabaseTemplate.accdb";
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

    /**
     * this method calculates new balance amount if user input right else ask new input
     * @return transactionAmount this is the sum of the balance change.
     * */
    static double checkTransactionAmount(){
        double transactionAmount = 0d;
        boolean isCorrect = false;
        while (!isCorrect) {
            String consoleInput = scanner.nextLine();
            isCorrect = checkDoubleOrNot(consoleInput);
            if (isCorrect) {
                transactionAmount = Double.parseDouble(consoleInput);
            } else {
                System.err.print("not correct sum input! follow format (pyb.cop): ");
            }
        }
        return Math.round(transactionAmount * 100.0) / 100.0;
    }


    /**
     * This method check our date and it date-format and if this value incorrectly asks for a new one.
     * @param consoleInput checking value
     * @return converting value to year-month-day
     * */
    static String checkDateFormat(String consoleInput) {
        boolean flag = true;
        boolean isFormatCorrect = true;
        int numTmp;
        int partLength;

        //если пользователь не вводил дату, а нажал ввод, то присвоить текущую дату
        if (consoleInput.isEmpty()) { //проверяем что строка пустая. при этом символ \n (enter) не учитывается
            LocalDate systemDate = LocalDate.now(); //получаем текущую дату
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd"); //подготавливаем шаблон для форматирования даты в строку
            consoleInput = dtf.format(systemDate); //применяем шаблон к дате при конвертациив строку
        }

        //проверка строки на формат ГГГГ-ММ-ДД
        while (flag) {
            String[] splitDate = consoleInput.split("-"); //разделяем строку на части, где разделитель символ тире

            for (int i=0; i<splitDate.length; i++) {
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
                System.err.print("not correct date! follow format (year-month-day): ");
                consoleInput = scanner.nextLine();
                flag = true;
                isFormatCorrect = true;
            } else {
                flag = false;
            }
        }
        return  consoleInput;
    }

    /**
     * this method checks length of name for new account or category and if this name using in DB asks for a new name.
     * @param connection it is the link between project and DB
     * @param consoleInput it is checking value
     * @param menuType it is work space (account or category)
     * @throws SQLException in case of errors in working with DB.
     * @return unique value for the new account or category.
     * */
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
            if (consoleInput.isEmpty() || consoleInput.length() > 20) { //проверяем на пустую строку и на ограничение в 20 символов
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
        return  consoleInput;
    }

    /**
     * This program checks way to work with project
     * @param connection it is the link between project and DB
     * @param userChoice it is checking value
     * @param menuType it if our work space (account, category and other)
     * @throws SQLException in case of errors in working with DB
     * @return true if user input is correct, else return false
     * */
    private static boolean checkNumberInRange(Connection connection, int userChoice, String menuType) throws SQLException {

        HashSet<Integer> menuSet = new HashSet<>(); //объявляем коллекцию типа HashSet<Integer>

        String sql = "SELECT ID FROM menu_items WHERE ID = 0" ;
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

    /**
     * This method create a new account
     * @param connection it is the link between project and DB
     * @throws SQLException in case of errors in working with DB.
     * */
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

    /**
     * This method create a new category
     * @param connection it is the link between project and DB
     * @throws SQLException in case of errors in working with DB.
     * */
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

    /**
     * this method displays a list of all categories without "increase account"
     * @param connection it is the link between project and DB
     * @throws SQLException in case of errors in working with DB.
     * */
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

    /**
     * this method show a list of all accounts
     * @param connection it is the link between project and DB
     * @throws SQLException in case of errors in working with DB.
     * */
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

    /**
     * this method displays a list of account with right accountID
     * @param accountID it is index of right account
     * @param connection it is link between project and DB
     * @throws SQLException in case of errors in working with DB.
     * */
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

    /**
     * this method changes balance with right accountID
     * @param accountID it is index of right account
     * @param connection it is the link between project and DB
     * @param flag this value shows what we want increase balance or decrease.
     * @param transactionAmount this value is module of the which we change the balance.
     * @throws SQLException in case of errors in working with DB.
     * */
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
    /**
     * this method
     * @param flag  if flag value equal 1 it mean make bigger, if equal 0 make smaller
     * @param connection it is the link between project and DB
     * @throws SQLException in case of errors in working with DB.
     * */
    private static void addActivity2Account(Connection connection, int flag) throws SQLException {
        double transactionAmount = 0d;

        boolean isCorrect = false;
        outputAllAccountsInfo(connection); //показываем информацию по всем счетам

        String subMenuMsg = "\nВыберите счет, указав порядковый номер счета из списка: ";
        int accountID = checkMenuUserChoice(connection, "accounts", subMenuMsg);
        int categoryID = 0;

        switch(flag) {
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
        String transactionDate = scanner.nextLine();
        transactionDate = checkDateFormat(transactionDate);

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
    /**
     * This method show operations history on account with right index.
     * message include 3 parts. At first prepare middle message part
     * Main part of message (were located SELECT) there are 2 forms.
     * 1.SELECT TOP 5 when need get the last 5 operation
     * 2.Select when need all aperation
     * Where also have 2 forms
     * 1. WHERE accounts.ID = ? when need get transactions on right account
     * 2. WHERE accounts.ID > ? when need get transactions on all accounts
     * Last part of massege (sorting) not changeable (from high to down sorting)
     * @param flag this value show
     * @param connection it is the link between project and DB
     * @param accountID it is right index
     * @throws SQLException in case of errors in working with DB.
     * */
    private static void outputAccountActivity(Connection connection, int accountID, int flag) throws SQLException {
        String sql = "TRANSACTION_ID, ACCOUNT_NUMBER, TRANSACTION_AMOUNT, CATEGORY_INFO, TRANSACTION_DATE " +
            "from (account_activity INNER JOIN accounts ON account_activity.ACCOUNT_ID = accounts.ID) " +
            "INNER JOIN categories ON account_activity.CATEGORY_ID = categories.CATEGORY_ID ";
        String sqlWhere = " WHERE accounts.ID = ? "; // первый вариант условия WHERE
        String sqlOrder = " ORDER by TRANSACTION_ID DESC"; // сортировка

        switch(accountID) {
            case -1:
                outputAllAccountsInfo(connection);
                String subMenuMsg = "\nДля получения истории операций по счету введите порядковый номер счета: ";
                accountID = checkMenuUserChoice(connection, "accounts", subMenuMsg);
                break;
            case 0:
                System.out.println("\noperation history on all accounts ");
                sqlWhere = " WHERE accounts.ID > ? ";
                break;
            default:
                System.out.println("\nhistory the last 5 operation on account");
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
        System.out.format("║ %-5s ║ %-20s ║ %-10s ║ %-20s ║ %-11s ║", "  №", "        Счет", "  Сумма","     Категория", "    Дата");

        while (result.next()) {
            int transactionId = result.getInt("TRANSACTION_ID");
            String accountNumber = result.getString("ACCOUNT_NUMBER");
            double transactionAmount = result.getDouble("TRANSACTION_AMOUNT");
            String categoryInfo = result.getString("CATEGORY_INFO");
            Date transactionDate = result.getDate("TRANSACTION_DATE");
            System.out.print("\n╠═══════╬══════════════════════╬════════════╬══════════════════════╬═════════════╣");
            System.out.format("\n║ %-5d ║ %-20s ║ %10.2f ║ %-20s ║ %-11s ║", transactionId, accountNumber, transactionAmount, categoryInfo, transactionDate );
        }
        System.out.println("\n╚═══════╩══════════════════════╩════════════╩══════════════════════╩═════════════╝");
    }

    // удаление транзакции
    // если transactionID = -1, то запросить пользователя номер транзакции для удаления
    /**
     * This method delete operation with right index if index is -1 ask new index
     * @param transactionID  it is start index
     * @param connection it is the link between project and DB
     * @throws SQLException in case of errors in working with DB
     * */
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

        if (categoryId == 0) {
            flag = -1;
        } else {
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

    /**
     * this method delete category, in this method user input index of category
     * @param connection it is the link between project and DB
     * @throws SQLException in case of errors in working with DB
     * */
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
    /**
     * this method delete account, in this method user input index of account
     * @param connection it is the link between project and DB
     * @throws SQLException in case of errors in working with DB
     * */
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

    /**
     * show category statistics
     * @param connection it is the link between project and DB
     * @throws SQLException in case of errors in working with DB
     * */
    private static void categoryStatistics (Connection connection) throws SQLException {
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




    //close
}
