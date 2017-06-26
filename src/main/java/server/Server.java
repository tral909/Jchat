package server;

import util.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Egorov Roman. Created on 25.06.2017
 */

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        ConsoleHelper.writeMessage("Введите номер порта: ");
        try (ServerSocket serverSocket = new ServerSocket(ConsoleHelper.readInt())) {
            ConsoleHelper.writeMessage("Сервер запущен");
            while (true) {
                new Handler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            ConsoleHelper.writeMessage(e.getMessage());
        }
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            String name = null;
            ConsoleHelper.writeMessage("Было установлено соединение с удаленным адресом " + socket.getRemoteSocketAddress());
            try (Connection connection = new Connection(socket)) {
                name = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, name));
                sendListOfUsers(connection, name);
                serverMainLoop(connection,name);
            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Ошибка при обмене данными с удаленным адресом");
            } finally {
                if (name != null) {
                    connectionMap.remove(name);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, name));
                    ConsoleHelper.writeMessage("Соединение с удаленным адресом закрыто");
                }
            }
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            Message respMes = null;
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                respMes = connection.receive();
                if (respMes.getType() == MessageType.USER_NAME &&
                        !respMes.getData().isEmpty() &&
                        !connectionMap.containsKey(respMes.getData())) {
                    connectionMap.put(respMes.getData(), connection);
                    connection.send(new Message(MessageType.NAME_ACCEPTED));
                    return respMes.getData();
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            Message respMes = null;
            while (true) {
                respMes = connection.receive();
                if (respMes.getType() == MessageType.TEXT) {
                    sendBroadcastMessage(new Message(MessageType.TEXT, String.format("%s: %s", userName, respMes.getData())));
                } else {
                    ConsoleHelper.writeMessage("Ошибка типа сообщения");
                }
            }
        }

        private void sendListOfUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {
                if (!entry.getKey().equals(userName)) {
                    connection.send(new Message(MessageType.USER_ADDED, entry.getKey()));
                }
            }
        }
    }

    public static void sendBroadcastMessage(Message message) {
        Connection connection = null;
        try {
            for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {
                connection = entry.getValue();
                connection.send(message);
            }
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Ошибка отправления сообщения");
        }
    }
}
