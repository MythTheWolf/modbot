import com.myththewolf.modbot.core.lib.invocation.impl.ImplPluginLoader;
import com.myththewolf.modbot.core.lib.invocation.interfaces.PluginManager;
import org.junit.Test;

import java.io.File;

public class PluginLoaderTest {
    @Test
    public void testJarLoader() throws ClassNotFoundException {
        PluginManager pl = new ImplPluginLoader();
        System.out.println("Loading sample plugin from:"+ System.getProperty("user.dir"));
        pl.loadJarFile(new File("sample.jar"));
    }
}
