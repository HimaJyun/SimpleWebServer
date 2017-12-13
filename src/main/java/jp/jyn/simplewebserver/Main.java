package jp.jyn.simplewebserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Main {
    public static final String TITLE;
    public static final String VERSION;

    static {
        Package pkg = Main.class.getPackage();
        String name = pkg.getImplementationTitle();
        String version = pkg.getImplementationVersion();
        TITLE = (name != null ? name : "SimpleWebServer");
        VERSION = (version != null ? version : "Unknown");
    }

    public static void main(String[] args) throws Exception {
        final Logger logger = LogManager.getLogger(Main.class);

        // Parse commandline option.
        final Option op;
        try {
            op = new Option(new ArrayDeque<>(Arrays.asList(args)));
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            ShowHelp();
            return;
        }
        logger.info("Starting {}({})", TITLE, VERSION);
        logger.info("");

        final Server server = JettyInitializer.createServer(op);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.stop();
            } catch (Exception e) {
                logger.error("", e);
            }
            logger.info("");
            logger.info("Good Bye!");
        }));

        server.join();
    }

    private static void ShowHelp() {
        Stream.of(
            String.format("%s v%s by @HimaJyun( https://jyn.jp/ )", Main.TITLE, Main.VERSION),
            "",
            "Usage: sws [option]",
            "",
            "Option:",
            "-p/--port         Specify port  (Ex: -p 8080, -p 0)",
            "-r/--root         Specify root  (Ex: -r ./webroot)",
            "-b/--bind         Bind address  (Ex: -b 127.0.0.1)",
            "-c/--cache        Enable cache",
            "--help/--version  Show help and version",
            ""
        ).forEach(System.out::println);
    }

    public static class Option {
        public int port = 8080;
        public Path root = null;
        public String bind = null;
        public boolean cache = false;
        public boolean showVersion = true;
        private Option(Queue<String> args) {
            Consumer<String> setter = s -> {throw new IllegalArgumentException("Unknown option: " + s);};

            while (!args.isEmpty()) {
                String arg = args.poll();
                switch (arg) {
                    case "--help":
                    case "--version":
                        throw new IllegalArgumentException("");
                    case "-b":
                    case "--bind":
                        setter = s -> bind = s;
                        break;
                    case "-p":
                    case "--port":
                        setter = s -> port = Integer.parseInt(s);
                        break;
                    case "-r":
                    case "--root":
                        setter = s -> root = Paths.get(s).normalize().toAbsolutePath();
                        break;
                    case "-c":
                    case "--cache":
                        cache = true;
                        break;
                    case "--hide": // Secret option(This software is for "development". Don't use in "production".)
                        showVersion = false;
                        break;
                    default:
                        setter.accept(arg);
                        break;
                }
            }

            // Get current directory
            if (root == null) {
                root = new File(".").getAbsoluteFile().getParentFile().toPath();
            }
        }
    }
}
