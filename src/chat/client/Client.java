package chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by kyojin on 30.06.17.
 */

//Запустить посредством класса main
public class Client {

    private BufferedReader in;  //переменная класса, считывающего из данные символьного потока ввода, буферизируя прочитанные символы
    private PrintWriter out;    //переменная класса, выводящего данные в консоль
    private Socket socket;     //переменная класса, реализующего клиентскй сокет, т.е. конечную точку для связи между двумя клиентами

    //Позволяет подключить клиент к серверу, используя IP и ник пользователем
    public Client() {
        Scanner scan = new Scanner(System.in);
        System.out.println("Введите IP для подключения к серверу");

        String ip = scan.nextLine();

        //Создать входящий и исходящий поток при написании IP и ника пользователя
        try {
            socket = new Socket(ip, 8080);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Введите свой ник:");
            out.println(scan.nextLine());

            Resender resend = new Resender();
            resend.start();

            //Передать данные клиента, при написании слова exit отключает клиента от сервера
            String str = "";
            while (!str.equals("exit")) {
                str = scan.nextLine();
                out.println(str);
            }
            resend.setStop();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }

    }

    //Закрывает все потоки
    private void close() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.err.println("Потоки не закрыты!");
        }
    }

    //Получает сообщения от сервера и выводить их в консоль до тех пор, пока поток не будет остановлен
    public class Resender extends Thread {
        private boolean stoped;

        private void setStop() {
            stoped = true;
        }

        @Override
        public void run() {
            while (!stoped) {
                try {
                    String str = in.readLine();
                    System.out.println(str);
                } catch (IOException e) {
                    System.err.println("Ошибка при получении сообщения!");
                    e.printStackTrace();
                }
            }
        }
    }
}
