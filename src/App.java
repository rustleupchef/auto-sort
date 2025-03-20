import java.io.FileReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class App {
    public static void main(String[] args) throws Exception {
        JSONObject config = (JSONObject) new JSONParser().parse(new FileReader("config.json"));
        
        String[] from = jsonArraytoStringArray(config.get("from"));
        String[] to = jsonArraytoStringArray(config.get("to"));
        String[] targets = jsonArraytoStringArray(config.get("targets"));
    }

    private static String[] jsonArraytoStringArray(Object objectArray) {
        JSONArray jsonArray = (JSONArray) objectArray;
        String[] stringArray = new String[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            stringArray[i] = (String) jsonArray.get(i);
        }
        return stringArray;
    }
}
