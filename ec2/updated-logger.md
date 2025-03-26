# Log Masking Solution Project Structure

## Project Structure
```
log-masking-solution/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── logmasking/
│   │   │           ├── MaskingManager.java
│   │   │           ├── MaskingStrategy.java
│   │   │           ├── strategies/
│   │   │           │   ├── RegexMaskingStrategy.java
│   │   │           │   ├── JsonMaskingStrategy.java
│   │   │           │   └── XmlMaskingStrategy.java
│   │   │           └── filter/
│   │   │               ├── LogbackMaskingFilter.java
│   │   │               └── Log4jMaskingFilter.java
│   │   │
│   │   └── resources/
│   │       ├── log-masking.properties
│   │       └── logback.xml
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── logmasking/
│                   └── MaskingTest.java
│
├── pom.xml
└── README.md
```

## 1. Maven POM File (pom.xml)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.logmasking</groupId>
    <artifactId>log-masking-solution</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        
        <slf4j.version>1.7.32</slf4j.version>
        <logback.version>1.2.6</logback.version>
        <log4j.version>2.14.1</log4j.version>
        <junit.version>5.8.1</junit.version>
    </properties>

    <dependencies>
        <!-- Logging Dependencies -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>${slf4j.version}</version>
        </dependency>
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>${logback.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-core</artifactId>
            <version>${log4j.version}</version>
        </dependency>

        <!-- Configuration -->
        <dependency>
            <groupId>commons-configuration</groupId>
            <artifactId>commons-configuration</artifactId>
            <version>1.10</version>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-engine</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>11</source>
                    <target>11</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.0.0-M5</version>
            </plugin>
        </plugins>
    </build>
</project>
```

## 2. Masking Configuration (log-masking.properties)
```properties
# Regex Patterns for Masking
mask.patterns.credit-card=\\b(?:\\d{4}[-]?){3}\\d{4}\\b
mask.patterns.email=\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b
mask.patterns.phone=\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b
mask.patterns.ssn=\\b\\d{3}-\\d{2}-\\d{4}\\b

# Masking Replacement Patterns
mask.replacement.credit-card=XXXX-XXXX-XXXX-{4}
mask.replacement.email=****@{domain}
mask.replacement.phone=XXX-XXX-{4}
mask.replacement.ssn=XXX-XX-{4}
```

## 3. Masking Strategy Interface
```java
package com.logmasking;

public interface MaskingStrategy {
    String mask(String input);
}
```

## 4. Regex Masking Strategy
```java
package com.logmasking.strategies;

import com.logmasking.MaskingStrategy;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexMaskingStrategy implements MaskingStrategy {
    private Pattern pattern;
    private String replacement;
    private boolean preserveLastN;

    public RegexMaskingStrategy(String patternStr, String replacementTemplate, boolean preserveLastN) {
        this.pattern = Pattern.compile(patternStr);
        this.replacement = replacementTemplate;
        this.preserveLastN = preserveLastN;
    }

    @Override
    public String mask(String input) {
        if (input == null) return null;
        
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String match = matcher.group();
            String maskedValue = maskValue(match);
            matcher.appendReplacement(result, maskedValue);
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    private String maskValue(String value) {
        if (!preserveLastN) {
            return replacement;
        }
        
        // Preserve last 4 characters
        if (value.length() > 4) {
            String lastFour = value.substring(value.length() - 4);
            String maskedPart = replacement.replace("{4}", lastFour);
            
            // Handle domain preservation for emails
            if (value.contains("@")) {
                String domain = value.substring(value.lastIndexOf("@"));
                maskedPart = maskedPart.replace("{domain}", domain);
            }
            
            return maskedPart;
        }
        
        return value;
    }

    public static class Builder {
        public static RegexMaskingStrategy fromConfig(String configKey) {
            try {
                Configuration config = new PropertiesConfiguration("log-masking.properties");
                
                String patternStr = config.getString("mask.patterns." + configKey);
                String replacementTemplate = config.getString("mask.replacement." + configKey);
                
                return new RegexMaskingStrategy(patternStr, replacementTemplate, true);
            } catch (Exception e) {
                throw new RuntimeException("Error loading masking configuration", e);
            }
        }
    }
}
```

## 5. Masking Manager
```java
package com.logmasking;

import com.logmasking.strategies.RegexMaskingStrategy;
import java.util.ArrayList;
import java.util.List;

public class MaskingManager {
    private static final MaskingManager INSTANCE = new MaskingManager();
    private final List<MaskingStrategy> strategies = new ArrayList<>();

    private MaskingManager() {
        // Load strategies from configuration
        addStrategy(RegexMaskingStrategy.Builder.fromConfig("credit-card"));
        addStrategy(RegexMaskingStrategy.Builder.fromConfig("email"));
        addStrategy(RegexMaskingStrategy.Builder.fromConfig("phone"));
        addStrategy(RegexMaskingStrategy.Builder.fromConfig("ssn"));
    }

    public static MaskingManager getInstance() {
        return INSTANCE;
    }

    public void addStrategy(MaskingStrategy strategy) {
        strategies.add(strategy);
    }

    public String maskSensitiveData(String input) {
        String maskedData = input;
        for (MaskingStrategy strategy : strategies) {
            maskedData = strategy.mask(maskedData);
        }
        return maskedData;
    }
}
```

## 6. Logback Masking Filter
```java
package com.logmasking.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.logmasking.MaskingManager;

public class LogbackMaskingFilter extends Filter<ILoggingEvent> {
    private final MaskingManager maskingManager = MaskingManager.getInstance();

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (event.getMessage() == null) {
            return FilterReply.NEUTRAL;
        }

        String maskedMessage = maskingManager.maskSensitiveData(event.getMessage());
        event.setMessage(maskedMessage);

        return FilterReply.NEUTRAL;
    }
}
```

## 7. Test Cases
```java
package com.logmasking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MaskingTest {
    private MaskingManager maskingManager;

    @BeforeEach
    void setUp() {
        maskingManager = MaskingManager.getInstance();
    }

    @Test
    void testCreditCardMasking() {
        String input = "Credit Card: 4111-1111-1111-1111";
        String masked = maskingManager.maskSensitiveData(input);
        assertEquals("Credit Card: XXXX-XXXX-XXXX-1111", masked);
    }

    @Test
    void testEmailMasking() {
        String input = "User email: john.doe@example.com";
        String masked = maskingManager.maskSensitiveData(input);
        assertEquals("User email: ****@example.com", masked);
    }

    @Test
    void testPhoneMasking() {
        String input = "Contact: 123-456-7890";
        String masked = maskingManager.maskSensitiveData(input);
        assertEquals("Contact: XXX-XXX-7890", masked);
    }

    @Test
    void testSSNMasking() {
        String input = "SSN: 123-45-6789";
        String masked = maskingManager.maskSensitiveData(input);
        assertEquals("SSN: XXX-XX-6789", masked);
    }

    @Test
    void testMultipleSensitiveDataMasking() {
        String input = "User john.doe@example.com with CC 4111-1111-1111-1111 and SSN 123-45-6789";
        String masked = maskingManager.maskSensitiveData(input);
        assertEquals("User ****@example.com with CC XXXX-XXXX-XXXX-1111 and SSN XXX-XX-6789", masked);
    }
}
```

## 8. Logback Configuration (logback.xml)
```xml
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <filter class="com.logmasking.filter.LogbackMaskingFilter"/>
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE" />
    </root>
</configuration>
```

## 9. README.md
```markdown
# Log Masking Solution

## Overview
A comprehensive solution for masking sensitive data in log files with configurable regex patterns.

## Features
- Configurable masking patterns
- Preserve last 4 characters of sensitive data
- Support for multiple data types (Credit Card, Email, Phone, SSN)
- Easy to extend and customize

## Configuration
Modify `log-masking.properties` to add or modify masking patterns.

## Usage
1. Add Maven dependency
2. Configure logging framework
3. Mask sensitive data automatically
```

## Key Changes
1. Moved regex patterns to `log-masking.properties`
2. Implemented option to preserve last 4 characters
3. Added comprehensive test cases
4. Created complete project structure with POM file
5. Added builder pattern for strategy creation
6. Included README and configuration files

Would you like me to elaborate on any specific aspect of the implementation?
