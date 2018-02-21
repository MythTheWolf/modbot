import com.myththewolf.modbot.core.ModBotCoreLoader;
import org.junit.Test;

public class PluginLoaderTest {
    @Test
    public void testJarLoader() {
        String[] args = {"--nobot"};
        ModBotCoreLoader.main(args);

    }
}
