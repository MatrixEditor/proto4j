package de.proto4j.common;//@date 24.11.2021

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.IllegalFormatException;
import java.util.Locale;

public class PrintService<T extends PrintStream> {

    private static final PrintService<PrintStream> systemOut = new PrintService<>(System.out);

    public static PrintService<PrintStream> getSystemService() {
        return systemOut;
    }

    public static final char ENDC = '\033';

    private final T stream;

    public PrintService(T stream) {this.stream = stream;}

    public static String embedC(String msg, PrintColor c) {
        return embedC(msg, c, "");
    }

    public static String embedC(String msg, PrintColor c, String other) {
        return ENDC + c.getColorCode() + msg + ENDC + "[0m " + other;
    }

    public static void printS(String msg, PrintColor c, String... other) {
        printS(msg, c, getSystemService(), other);
    }

    public static <E extends PrintStream> void printS(String msg, PrintColor c, PrintService<E> ps, String... other) {
        ps.println(embedC(msg, c, String.join(" ", other)));
    }

    public static void logError(Throwable e, PrintColor pc) {
        printS("[!]", pc, "[" + e.getClass().getSimpleName() + "]", e.getMessage());
    }

    public static void log(PrintColor pcm, String...msg) {
        printS("[i]", pcm, msg);
    }

    public void printf(String m, Object...args) {
        stream.printf(m, args);
    }

    /**
     * Flushes the stream.  This is done by writing any buffered output bytes to
     * the underlying output stream and then flushing that stream.
     *
     * @see        OutputStream#flush()
     */
    public void flush() {
        stream.flush();
    }

    /**
     * Closes the stream.  This is done by flushing the stream and then closing
     * the underlying output stream.
     *
     * @see        OutputStream#close()
     */
    public void close() {
        stream.close();
    }

    /**
     * Flushes the stream and checks its error state. The internal error state
     * is set to {@code true} when the underlying output stream throws an
     * {@code IOException} other than {@code InterruptedIOException},
     * and when the {@code setError} method is invoked.  If an operation
     * on the underlying output stream throws an
     * {@code InterruptedIOException}, then the {@code PrintStream}
     * converts the exception back into an interrupt by doing:
     * <pre>{@code
     *     Thread.currentThread().interrupt();
     * }</pre>
     * or the equivalent.
     *
     * @return {@code true} if and only if this stream has encountered an
     *         {@code IOException} other than
     *         {@code InterruptedIOException}, or the
     *         {@code setError} method has been invoked
     */
    public boolean checkError() {
        return stream.checkError();
    }

    /**
     * Writes the specified byte to this stream.  If the byte is a newline and
     * automatic flushing is enabled then the {@code flush} method will be
     * invoked.
     *
     * <p> Note that the byte is written as given; to write a character that
     * will be translated according to the platform's default character
     * encoding, use the {@code print(char)} or {@code println(char)}
     * methods.
     *
     * @param  b  The byte to be written
     * @see #print(char)
     * @see #println(char)
     */
    public void write(int b) {
        stream.write(b);
    }

    /**
     * Writes {@code len} bytes from the specified byte array starting at
     * offset {@code off} to this stream.  If automatic flushing is
     * enabled then the {@code flush} method will be invoked.
     *
     * <p> Note that the bytes will be written as given; to write characters
     * that will be translated according to the platform's default character
     * encoding, use the {@code print(char)} or {@code println(char)}
     * methods.
     *  @param  buf   A byte array
     * @param  off   Offset from which to start taking bytes
     * @param  len   Number of bytes to write
     */
    public void write(byte[] buf, int off, int len) {
        stream.write(buf, off, len);
    }

    /**
     * Prints a boolean value.  The string produced by {@link
     * String#valueOf(boolean)} is translated into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the
     * {@link #write(int)} method.
     *
     * @param      b   The {@code boolean} to be printed
     */
    public void print(boolean b) {
        stream.print(b);
    }

    /**
     * Prints a character.  The character is translated into one or more bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the
     * {@link #write(int)} method.
     *
     * @param      c   The {@code char} to be printed
     */
    public void print(char c) {
        stream.print(c);
    }

    /**
     * Prints an integer.  The string produced by {@link
     * String#valueOf(int)} is translated into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the
     * {@link #write(int)} method.
     *
     * @param      i   The {@code int} to be printed
     * @see        Integer#toString(int)
     */
    public void print(int i) {
        stream.print(i);
    }

    /**
     * Prints a long integer.  The string produced by {@link
     * String#valueOf(long)} is translated into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the
     * {@link #write(int)} method.
     *
     * @param      l   The {@code long} to be printed
     * @see        Long#toString(long)
     */
    public void print(long l) {
        stream.print(l);
    }

    /**
     * Prints a floating-point number.  The string produced by {@link
     * String#valueOf(float)} is translated into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the
     * {@link #write(int)} method.
     *
     * @param      f   The {@code float} to be printed
     * @see        Float#toString(float)
     */
    public void print(float f) {
        stream.print(f);
    }

    /**
     * Prints a double-precision floating-point number.  The string produced by
     * {@link String#valueOf(double)} is translated into
     * bytes according to the platform's default character encoding, and these
     * bytes are written in exactly the manner of the {@link
     * #write(int)} method.
     *
     * @param      d   The {@code double} to be printed
     * @see        Double#toString(double)
     */
    public void print(double d) {
        stream.print(d);
    }

    /**
     * Prints an array of characters.  The characters are converted into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the
     * {@link #write(int)} method.
     *
     * @param      s   The array of chars to be printed
     *
     * @throws NullPointerException  If {@code s} is {@code null}
     */
    public void print(char[] s) {
        stream.print(s);
    }

    /**
     * Prints a string.  If the argument is {@code null} then the string
     * {@code "null"} is printed.  Otherwise, the string's characters are
     * converted into bytes according to the platform's default character
     * encoding, and these bytes are written in exactly the manner of the
     * {@link #write(int)} method.
     *
     * @param      s   The {@code String} to be printed
     */
    public void print(String s) {
        stream.print(s);
    }

    /**
     * Prints an object.  The string produced by the {@link
     * String#valueOf(Object)} method is translated into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the
     * {@link #write(int)} method.
     *
     * @param      obj   The {@code Object} to be printed
     * @see        Object#toString()
     */
    public void print(Object obj) {
        stream.print(obj);
    }

    /**
     * Terminates the current line by writing the line separator string.  The
     * line separator string is defined by the system property
     * {@code line.separator}, and is not necessarily a single newline
     * character ({@code '\n'}).
     */
    public void println() {
        stream.println();
    }

    /**
     * Prints a boolean and then terminate the line.  This method behaves as
     * though it invokes {@link #print(boolean)} and then
     * {@link #println()}.
     *
     * @param x  The {@code boolean} to be printed
     */
    public void println(boolean x) {
        stream.println(x);
    }

    /**
     * Prints a character and then terminate the line.  This method behaves as
     * though it invokes {@link #print(char)} and then
     * {@link #println()}.
     *
     * @param x  The {@code char} to be printed.
     */
    public void println(char x) {
        stream.println(x);
    }

    /**
     * Prints an integer and then terminate the line.  This method behaves as
     * though it invokes {@link #print(int)} and then
     * {@link #println()}.
     *
     * @param x  The {@code int} to be printed.
     */
    public void println(int x) {
        stream.println(x);
    }

    /**
     * Prints a long and then terminate the line.  This method behaves as
     * though it invokes {@link #print(long)} and then
     * {@link #println()}.
     *
     * @param x  a The {@code long} to be printed.
     */
    public void println(long x) {
        stream.println(x);
    }

    /**
     * Prints a float and then terminate the line.  This method behaves as
     * though it invokes {@link #print(float)} and then
     * {@link #println()}.
     *
     * @param x  The {@code float} to be printed.
     */
    public void println(float x) {
        stream.println(x);
    }

    /**
     * Prints a double and then terminate the line.  This method behaves as
     * though it invokes {@link #print(double)} and then
     * {@link #println()}.
     *
     * @param x  The {@code double} to be printed.
     */
    public void println(double x) {
        stream.println(x);
    }

    /**
     * Prints an array of characters and then terminate the line.  This method
     * behaves as though it invokes {@link #print(char[])} and
     * then {@link #println()}.
     *
     * @param x  an array of chars to print.
     */
    public void println(char[] x) {
        stream.println(x);
    }

    /**
     * Prints a String and then terminate the line.  This method behaves as
     * though it invokes {@link #print(String)} and then
     * {@link #println()}.
     *
     * @param x  The {@code String} to be printed.
     */
    public void println(String x) {
        stream.println(x);
    }

    /**
     * Prints an Object and then terminate the line.  This method calls
     * at first String.valueOf(x) to get the printed object's string value,
     * then behaves as
     * though it invokes {@link #print(String)} and then
     * {@link #println()}.
     *
     * @param x  The {@code Object} to be printed.
     */
    public void println(Object x) {
        stream.println(x);
    }

    /**
     * A convenience method to write a formatted string to this output stream
     * using the specified format string and arguments.
     *
     * <p> An invocation of this method of the form
     * {@code out.printf(l, format, args)} behaves
     * in exactly the same way as the invocation
     *
     * <pre>{@code
     *     out.format(l, format, args)
     * }</pre>
     *
     * @param  l
     *         The {@linkplain Locale locale} to apply during
     *         formatting.  If {@code l} is {@code null} then no localization
     *         is applied.
     *
     * @param  format
     *         A format string as described in <a
     *         href="../util/Formatter.html#syntax">Format string syntax</a>
     *
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.  If there are more arguments than format specifiers, the
     *         extra arguments are ignored.  The number of arguments is
     *         variable and may be zero.  The maximum number of arguments is
     *         limited by the maximum dimension of a Java array as defined by
     *         <cite>The Java&trade; Virtual Machine Specification</cite>.
     *         The behaviour on a
     *         {@code null} argument depends on the <a
     *         href="../util/Formatter.html#syntax">conversion</a>.
     *
     * @throws IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the <a
     *          href="../util/Formatter.html#detail">Details</a> section of the
     *          formatter class specification.
     *
     * @throws NullPointerException
     *          If the {@code format} is {@code null}
     *
     * @return This output stream
     *
     * @since 1.5
     */
    public PrintStream printf(Locale l, String format, Object... args) {
        return stream.printf(l, format, args);
    }

    /**
     * Writes a formatted string to this output stream using the specified
     * format string and arguments.
     *
     * <p> The locale always used is the one returned by {@link
     * Locale#getDefault(Locale.Category)} with
     * {@link Locale.Category#FORMAT FORMAT} category specified,
     * regardless of any previous invocations of other formatting methods on
     * this object.
     *
     * @param  format
     *         A format string as described in <a
     *         href="../util/Formatter.html#syntax">Format string syntax</a>
     *
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.  If there are more arguments than format specifiers, the
     *         extra arguments are ignored.  The number of arguments is
     *         variable and may be zero.  The maximum number of arguments is
     *         limited by the maximum dimension of a Java array as defined by
     *         <cite>The Java&trade; Virtual Machine Specification</cite>.
     *         The behaviour on a
     *         {@code null} argument depends on the <a
     *         href="../util/Formatter.html#syntax">conversion</a>.
     *
     * @throws IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the <a
     *          href="../util/Formatter.html#detail">Details</a> section of the
     *          formatter class specification.
     *
     * @throws NullPointerException
     *          If the {@code format} is {@code null}
     *
     * @return This output stream
     *
     * @since 1.5
     */
    public PrintStream format(String format, Object... args) {
        return stream.format(format, args);
    }

    /**
     * Writes a formatted string to this output stream using the specified
     * format string and arguments.
     *
     * @param  l
     *         The {@linkplain Locale locale} to apply during
     *         formatting.  If {@code l} is {@code null} then no localization
     *         is applied.
     *
     * @param  format
     *         A format string as described in <a
     *         href="../util/Formatter.html#syntax">Format string syntax</a>
     *
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.  If there are more arguments than format specifiers, the
     *         extra arguments are ignored.  The number of arguments is
     *         variable and may be zero.  The maximum number of arguments is
     *         limited by the maximum dimension of a Java array as defined by
     *         <cite>The Java&trade; Virtual Machine Specification</cite>.
     *         The behaviour on a
     *         {@code null} argument depends on the <a
     *         href="../util/Formatter.html#syntax">conversion</a>.
     *
     * @throws IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the <a
     *          href="../util/Formatter.html#detail">Details</a> section of the
     *          formatter class specification.
     *
     * @throws NullPointerException
     *          If the {@code format} is {@code null}
     *
     * @return This output stream
     *
     * @since 1.5
     */
    public PrintStream format(Locale l, String format, Object... args) {
        return stream.format(l, format, args);
    }

    /**
     * Appends the specified character sequence to this output stream.
     *
     * <p> An invocation of this method of the form {@code out.append(csq)}
     * behaves in exactly the same way as the invocation
     *
     * <pre>{@code
     *     out.print(csq.toString())
     * }</pre>
     *
     * <p> Depending on the specification of {@code toString} for the
     * character sequence {@code csq}, the entire sequence may not be
     * appended.  For instance, invoking then {@code toString} method of a
     * character buffer will return a subsequence whose content depends upon
     * the buffer's position and limit.
     *
     * @param  csq
     *         The character sequence to append.  If {@code csq} is
     *         {@code null}, then the four characters {@code "null"} are
     *         appended to this output stream.
     *
     * @return This output stream
     *
     * @since 1.5
     */
    public PrintStream append(CharSequence csq) {
        return stream.append(csq);
    }

    /**
     * Appends a subsequence of the specified character sequence to this output
     * stream.
     *
     * <p> An invocation of this method of the form
     * {@code out.append(csq, start, end)} when
     * {@code csq} is not {@code null}, behaves in
     * exactly the same way as the invocation
     *
     * <pre>{@code
     *     out.print(csq.subSequence(start, end).toString())
     * }</pre>
     *
     * @param  csq
     *         The character sequence from which a subsequence will be
     *         appended.  If {@code csq} is {@code null}, then characters
     *         will be appended as if {@code csq} contained the four
     *         characters {@code "null"}.
     *
     * @param  start
     *         The index of the first character in the subsequence
     *
     * @param  end
     *         The index of the character following the last character in the
     *         subsequence
     *
     * @return This output stream
     *
     * @throws IndexOutOfBoundsException
     *          If {@code start} or {@code end} are negative, {@code start}
     *          is greater than {@code end}, or {@code end} is greater than
     *          {@code csq.length()}
     *
     * @since 1.5
     */
    public PrintStream append(CharSequence csq, int start, int end) {
        return stream.append(csq, start, end);
    }

    /**
     * Appends the specified character to this output stream.
     *
     * <p> An invocation of this method of the form {@code out.append(c)}
     * behaves in exactly the same way as the invocation
     *
     * <pre>{@code
     *     out.print(c)
     * }</pre>
     *
     * @param  c
     *         The 16-bit character to append
     *
     * @return This output stream
     *
     * @since 1.5
     */
    public PrintStream append(char c) {
        return stream.append(c);
    }

    /**
     * Writes <code>b.length</code> bytes to this output stream.
     * <p>
     * The <code>write</code> method of <code>FilterOutputStream</code>
     * calls its <code>write</code> method of three arguments with the
     * arguments <code>b</code>, <code>0</code>, and
     * <code>b.length</code>.
     * <p>
     * Note that this method does not call the one-argument
     * <code>write</code> method of its underlying output stream with
     * the single argument <code>b</code>.
     *
     * @param      b   the data to be written.
     * @exception IOException  if an I/O error occurs.
     * @see        FilterOutputStream#write(byte[], int, int)
     */
    public void write(byte[] b) throws IOException {
        stream.write(b);
    }
}
