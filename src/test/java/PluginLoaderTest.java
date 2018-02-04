import com.myththewolf.modbot.core.invocation.impl.ImplPluginLoader;
import com.myththewolf.modbot.core.invocation.interfaces.PluginLoader;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

public class PluginLoaderTest {
    @Test
    public void testJarLoder() throws ClassNotFoundException {
        PluginLoader pl = new ImplPluginLoader();
        System.out.println("Loading sample plugin from:"+ System.getProperty("user.dir"));
        pl.loadJarFile(new File("sample.jar"));
    }
}
