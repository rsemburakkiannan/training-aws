```
import ch.qos.logback.core.encoder.EncoderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.Context;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PiiMaskingEncoder extends EncoderBase<ILoggingEvent> {

    private final PatternLayoutEncoder delegate = new PatternLayoutEncoder();

    // comma-separated Raw properties from logback-spring.xml
    private String regexList;
    private String unmaskedList;

    // compiled at start()
    private List<Pattern> patterns = new ArrayList<>();
    private List<Integer> unmaskedCounts = new ArrayList<>();

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        delegate.setContext(context);
    }

    public void setPattern(String pattern) {
        delegate.setPattern(pattern);
    }

    public void setCharset(String charsetName) {
        delegate.setCharset(Charset.forName(charsetName));
    }

    /** comma-separated regexes, e.g. "\\b\\d{9}\\b,\\b\\d{7,}\\b" */
    public void setRegexList(String regexList) {
        this.regexList = regexList;
    }

    /** comma-separated unmasked tail lengths, e.g. "3,4" */
    public void setUnmaskedList(String unmaskedList) {
        this.unmaskedList = unmaskedList;
    }

    @Override
    public void start() {
        // compile supplied patterns
        if (regexList != null && unmaskedList != null) {
            String[] regs = regexList.split(",");
            String[] counts = unmaskedList.split(",");
            for (int i = 0; i < regs.length && i < counts.length; i++) {
                patterns.add(Pattern.compile(regs[i].trim()));
                unmaskedCounts.add(Integer.parseInt(counts[i].trim()));
            }
        }
        // fallback default if none configured
        if (patterns.isEmpty()) {
            patterns.add(Pattern.compile("\\b\\d{9}\\b"));
            unmaskedCounts.add(3);
            patterns.add(Pattern.compile("\\b\\d{7,}\\b"));
            unmaskedCounts.add(3);
        }

        delegate.start();
        super.start();
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        String rendered = delegate.getLayout().doLayout(event);
        String masked = rendered;
        for (int i = 0; i < patterns.size(); i++) {
            masked = maskTail(masked, patterns.get(i), unmaskedCounts.get(i));
        }
        return masked.getBytes(delegate.getCharset());
    }

    @Override
    public void close() {
        delegate.stop();
    }

    private String maskTail(String input, Pattern p, int unmaskedCount) {
        Matcher m = p.matcher(input);
        StringBuffer sb = new StringBuffer(input.length());
        while (m.find()) {
            String match = m.group();
            int keep = Math.min(unmaskedCount, match.length());
            int stars = match.length() - keep;
            String suffix = match.substring(match.length() - keep);
            String repl = repeat('*', stars) + suffix;
            m.appendReplacement(sb, repl);
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private String repeat(char c, int times) {
        if (times <= 0) return "";
        char[] arr = new char[times];
        for (int i = 0; i < times; i++) arr[i] = c;
        return new String(arr);
    }
}
```

```
logging:
  pii:
    regexes: "\\b\\d{9}\\b,\\b\\d{7,}\\b,\\b\\d{3}-\\d{2}-\\d{4}\\b"
    unmaskedCounts: "3,4,4"
```

```
<configuration scan="true" scanPeriod="30 seconds">

  <springProperty name="piiRegexes"   source="logging.pii.regexes" />
  <springProperty name="piiUnmasks"   source="logging.pii.unmaskedCounts" />

  <appender name="FILE" class="ch.qos.logback.core.FileAppender">
    <file>logs/application.log</file>
    <append>true</append>

    <encoder class="com.sample.PiiMaskingEncoder">
      <pattern>%d{yyyy-MM-dd HH:mm:ss,SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
      <regexList>${piiRegexes}</regexList>
      <unmaskedList>${piiUnmasks}</unmaskedList>
    </encoder>
  </appender>

  <root level="INFO">
    <appender-ref ref="FILE" />
  </root>
</configuration>
```
