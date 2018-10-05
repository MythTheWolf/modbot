package com.myththewolf.modbot.core.systemPlugin.commands;

import com.myththewolf.modbot.core.lib.logging.Loggable;
import com.myththewolf.modbot.core.systemPlugin.SystemCommand;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class downloadTest implements SystemCommand, Loggable {
    private static int getFileSize(URL url) {
        URLConnection conn = null;
        try {
            conn = url.openConnection();
            if (conn instanceof HttpURLConnection) {
                ((HttpURLConnection) conn).setRequestMethod("HEAD");
            }
            conn.getInputStream();
            return conn.getContentLength();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn instanceof HttpURLConnection) {
                ((HttpURLConnection) conn).disconnect();
            }
        }
    }

    @Override
    public void onCommand(MessageAuthor author, Message message) {
        try {
            URL url = new URL(message.getContent().split(" ")[1]);
            getLogger().info(getFileSize(url) + "");
            BufferedInputStream bis = new BufferedInputStream(url.openStream());
            FileOutputStream fis = new FileOutputStream(new File("out"));
            ProgressBarBuilder pbb = new ProgressBarBuilder();
            ProgressBar pb = new ProgressBar("Downloading", getFileSize(url));
            try {
                ProgressBar.wrap(bis, pbb);
                byte[] buffer = new byte[1024];
                int count = 0;
                while ((count = bis.read(buffer, 0, 1024)) != -1) {
                    fis.write(buffer, 0, count);
                    pb.stepBy(count);
                }
                fis.close();
                bis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            pb.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
