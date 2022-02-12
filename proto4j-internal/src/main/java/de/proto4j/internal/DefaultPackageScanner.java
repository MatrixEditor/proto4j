package de.proto4j.internal; //@date 12.02.2022

import de.proto4j.internal.logger.Logger;
import de.proto4j.internal.logger.PrintColor;
import de.proto4j.internal.logger.PrintService;
import de.proto4j.internal.model.PackageScanner;
import de.proto4j.stream.InterruptedStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

class DefaultPackageScanner implements PackageScanner {

    private static final Logger LOGGER = PrintService.createLogger(PackageScanner.class);

    @Override
    public void readInto(InterruptedStream<Class<?>> stream, String pkg, boolean recursive) {
        if (pkg == null || pkg.length() == 0) return;

        ClassLoader cl = ClassLoader.getSystemClassLoader();
        InputStream is = Objects.requireNonNull(cl.getResourceAsStream(pkg.replaceAll("[.]", "/")));

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            while (br.ready()) {
                String name = br.readLine();
                if (name != null && FILENAME_FILTER.test(name)) {
                    Class<?> c = Class.forName(makeClassName(pkg, name.split("[.]")[0]));

                    if (c.getDeclaredClasses().length != 0) {
                        for (Class<?> cn : c.getDeclaredClasses()) {
                            stream.yield(cn);
                        }
                    }
                    stream.yield(c);
                } else if (recursive && name != null && !name.contains("package-info")) {
                    readInto(stream, makeClassName(pkg, name), true);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.except(PrintColor.BRIGHT_MAGENTA, e);
        }

    }

    private String makeClassName(String p, String n) {
        return String.join(".", p, n);
    }
}
