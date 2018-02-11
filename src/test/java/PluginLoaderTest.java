import com.myththewolf.modbot.core.ModBotCoreLoader;
import com.myththewolf.modbot.core.lib.invocation.impl.ImplPluginLoader;
import com.myththewolf.modbot.core.lib.invocation.interfaces.PluginManager;
import org.junit.Test;

import java.io.File;

public class PluginLoaderTest {
    @Test
    public void testJarLoader() {
        String[] args = {"--nobot"};
        ModBotCoreLoader.main(args);

    }
}
