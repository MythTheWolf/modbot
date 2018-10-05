package com.myththewolf.modbot.core.lib.logging.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.myththewolf.modbot.core.MyriadBotLoader;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class logbackJlineAppender extends AppenderBase<ILoggingEvent> {
    private String prefix;

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        LineReader reader = MyriadBotLoader.lineReader;
        if (reader == null) {
            return;
        }
        Terminal terminal = reader.getTerminal();
        synchronized (reader) {
            if (reader.isReading()) {
                reader.getTerminal().puts(InfoCmp.Capability.carriage_return);
                //{HH:mm:ss.SSS} [%thread]%level: %msg%
                DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
                String log = df.format(new Date(iLoggingEvent.getTimeStamp())) + " [" + iLoggingEvent.getThreadName() + "]" + iLoggingEvent.getLevel().toString() + ": " + iLoggingEvent.getFormattedMessage();
                reader.printAbove(log);
                reader.callWidget(LineReader.CLEAR);
                reader.callWidget(LineReader.REDRAW_LINE);
                reader.callWidget(LineReader.REDISPLAY);
            }
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
