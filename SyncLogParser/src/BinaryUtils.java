public class BinaryUtils
{
    public static byte[] hexStringToByteArray(String s)
    {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String toBitString(int value)
    {
        StringBuilder builder = new StringBuilder();

        for (int i = 31; i >= 0; i--)
        {
            builder.append(BinaryUtils.isBitSet(value, i) ? "1" : "0");
        }

        return builder.toString();
    }

    public static boolean isBitSet(int value, int bit)
    {
        return ((value & (1 << bit)) != 0);
    }

    public static int getBitRange(int value, int start, int length)
    {
        int mask = 0;
        for (int i = 0; i < length; i++)
        {
            mask |= (1 << (start + i));
        }

        return (value & mask) >> start;
    }

    // Mask off sign extension
    public static int toInt(byte b)
    {
        return b & 0x000000ff;
    }

    public static int composeInt(byte b1, byte b2, byte b3, byte b4)
    {
        //System.out.println("b1: " + Integer.toHexString(BinaryUtils.toInt(b1)));
        //System.out.println("b2: " + Integer.toHexString(BinaryUtils.toInt(b2)));
        //System.out.println("b3: " + Integer.toHexString(BinaryUtils.toInt(b3)));
        //System.out.println("b4: " + Integer.toHexString(BinaryUtils.toInt(b4)));

        return BinaryUtils.toInt(b1) << 24 |
               BinaryUtils.toInt(b2) << 16 | 
               BinaryUtils.toInt(b3) << 8 | 
               BinaryUtils.toInt(b4);
    }
}
