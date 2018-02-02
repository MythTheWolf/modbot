import com.myththewolf.modbot.core.invocation.impl.ImplPluginLoader;
import com.myththewolf.modbot.core.invocation.interfaces.PluginLoader;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

public class PluginLoaderTest {
    @Test
    public void testJarLoder() throws ClassNotFoundException {
        PluginLoader pl = new ImplPluginLoader();
      //  pl.loadJarFile(new File("sampleplugin.jar"));
      //  Class<?> clazz = Class.forName("com.myththewolf.sampleplugin.SamplePluginClass");
     //   Arrays.stream(clazz.getMethods()).forEach(method -> {
     //       System.out.println(method.getName());
     //   });
    }
}
