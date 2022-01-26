package de.proto4j.common; //@date 29.12.2021

import de.proto4j.common.annotation.*;
import de.proto4j.common.exception.ProtocolClassException;
import de.proto4j.common.exception.ProtocolItemNotFoundException;

import java.lang.annotation.Annotation;
import java.lang.annotation.AnnotationTypeMismatchException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.BiPredicate;

public class ProtocolUtil {

    public static final ProtocolUtil PROTOCOL_UTIL = new ProtocolUtil();

    public ProtocolUtil() {
    }

    public static ProtocolUtil getInstance() {
        return PROTOCOL_UTIL;
    }

    public Item[] getItems(Object t) {
        Objects.requireNonNull(t);

        Item[] p = t.getClass().getAnnotationsByType(Item.class);
        if (p.length == 0)
            throw new NoSuchElementException();

        return p;
    }

    public <R> R newObject(Class<R> __c, Object... params) throws NoSuchMethodException {
        return newObject(__c, null, params);
    }

    @SuppressWarnings("unchecked")
    public <E extends Annotation, R> R newObject(Class<R> __c, Class<E> __ac, Object... params) throws NoSuchMethodException {
        if (__ac != null) {
            E e = AnnotationUtil.lookup(__c, __ac);
            if (e == null)
                throw new AnnotationTypeMismatchException(null, "");
        }

        for (Constructor<?> c : __c.getDeclaredConstructors()) {
            if (c.getParameterCount() == params.length) {
                if (AnnotationUtil.checkParameterClasses(c.getParameterTypes(), params)) try {
                    return (R) c.newInstance(params);
                } catch (ReflectiveOperationException ex) {
                    if (ex instanceof InvocationTargetException)
                        PrintService.logError(((InvocationTargetException) ex).getTargetException(),
                                              PrintColor.DARK_RED);
                    else PrintService.logError(ex, PrintColor.DARK_RED);
                    return null;
                }
            }
        }
        throw new NoSuchMethodException("Constructor not found!");
    }

    private boolean isSameClass(Class<?> a, Class<?> b) {
        return a.isAssignableFrom(b) || b.isAssignableFrom(a);
    }

    @SuppressWarnings("unchecked")
    public <R> R readPacket(Protocol p, Object ref, Object... input) throws ReflectiveOperationException,
            ProtocolClassException,
            ProtocolItemNotFoundException {
        Objects.requireNonNull(p);

        Map<String, Class<?>> map = AnnotationUtil.get("attributes", p);
        if (!map.containsKey(Protocol.PACKET_READER_ATTRIBUTE)) {
            if (input.length == 1) {
                try {
                    return (R) input[0];
                } catch (ClassCastException ignored) {}
            }
            throw new ProtocolItemNotFoundException("Attribute '" + Protocol.PACKET_READER_ATTRIBUTE + "' not found!");
        }
        Class<?> protocolReader = map.get(Protocol.PACKET_READER_ATTRIBUTE);
        if (protocolReader != null) {
            IProtocolReader pw = AnnotationUtil.lookup(protocolReader, IProtocolReader.class);
            Objects.requireNonNull(pw);

            if (AnnotationUtil.checkParameterClasses(pw.inputClasses(), input)) {
                Method m0 = protocolReader.getMethod(pw.method(), pw.inputClasses());
                if (!m0.trySetAccessible())
                    PrintService.log(PrintColor.DARK_YELLOW, m0.getName(), " -> could not access!");

                Object x = m0.invoke(ref, input);
                if (x.getClass().isAssignableFrom(pw.outputClass())) {
                    return (R) x;
                }
            }
        }
        throw new ProtocolClassException("PacketReader is null or not found!");
    }

    public <SOCKET, R> R receive(SOCKET s, Object... params)
            throws ReflectiveOperationException, ProtocolClassException {
        Objects.requireNonNull(s);

        ISocket s_ = AnnotationUtil.lookup(s.getClass(), ISocket.class);
        Objects.requireNonNull(s_);
        return executeOnSocket(s, s_.receive(), (a, b) -> true, params);
    }

    public <SOCKET, R> R send(SOCKET s, Object... params)
            throws ReflectiveOperationException, ProtocolClassException {
        Objects.requireNonNull(s);

        ISocket s_ = AnnotationUtil.lookup(s.getClass(), ISocket.class);
        Objects.requireNonNull(s_);
        return executeOnSocket(s, s_.send(), params);
    }

    public <SOCKET, R> R close(SOCKET s, Object... params)
            throws ReflectiveOperationException, ProtocolClassException {
        Objects.requireNonNull(s);

        ISocket s_ = AnnotationUtil.lookup(s.getClass(), ISocket.class);
        Objects.requireNonNull(s_);
        return executeOnSocket(s, s_.close(), params);
    }

    public <SERV_S, R> R accept(SERV_S s, Object... params) throws ReflectiveOperationException,
            ProtocolClassException {
        Objects.requireNonNull(s);

        IServerSocket s_ = AnnotationUtil.lookup(s.getClass(), IServerSocket.class);
        Objects.requireNonNull(s_);
        return executeOnSocket(s, s_.accept(), params);
    }

    @SuppressWarnings("unchecked")
    <SOCKET, R> R executeOnSocket(SOCKET s, ISocket.SocketMethod sm, Object... params)
            throws ReflectiveOperationException, ProtocolClassException {
        return executeOnSocket(s, sm, ProtocolUtil.getInstance()::isSameClass, params);
    }

    @SuppressWarnings("unchecked")
    <SOCKET, R> R executeOnSocket(SOCKET s, ISocket.SocketMethod sm,
                                  BiPredicate<Class<?>, Class<?>> p, Object... params)
            throws ReflectiveOperationException, ProtocolClassException {
        Method m0 = s.getClass().getMethod(sm.name(), sm.params());
        if (!m0.canAccess(s)) m0.setAccessible(true);

        Object x = m0.invoke(s, params);
        if (x == null) return null;

        if (p.test(x.getClass(), sm.returnClass()))
            return (R) x;
        throw new ProtocolClassException();
    }

    public <T> String toString(T t) {
        Objects.requireNonNull(t);

        StringJoiner sj = new StringJoiner(", ", t.getClass().getSimpleName() + ":[", "]");
        for (Field f0 : t.getClass().getDeclaredFields()) {
            f0.trySetAccessible();
            sj.add(f0.getName() + "=" + f0.getType().getSimpleName());
        }
        return sj.toString();
    }

    public <T> boolean isProtocol(T newProtocol) {
        return newProtocol instanceof Protocol;
    }

}
