package client;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Egorov Roman. Created on 25.06.2017
 */


public class BotClient extends Client {

    public static void main(String[] args) {
        new BotClient().run();
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        return "date_bot_" + (int) (Math.random() * 100);
    }

    public class BotSocketThread extends SocketThread {
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            super.processIncomingMessage(message);
            if (message.contains(":")) {
                String name = message.substring(0, message.indexOf(":"));
                String data = message.substring(message.indexOf(":") + 2);
                String format = null;
                switch (data) {
                    case "дата":
                        format = "d.MM.YYYY";
                        break;
                    case "день":
                        format = "d";
                        break;
                    case "месяц":
                        format = "MMMM";
                        break;
                    case "год":
                        format = "YYYY";
                        break;
                    case "время":
                        format = "H:mm:ss";
                        break;
                    case "час":
                        format = "H";
                        break;
                    case "минуты":
                        format = "m";
                        break;
                    case "секунды":
                        format = "s";
                        break;
                    default:
                        return;
                }
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                Calendar calendar = Calendar.getInstance();
                Date currentDate = calendar.getTime();
                sendTextMessage(String.format("Информация для %s: %s", name, sdf.format(currentDate)));
            }
        }
    }
}
