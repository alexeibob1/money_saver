package com.company;

import java.sql.*;
import java.util.HashSet;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
* ����������� � �� Microsoft Access (accdb) ��������� ������� UCanAccess JDBC
* ��������� �������� UCanAccess � Intellij IDEA
* https://www.youtube.com/watch?v=xM1KNbRkF3A
*/
public class Main {
    public static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        //����������� � �� MS ACCESS. ���������� ������� UCanAccess. ������ ������ ���� � ����� �� (.accdb)
        String databaseURL = "jdbc:ucanaccess://d://Database_Accounting.accdb";
        int userChoice = -1;
        boolean closeProg = false;
        String menuMsg =  "\n�====================================�" +
                          "\n�                ����                �" +
                          "\n�===T================================�" +
                          "\n� 1 � ������� ����                   �" +
                          "\n� 2 � ������� ���������              �" +
                          "\n� 3 � ��������� ����                 �" +
                          "\n� 4 � ������ ������ ������� �� ����� �" +
                          "\n� 5 � ������� ��������               �" +
                          "\n� 6 � ������� ����                   �" +
                          "\n� 7 � ������� ���������              �" +
                          "\n� 8 � ������� ��������               �" +
                          "\n� 9 � ���������� �� ����������       �" +
                          "\n�===+================================�" +
                          "\n� 0 � �����                          �" +
                          "\nL===�================================-" +
                          "\n�������� ����� ����: ";


        do {
            try {
                Connection connection = DriverManager.getConnection(databaseURL);
                userChoice = checkUserChoice(connection, "mainmenu", menuMsg);

                switch (userChoice) {
                    //������� ����
                    case 1 -> addAccount(connection);
                    //������� ���������
                    case 2 -> addCategory(connection);
                    //��������� ����
                    case 3 -> addActivity2Account(connection, 1);
                    //������ ������ ������� �� �����
                    case 4 -> addActivity2Account(connection, -1);
                    //������� ��������
                    case 5 -> outputAccountActivity(connection, -1, 5);
                    //������� ����
                    case 6 -> {
                        deleteAccount(connection);
                        outputAllAccountsInfo(connection);
                    }
                    //������� ���������
                    case 7 -> {
                        deleteCategory(connection);
                        outputAllAccountsInfo(connection);
                    }
                    //������� ��������
                    case 8 -> {
                        deleteAccountActivity(connection, -1);
                        outputAllAccountsInfo(connection);
                    }
                    //���������� �� ����������
                    case 9 -> categoryStatistics(connection);
                    //����� �� ���������
                    case 0 -> closeProg = true;
                    //���� ������������ ������ ������������ �����
                    default -> {
                        System.err.println("������������ ����! ������� ����� �� ������!");
                        closeProg = false;
                    }
                }

             //   connection.close();
            }
            catch (SQLException e) {
                //������� ��������� ������ � �������
                System.err.println("��� ������ � ����� ������ ��������� ������! ��������� ���������� � ���� � ��������� ���� ������");
                System.err.println("Error Message: " + e.getMessage());
                System.err.println("Error Code: " + e.getErrorCode());
                System.err.println("SQLState: " + e.getSQLState());
                //System.err.println("Trace: ");
                //e.printStackTrace();
                userChoice = 0; //�� �������� "backmenu"
                closeProg = true; //����� �� ���������
            }

            if (userChoice !=0 ) {
                closeProg = back2menu();
            }

        } while (!closeProg);

        System.out.print("\n���������� ���������... ");

    }