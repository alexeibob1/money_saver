//вывести список ВСЕХ счетов
private static void outputAllAccountsInfo(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("SELECT ID, ACCOUNT_NUMBER, CURRENT_BALANCE FROM accounts ORDER by ID");

        System.out.println("\nТекущий баланс по счетам.");

        while (result.next()) {
            int accountId = result.getInt("ID");
            String accountNumber = result.getString("ACCOUNT_NUMBER");
            double currentBalance = result.getDouble("CURRENT_BALANCE");
            System.out.println(accountId + "   " + accountNumber + "   " + currentBalance);
        }
    }

    //вывести список ОДНОГО счета
    private static void outputAccountInfo(Connection connection, int accountID) throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement("SELECT ID, ACCOUNT_NUMBER, CURRENT_BALANCE FROM accounts WHERE ID = ?");
        preparedStatement.setInt(1, accountID);
        ResultSet result = preparedStatement.executeQuery(); //т.к. запрос параметризованный, подготавливаем его классом preparedStatement

        System.out.println("\nТекущий баланс по счету:");

        while (result.next()) {
            int accountId = result.getInt("ID");
            String accountNumber = result.getString("ACCOUNT_NUMBER");
            double currentBalance = result.getDouble("CURRENT_BALANCE");
            System.out.println(accountId + "   " + accountNumber + "   " + currentBalance);
        }
    }

    //изменение баланса счету
    private static void changeAccountBalance(Connection connection, int accountID, double transactionAmount) throws SQLException {

        //текст параметризированного sql-запроса для обновления значения поля CURRENT_BALANCE в таблице accounts
        String sql = "UPDATE accounts SET CURRENT_BALANCE = CURRENT_BALANCE + ? WHERE ID = ?";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setDouble(1, transactionAmount);
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

        String consoleInput = scanner.nextLine();
        double transactionAmount = checkTransactionAmount();

        System.out.print("\nУкажите дату в формате ГГГГ-ММ-ДД. Для ввода текущей даты оставьте строку пустую и нажмите Enter: ");
        String transactionDate = scanner.nextLine();
        transactionDate = checkDateFormat(transactionDate);

        //текст параметризированного запроса на вставку данных в 4-ре поля талицы account_activity
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

        //выполняем обновление баланса.
        // Math.abs(Х)*flag призвана исключить ситуацию когда пользователь пополняет баланс, но при этом вводит отрицательную сумму пополнения (и обратная ситуация).
        changeAccountBalance(connection, accountID, Math.abs(transactionAmount) * flag);

        outputAccountInfo(connection, accountID); //выводим список всех счетов
        outputAccountActivity(connection, accountID, 5); //выводим список 5-ти последних транзакций по обновленному счету

    }

    // вывести информацию о движении средств по указанному счету.
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

        switch(accountID) {
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

        while (result.next()) {
            int transactionId = result.getInt("TRANSACTION_ID");
            String accountNumber = result.getString("ACCOUNT_NUMBER");
            double transactionAmount = result.getDouble("TRANSACTION_AMOUNT");
            String categoryInfo = result.getString("CATEGORY_INFO");
            Date transactionDate = result.getDate("TRANSACTION_DATE");

            System.out.println(transactionId + "   " + accountNumber + "   " + transactionAmount + "   " + categoryInfo + "   " + transactionDate);
        }
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
        transactionAmount = Math.abs(transactionAmount) * flag;

        preparedStatement = connection.prepareStatement("DELETE FROM account_activity WHERE TRANSACTION_ID = ?");
        preparedStatement.setInt(1, transactionID);
        int qRow = preparedStatement.executeUpdate();
        if (qRow > 0) {
            System.out.println("Транзакция успешно удалена.");
        }

        changeAccountBalance(connection, accountId, transactionAmount); //передаем данные на удаление
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
    private static void categoryStatistics (Connection connection) throws SQLException {
        // Запрос состоит из двух запросов:
        // (SELECT CATEGORY_ID, ROUND(SUM(TRANSACTION_AMOUNT), 2) as TRANSACTION_SUM FROM account_activity GROUP by CATEGORY_ID) q
        // выбирает данные из таблицы account_activity, группирует по CATEGORY_ID и для каждой группы подсчитывает сумму внутри группы ROUND(SUM(TRANSACTION_AMOUNT), 2)
        // далее результат этого запроса в виде "код (группа) - сумма" через INNER JOIN присоединяется к таблице categories, с целью выбрать из таблицы categories
        // для каждого кода (группы) соответсвующее текстовое поле описания (CATEGORY_INFO).
        //
        String sql = "SELECT categories.CATEGORY_INFO, q.TRANSACTION_SUM FROM categories " +
            "INNER JOIN (SELECT CATEGORY_ID, ROUND(SUM(TRANSACTION_AMOUNT), 2) as TRANSACTION_SUM FROM account_activity GROUP by CATEGORY_ID) q " +
            "ON categories.CATEGORY_ID = q.CATEGORY_ID ORDER BY categories.CATEGORY_ID";

        String categoryInfo;
        double transactionSum;

        System.out.println("\nСтатистика по категориям.\n");
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(sql);

        while (result.next()) {
            categoryInfo = result.getString("CATEGORY_INFO");
            transactionSum = result.getDouble("TRANSACTION_SUM");

            System.out.println(categoryInfo + "   " + transactionSum);
        }
    }




    //close

}
