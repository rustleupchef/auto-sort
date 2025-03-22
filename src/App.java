import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class App {
    public static void main(String[] args) throws Exception {
        JSONObject config = (JSONObject) new JSONParser().parse(new FileReader("config.json"));

        String[] from = jsonArraytoStringArray(config.get("from"));
        String[] to = jsonArraytoStringArray(config.get("to"));
        String[] exceptions = jsonArraytoStringArray(config.get("exceptions"));

        from = filterPaths(from);
        to = filterPaths(to);
        exceptions = filter(false, exceptions);
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
}