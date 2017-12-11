package jp.jyn.simplewebserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.rewrite.handler.HeaderPatternRule;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Slf4jRequestLog;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Slf4jLog;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.util.stream.Stream;

public class JettyInitializer {
    private final Logger logger = LogManager.getLogger(JettyInitializer.class);

    private final Server server;

    public static Server createServer(Main.Option option) {
        return new JettyInitializer(option).server;
    }

    private JettyInitializer(Main.Option option) {
        // Set logger.
        try {
            logger.info("Set Jetty logger to Log4j2");
            Log.setLog(new Slf4jLog());
            Log.initialized(); // wait
        } catch (Exception e) {
            logger.error("", e);
        }

        // Check port range
        if (option.port <= 0 || option.port >= 65535) {
            if (option.port != 0) {
                logger.warn("Port number range is 1-65534.");
            }
            // Select available port
            option.port = getAvailablePort();
        }

        logger.info("");
        logger.info("========= config =========");

        // Socket initialize.
        InetSocketAddress address;
        if (option.bind == null) {
            address = new InetSocketAddress(option.port);
        } else {
            logger.info("Bind address: {}", option.bind);
            address = new InetSocketAddress(option.bind, option.port);
        }
        logger.info("Server port: {}", option.port);

        server = new Server(address);

        // Show version...(SWS is for development)
        setShowVersion(option.showVersion);

        /* === Handler === */
        HandlerList handlers = new HandlerList();

        if (option.showVersion) {
            RewriteHandler rewrite = new RewriteHandler();
            rewrite.addRule(createHeaderPatternRule("/*", "X-Powered-By", String.format("%s(%s)", Main.TITLE, Main.VERSION)));
            handlers.addHandler(rewrite);
        }

        handlers.addHandler(createResourceHandler(option.showVersion, option.root, "index.html"));

        server.setHandler(handlers);
        /* === Handler === */

        // Show console log.
        server.setRequestLog(createConsoleRequestLog());
        //server.setStopAtShutdown(true);
        // KeepAlive is default Enabled.

        logger.info("==========================");
        logger.info("");
    }

    private void setShowVersion(boolean showVersion) {
        logger.info("Show server version: {}", showVersion);
        Stream.of(server.getConnectors())
            .flatMap(connector -> connector.getConnectionFactories().stream())
            .flatMap(factory -> (factory instanceof HttpConnectionFactory) ? Stream.of((HttpConnectionFactory) factory) : Stream.empty())
            .forEach(factory -> factory.getHttpConfiguration().setSendServerVersion(showVersion));
    }

    private ResourceHandler createResourceHandler(boolean showIndexOf, Path webroot, String... index) {
        String root = webroot.toString();
        logger.info("Show directory list: {}", showIndexOf);
        logger.info("Web root: {}", root);
        logger.info("Index: {}", () -> String.join(",", index));

        ResourceHandler resource = new ResourceHandler();
        resource.setDirectoriesListed(showIndexOf);
        resource.setResourceBase(root);
        resource.setWelcomeFiles(index);

        return resource;
    }

    private HeaderPatternRule createHeaderPatternRule(String pattern, String name, String values) {
        HeaderPatternRule rule = new HeaderPatternRule();
        rule.setPattern(pattern);
        rule.setName(name);
        rule.setValue(values);
        return rule;
    }

    private RequestLog createConsoleRequestLog() {
        return new Slf4jRequestLog();
    }

    private int getAvailablePort() {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        } catch (IOException e) {
            logger.error("", e);
        }
        return 0;
    }
}
