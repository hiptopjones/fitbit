public class AlarmEntry
{
    public int index;
    public int secondsPastMidnight;
    public boolean isInPM;
    public int dayFlags;

    public String toString()
    {
        int minutes = (this.secondsPastMidnight % 3600) / 60;
        int hours = this.secondsPastMidnight / 3600;

        StringBuilder builder = new StringBuilder();

        builder.append("Time: ");
        builder.append(hours < 10 ? "0" : "");
        builder.append(hours);
        builder.append(":");
        builder.append(minutes < 10 ? "0" : "");
        builder.append(minutes);

        // If bit 7 is set, then one or more day bits should be set
        // If bit 7 is not set, then only one day bit should be set
        if (BinaryUtils.isBitSet(dayFlags, 7))
            builder.append(" repeats ");

        if (BinaryUtils.isBitSet(dayFlags, 0))
            builder.append(" M ");
        if (BinaryUtils.isBitSet(dayFlags, 1))
            builder.append(" Tu ");
        if (BinaryUtils.isBitSet(dayFlags, 2))
            builder.append(" W ");
        if (BinaryUtils.isBitSet(dayFlags, 3))
            builder.append(" Th ");
        if (BinaryUtils.isBitSet(dayFlags, 4))
            builder.append(" F ");
        if (BinaryUtils.isBitSet(dayFlags, 5))
            builder.append(" Sa ");
        if (BinaryUtils.isBitSet(dayFlags, 6))
            builder.append(" Su ");

        return builder.toString();
    }
}
