import com.myththewolf.modbot.core.extensions.SystemPlugin;
import com.myththewolf.modbot.core.lib.invocation.impl.ImplPluginLoader;
import com.myththewolf.modbot.core.lib.invocation.interfaces.PluginManager;
import org.json.JSONObject;
import org.junit.Test;

public class SystemPluginTest {
    @Test
    public void runSystemTest() {
        PluginManager pl = new ImplPluginLoader();
        JSONObject runconfig = new JSONObject();
        runconfig.put("mainClass", "com.myththewolf.modbot.core.extensions.SystemPlugin");
        runconfig.put("pluginName", "SystemPlugin");
        runconfig.put("pluginVersion", "1.0.0");
        runconfig.put("pluginDescription", "A collection of system commands packages inside a immutable system plugin");
        ((ImplPluginLoader) pl).addRawPlugin(runconfig, new SystemPlugin());
    }
}
