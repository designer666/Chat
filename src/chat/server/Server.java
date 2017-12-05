package chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by kyojin on 30.06.17.
 */

//Запустить посредством класса main
public class Server {

    private List<Connection> connections = Collections.synchronizedList(new ArrayList<Connection>());   //Переменна списка соединений
    private ServerSocket server;    //Переменная сокета сервера

    //Создать сервер
    public Server() {

        //Создание сокета сервера с определенным портом
        try {
            server = new ServerSocket(8080);

            while (true) {

                //Указывает серверу ожидать подключения
                Socket socket = server.accept();

                //создаёт объект Connection, инициализированный этим сокетом и добавляется в массив
                Connection con = new Connection(socket);
                connections.add(con);
                con.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeAll();
        }
    }

    //Закрыть все потоки когда отключаются все клиенты
    private void closeAll() {
        try {
            server.close();

            //Перебирает синхронизированно перебирает соединения и при отсутствии данных закрывает потоки
            synchronized (connections) {
                Iterator<Connection> iter = connections.iterator();
                while (iter.hasNext()) {
                    ((Connection) iter.next()).close();
                }
            }
        } catch (IOException e) {
            System.err.println("Потоки не закрыты!");
        }
    }

    //Класс принимает от пользователя сообщения и рассылающий их остальным клиентам:
    private class Connection extends Thread {
        private BufferedReader in;
        private PrintWriter out;
        private Socket socket;
        private String name = "";

        //Конструктор преобразовывает потоки, связанные с сокетом
        public Connection(Socket socket) {
            this.socket = socket;

            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
                close();
            }

        }

        //Запускается в отдельном потоке и выполняется параллельно с остальной частью программы
        @Override
        public void run() {
            //Считатьт имя пользователя, после чего его сообщения рассылаются всем клиентам чата
            try {
                name = in.readLine();
                synchronized (connections) {
                    Iterator<Connection> iter = connections.iterator();
                    while (iter.hasNext()) {
                        ((Connection) iter.next()).out.println(name + " подключился");
                    }
                }

                //Отключить пользователя когда приходит сообщение exit
                String str = "";
                while (true) {
                    str = in.readLine();
                    if (str.equals("exit")) {
                        break;
                    }
                    synchronized (connections) {
                        Iterator<Connection> iter = connections.iterator();
                        while (iter.hasNext()) {
                            ((Connection) iter.next()).out.println(name + ": " + str);
                        }
                    }
                }
                synchronized (connections) {
                    Iterator<Connection> iter = connections.iterator();
                    while (iter.hasNext()) {
                        ((Connection) iter.next()).out.println(name + " отключился");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close();
            }
        }

        //Закрыть все связанные с пользователем потоки при его отключении
        public void close() {
            try {
                in.close();
                out.close();
                socket.close();

                connections.remove(this);
                if (connections.size() == 0) {
                    Server.this.closeAll();
                    System.exit(0);
                }
            } catch (IOException e) {
                System.out.println("Потоки не закрыты!");
            }
        }
    }
}
