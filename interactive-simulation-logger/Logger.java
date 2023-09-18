import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Logger {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/log", new LogHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started");
    }
}

class LogHandler implements HttpHandler {

    public static final String LOG_FILE_NAME = "log.csv";
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String message = exchange.getRequestURI().getQuery();
        Map<String, String> data = parseQueryString(message);
        log(data.get("room"), data.get("caseid"));

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, message.length());
        exchange.getResponseBody().write(message.getBytes());
        exchange.close();
    }

    public void log(String room, String caseId) {
        String contentToAppend = caseId + ";" + room + ";" + LocalDateTime.now().format(dateFormat) +"\n";
        try {
            Path path = Paths.get(LOG_FILE_NAME);
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
            Files.write(
                    path,
                    contentToAppend.getBytes(),
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Map<String, String> parseQueryString(String query) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }
}
