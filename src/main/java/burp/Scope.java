package burp;

import org.json.JSONObject;

public class Scope {
    final Boolean enabled = true;
    String host;
    final String protocol = "any";
    String file;

    public Scope(String host, String file) {
        this.host = host;
        this.file = file;
    }

    public JSONObject toJSON() {
        JSONObject object = new JSONObject();

        object.put("enabled", this.enabled);
        object.put("host", this.host);
        object.put("protocol", this.protocol);
        object.put("file", this.file);

        return object;
    }
}
