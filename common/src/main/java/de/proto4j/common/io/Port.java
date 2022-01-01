package de.proto4j.common.io;//@date 25.11.2021

// maybe the raw port size shouldn't be fixed size
public final class Port {

    private static final int[][][] port_wrapper = {
            {{0, 0, 1}},
            {{0, 0, 2}},
            {{0, 0, 2}, {1, 2, 3}},
            {{0, 0, 2}, {1, 2, 4}},
            {{0, 0, 2}, {1, 2, 4}, {2, 4, 5}}
    };
    private static final int[] port_numbers = {
            10, 100, 1000, 10000, 100000
    };

    public static final byte PORT_ZERO = -1;

    public static final byte PORT_DOUBLE_ZERO = -2;

    private int port;
    private byte[] raw_port;

    public Port(int p) {
        this(toArray(p));
    }

    public Port(byte[] ps) {
        if (ps.length != 3)
            throw new IllegalArgumentException();

        int p = toInt(ps);
        if (1 <= p && p <= 65535) {
            port     = p;
            raw_port = ps;
        }
    }

    public static Port of(int... values) {
        if (values.length != 3)
            throw new IllegalArgumentException();

        byte[] b = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            if (values[i] > 100)
                throw new IllegalArgumentException();
            b[i] = (byte) values[i];
        }
        return new Port(b);
    }

    public static int toInt(byte[] ps) {
        if (ps.length == 3) {
            StringBuilder d = new StringBuilder();
            for (byte b : ps) {
                switch (b) {
                    case PORT_ZERO:
                        d.append("0");break;
                    case PORT_DOUBLE_ZERO:
                        d.append("00");break;
                    default:
                        if (b != 0)
                            d.append(b);
                        break;
                }
            }
            return Integer.parseInt(d.toString());
        } else return -1;
    }

    public static Port ofInt(int port) {
        return new Port(toArray(port));
    }

    private static byte[] toArray(int port) {
        return toArray(String.valueOf(port), port);
    }

    private static byte[] toArray(String s, int port) {
        byte[] b = new byte[3];

        for (int i = 0, port_numbersLength = port_numbers.length; i < port_numbersLength; i++) {
            int o = port_numbers[i];
            if (port < o) {
                for (int[] pp : port_wrapper[i]) {
                    byte b0 = Byte.parseByte(s.substring(pp[1], pp[2]));
                    if (pp[2] - pp[1] == 2) {
                        b[pp[0]] = b0 == 0 ? PORT_DOUBLE_ZERO : b0;
                    } else b[pp[0]] = b0 == 0 ? PORT_ZERO : b0;
                }
                break;
            }
        }
        return b;
    }

    public int getInt() {
        return port;
    }

    public void setPort(int p) {
        if (1 <= p && p <= 65535) {
            port     = p;
            raw_port = toArray(p);
        }
    }

    public byte[] getAsBytes() {
        return raw_port;
    }

    public void setRawPort(byte[] ps) {
        int p = toInt(ps);
        if (1 <= p && p <= 65565) {
            port     = p;
            raw_port = ps;
        }
    }

    @Override
    public String toString() {
        return String.valueOf(port);
    }
}
