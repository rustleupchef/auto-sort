import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.JOptionPane;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class App {
    private static String model;
    private static HashSet<String> types = new HashSet<String>();
    private static boolean isException;
    public static void main(String[] args) throws Exception {

        if (args.length > 0 && args[0].equals("clear")) {
            File logFile = new File("log.txt");
            if (logFile.exists()) {
                logFile.delete();
            }
            return;
        }

        JSONObject config = (JSONObject) new JSONParser().parse(new FileReader("config.json"));

        String[] from = jsonArraytoStringArray(config.get("from"));
        String[] to = jsonArraytoStringArray(config.get("to"));
        String[] typesArray = jsonArraytoStringArray(config.get("types"));
        isException = (boolean) config.get("isException");
        model = (String) config.get("model");

        from = filterPaths(from);
        to = filterPaths(to);
        typesArray = filter(false, typesArray);

        for (String type : typesArray)
            types.add(type);

        if (to.length == 0) {
            JOptionPane.showMessageDialog(
                null, 
                "Invalid Config.json. To is not filled out", 
                "Error", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (from.length == 0) {
            JOptionPane.showMessageDialog(
                null, 
                "Invalid Config.json. From is not filled out", 
                "Error", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (model.equals("")) {
            JOptionPane.showMessageDialog(
                null,
                "Invalid Config.json. Model is not filled out",
                "Error",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        while (true) {
            for (String fromPath : from) {
                File[] files = new File(fromPath).listFiles();
                for (File file : files) {

                    String[] nameSplit = file.getName().split(".");
                    if (types.contains(nameSplit[nameSplit.length - 1]) && isException)
                        continue;
                    
                    if (types.contains(nameSplit[nameSplit.length - 1]) && isException)
                        continue;
                    
                    if (file.isDirectory())
                        continue;
                    
                    int goTo = 0;
                    if (to.length > 1) {
                        goTo = ollama(file.getName(), to);
                        if (goTo < 0) continue;
                        file.renameTo(new File(Paths.get(to[goTo]).resolve(Paths.get(file.getName())).toAbsolutePath().toString()));
                        log("File: " + file.getName() + " moved to: " + to[goTo]);
                    }
                    file.renameTo(new File(Paths.get(to[0]).resolve(Paths.get(file.getName())).toAbsolutePath().toString()));
                    log("File: " + file.getName() + " moved to: " + to[0]);
                }
            }
        }
    }

    private static String[] jsonArraytoStringArray(Object objectArray) {
        JSONArray jsonArray = (JSONArray) objectArray;
        String[] stringArray = new String[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            stringArray[i] = (String) jsonArray.get(i);
        }
        return stringArray;
    }

    private static String[] filterPaths(String[] paths) {
        return filter(true, paths);
    }

    private static String[] filter(boolean isPath, String[] array) {
        if (isPath) {
            List<String> paths = new ArrayList<String>();
            HashSet<String> unique = new HashSet<String>();
            for (String path : array) {
                if (!new File(path).exists() || !new File(path).isDirectory())
                    continue;
                if (unique.add(path)) {
                    paths.add(path);
                }
            }
            return paths.toArray(new String[0]);
        }

        for (int i = 0; i < array.length; i++) {
            String trimed = array[i];
            if (trimed.length() == 0)
                continue;
            if (trimed.charAt(0) == '.')
                trimed = trimed.substring(1);
            array[i] = trimed;
        }
        return array;
    }

    private static int ollama(String filename, String[] targetDirs) {
        String systemPrompt = "";

        for (int i = 0; i < targetDirs.length; i++) {
            systemPrompt += "[" + i + "] " + targetDirs[i] + "\n";
        }

        systemPrompt += 
            "You will be prompted to choose a location to place the file in.\n" +
            "Please enter the number of the location you want to place the file in.\n" +
            "Enter just the number";

        JSONObject payload = new JSONObject();

        payload.put("model", model);
        payload.put("system", systemPrompt);
        payload.put("prompt", "Which location should I place the file in? The file name is:" + filename);

        JSONObject options = new JSONObject();
        options.put("num_predict", 1);
        options.put("temperature", 0.0);

        payload.put("options", options);
        payload.put("stream", false);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:11434/api/generate"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload.toJSONString()))
            .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200)
                return -1;
            
            return 0;
        } catch (Exception e) {
            return -1;
        }
    }

    private static void log(String message) throws IOException {
        FileWriter writer = new FileWriter(new File("log.txt"), true);
        writer.write(message + "\n");
        writer.close();
    }
}