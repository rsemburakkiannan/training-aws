package com.example.logmasking;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.appender.rewrite.RewritePolicy;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.ConcurrentHashMap;

@Plugin(name = "MaskingRewritePolicy", 
         category = "Core", 
         elementType = "rewritePolicy", 
         printObject = true)
public class MaskingRewritePolicy implements RewritePolicy {

    private static final Logger LOGGER = LogManager.getLogger(MaskingRewritePolicy.class);
    
    // Cache compiled patterns for performance
    private final ConcurrentHashMap<String, Pattern> compiledPatterns = new ConcurrentHashMap<>();
    private final Properties patterns;
    private final String configFile;

    private MaskingRewritePolicy(String configFile, Properties patterns) {
        this.configFile = configFile;
        this.patterns = patterns;
        precompilePatterns();
        LOGGER.info("MaskingRewritePolicy initialized with {} patterns from {}", 
                   patterns.size() / 2, configFile); // Divide by 2 because we have .pattern and .unmasked pairs
    }

    private void precompilePatterns() {
        for (String key : patterns.stringPropertyNames()) {
            if (key.endsWith(".pattern")) {
                try {
                    String patternStr = patterns.getProperty(key);
                    Pattern pattern = Pattern.compile(patternStr);
                    compiledPatterns.put(key, pattern);
                    LOGGER.debug("Compiled pattern for key: {}", key);
                } catch (Exception e) {
                    LOGGER.error("Failed to compile pattern for key: {}", key, e);
                }
            }
        }
    }

    @Override
    public LogEvent rewrite(LogEvent event) {
        if (event == null) {
            return event;
        }

        try {
            String originalMessage = event.getMessage().getFormattedMessage();
            String maskedMessage = applyMasking(originalMessage);
            
            // Only create new event if message was actually masked
            if (!originalMessage.equals(maskedMessage)) {
                return createMaskedEvent(event, maskedMessage);
            }
        } catch (Exception e) {
            LOGGER.error("Error applying masking policy to message", e);
        }
        
        return event;
    }

    private LogEvent createMaskedEvent(LogEvent original, String maskedMessage) {
        // Create a new log event with the masked message
        return Log4jLogEvent.newBuilder()
                .setLoggerName(original.getLoggerName())
                .setLoggerFqcn(original.getLoggerFqcn())
                .setLevel(original.getLevel())
                .setMessage(new SimpleMessage(maskedMessage))
                .setThreadName(original.getThreadName())
                .setTimeMillis(original.getTimeMillis())
                .setThrown(original.getThrown())
                .setContextData(original.getContextData())
                .setContextStack(original.getContextStack())
                .setSource(original.getSource())
                .build();
    }

    private String applyMasking(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        String result = message;
        
        for (String key : compiledPatterns.keySet()) {
            try {
                Pattern pattern = compiledPatterns.get(key);
                String unmaskedKey = key.replace(".pattern", ".unmasked");
                String unmaskedStr = patterns.getProperty(unmaskedKey, "0");
                int unmasked = Integer.parseInt(unmaskedStr);
                
                result = maskSensitiveData(result, pattern, unmasked);
            } catch (Exception e) {
                LOGGER.warn("Failed to apply masking for pattern key: {}", key, e);
            }
        }
        
        return result;
    }

    private String maskSensitiveData(String message, Pattern pattern, int unmasked) {
        if (pattern == null) {
            return message;
        }

        Matcher matcher = pattern.matcher(message);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String match = matcher.group();
            String masked = createMaskedString(match, unmasked);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(masked));
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }

    private String createMaskedString(String original, int unmasked) {
        if (original == null || original.length() <= unmasked) {
            return original;
        }
        
        int maskLength = original.length() - unmasked;
        StringBuilder masked = new StringBuilder();
        
        // Preserve structure (spaces, dashes) while masking digits/letters
        for (int i = 0; i < original.length(); i++) {
            char c = original.charAt(i);
            if (i < maskLength) {
                if (Character.isLetterOrDigit(c)) {
                    masked.append('*');
                } else {
                    masked.append(c); // Preserve formatting characters
                }
            } else {
                masked.append(c); // Show last 'unmasked' characters
            }
        }
        
        return masked.toString();
    }

    @PluginFactory
    public static MaskingRewritePolicy createPolicy(
            @PluginAttribute("configFile") String configFile,
            @PluginAttribute(value = "maskChar", defaultValue = "*") String maskChar) {
        
        LOGGER.info("Creating MaskingRewritePolicy with configFile: {}", configFile);
        
        if (configFile == null || configFile.trim().isEmpty()) {
            LOGGER.error("configFile attribute is required for MaskingRewritePolicy");
            throw new IllegalArgumentException("configFile attribute is required");
        }
        
        Properties patterns = loadProperties(configFile);
        return new MaskingRewritePolicy(configFile, patterns);
    }

    private static Properties loadProperties(String configFile) {
        Properties patterns = new Properties();
        
        try (InputStream input = MaskingRewritePolicy.class.getClassLoader()
                .getResourceAsStream(configFile)) {
            
            if (input == null) {
                LOGGER.error("Properties file not found in classpath: {}", configFile);
                throw new IllegalArgumentException("Properties file not found: " + configFile);
            }
            
            patterns.load(input);
            LOGGER.info("Successfully loaded {} properties from {}", patterns.size(), configFile);
            
            // Validate properties
            validateProperties(patterns);
            
        } catch (IOException e) {
            LOGGER.error("Failed to load properties file: {}", configFile, e);
            throw new RuntimeException("Failed to load properties file: " + configFile, e);
        }
        
        return patterns;
    }

    private static void validateProperties(Properties patterns) {
        for (String key : patterns.stringPropertyNames()) {
            if (key.endsWith(".pattern")) {
                String unmaskedKey = key.replace(".pattern", ".unmasked");
                if (!patterns.containsKey(unmaskedKey)) {
                    LOGGER.warn("Missing unmasked property for pattern: {}", key);
                }
                
                // Test pattern compilation
                try {
                    Pattern.compile(patterns.getProperty(key));
                } catch (Exception e) {
                    LOGGER.error("Invalid regex pattern for key {}: {}", key, patterns.getProperty(key));
                    throw new IllegalArgumentException("Invalid regex pattern for key: " + key, e);
                }
            }
        }
    }
}
