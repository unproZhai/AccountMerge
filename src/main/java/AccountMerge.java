import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class AccountMerge {
    public static void mergeAccount(String filename) {
        JSONArray applications = readInput(filename);
        JSONArray accountResult = mergingAccount(applications);
        System.out.println(accountResult);

    }

    // Read Input for applications
    public static JSONArray readInput(String filename) {
        try {
            JSONParser parser = new JSONParser();
            //Use JSONObject for simple JSON and JSONArray for array of JSON.
            JSONArray applications = (JSONArray) parser.parse(new FileReader(filename));//path to the JSON file.
            return applications;

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Start merging
    public static JSONArray mergingAccount(JSONArray applications) {
        Map<String, String> owner = new HashMap<>();
        Map<String, List<Long>> apps = new HashMap<>();
        Map<String, String> parents = new HashMap<>();
        Map<String, TreeSet<String>> unions = new HashMap<>();

        for (Object app : applications) {
            JSONArray emails = (JSONArray) ((JSONObject) app).get("emails");
            String name = (String) ((JSONObject) app).get("name");
            Long appId = (Long) ((JSONObject) app).get("application");

            for (int i = 0; i < emails.size(); i++) {
                parents.put(emails.get(i).toString(), emails.get(i).toString());
                owner.put(emails.get(i).toString(), name);
                List<Long> old;
                if(apps.containsKey(emails.get(i).toString())) {
                    old = apps.get(emails.get(i).toString());
                    old.add(old.size(),appId);
                } else {
                    old = new ArrayList();
                    old.add(0,appId);
                }
                apps.put(emails.get(i).toString(),old);
            }
        }

        for (Object app : applications) {
            JSONArray emails = (JSONArray) ((JSONObject) app).get("emails");

            String p = find(emails.get(0).toString(), parents);
            for (int i = 1; i < emails.size(); i++)
                parents.put(find(emails.get(i).toString(), parents), p);
        }

        for(Object app : applications) {
            JSONArray emails = (JSONArray) ((JSONObject) app).get("emails");

            String p = find(emails.get(0).toString(), parents);
            if (!unions.containsKey(p)) unions.put(p, new TreeSet<>());
            for (int i = 0; i < emails.size(); i++)
                unions.get(p).add(emails.get(i).toString());
        }

        JSONArray result = new JSONArray();
        for (String p : unions.keySet()) {
            List<String> emails = new ArrayList(unions.get(p));
            JSONObject newJSON = new JSONObject();
            List<Long> appTotal = new ArrayList();
            for(String email: emails) {
                appTotal = Stream.concat(appTotal.stream(), apps.get(email).stream())
                        .collect(Collectors.toList());
                Set<String> hashSet = new LinkedHashSet(appTotal);
                appTotal = new ArrayList(hashSet);
            }
            newJSON.put("applications", appTotal);
            newJSON.put("emails", emails);
            newJSON.put("name", owner.get(p));

            result.add(newJSON);
        }
        return result;
    }

    private static String find(String s, Map<String, String> p) {
        return p.get(s) == s ? s : find(p.get(s), p);
    }

    public static void main(String[] args) {
        String testFilePath = "src/main/resources/accounts.json";
        mergeAccount(testFilePath);
    }
}

