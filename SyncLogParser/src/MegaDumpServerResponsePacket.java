import java.util.*;

public class MegaDumpServerResponsePacket extends SyncPacket
{
    public static MegaDumpServerResponsePacket createInstance()
    {
        MegaDumpServerResponsePacket packet = new MegaDumpServerResponsePacket();
        return packet;
    }

    public MegaDumpServerResponsePacket()
    {
        this.packetType = SyncPacketType.ServerResponse;
    }

    public void parse()
    {
        this.parsePersonalMetrics(14);
        this.parseDisplayConfiguration(22);
        this.parseGreetingAndChatter(34);
        this.parseAlarms(120);

        // ad hoc

        //int byte32 = BinaryUtils.toInt(this.bytes[32]);
        //this.sequenceNum = BinaryUtils.getBitRange(byte32, 0, 3); 

        // This seems to maybe be the time of the last sync
        // (or maybe it's just the start of the series time after the last sync?)
        //this.lastSyncTime = this.parseDateBigEndian(143);
    }

    protected void parseAlarms(int startIndex)
    {
        int byte79 = BinaryUtils.toInt(this.bytes[79]);
        this.isAlarmDataPresent = BinaryUtils.isBitSet(byte79, 0); 

        if (this.isAlarmDataPresent)
        {
            this.numAlarms = BinaryUtils.toInt(this.bytes[startIndex]);
            if (this.numAlarms > 0)
            {
                this.alarms = new AlarmEntry[this.numAlarms];

                int alarmStartIndex = startIndex + 4;
                for (int i = 0; i < this.numAlarms; i++)
                {
                    this.alarms[i] = new AlarmEntry();

                    this.alarms[i].index = BinaryUtils.toInt(this.bytes[alarmStartIndex + 23]);
                    this.alarms[i].secondsPastMidnight = BinaryUtils.composeInt((byte)0, (byte)0, this.bytes[alarmStartIndex + 1], this.bytes[alarmStartIndex]);
                    this.alarms[i].dayFlags = BinaryUtils.toInt(this.bytes[alarmStartIndex + 7]);

                    // Each alarm record is 24 bytes long
                    alarmStartIndex += 24;
                }
            }
        }
    }
}
