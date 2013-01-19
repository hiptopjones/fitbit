import java.util.*;

// MegaDump packet looks like this:
//  - six major sections, separated by 'C0DBDCDD'
//     - first section contains configuration info
//     - second section contains ??
//     - third section contains steps minute-wise data
//     - fourth section contains floors (in feet) minute-wise data
//     - fifth section contains summaries?
//     - sixth section contains alarms, ??
//  - steps data:
//     - starts with a timestamp
//     - 4 bytes of data per minute (81, calories?, steps, activity?)
//     - appears that if the user goes idle, we stop adding entries
//     - when the user moves, another timestamp is added to sync the subsequent entries
//  - floors data:
//     - starts with a timestamp
//     - 2 bytes of data per minute (80, elevation in feet - looks like 10 feet equals one floor)
//     - same idea as steps with idleness then timestamp
//  - summaries data:
//     - starts with a timestamp
//     - 2 bytes containing calories (what units?)
//     - 4 bytes containing number of steps
//     - 4 bytes containing distance travelled in mm
//     - 2 bytes containing number floors (in feet)
//     - another timestamp, then repeating above
//  - alarms data:
public class MegaDumpSyncPacket extends SyncPacket
{
    public static MegaDumpSyncPacket createInstance()
    {
        MegaDumpSyncPacket packet = new MegaDumpSyncPacket();
        return packet;
    }

    public MegaDumpSyncPacket()
    {
        this.packetType = SyncPacketType.MegaDump;
    }

    public void parse()
    {
        this.parsePersonalMetrics(40);
        this.parseDisplayConfiguration(48);
        this.parseGreetingAndChatter(60);
        //this.parseAlarms(105, 120);

        // ad hoc

        int byte32 = BinaryUtils.toInt(this.bytes[32]);
        this.sequenceNum = BinaryUtils.getBitRange(byte32, 0, 3); 

        // This seems to maybe be the time of the last sync
        // (or maybe it's just the start of the series time after the last sync?)
        this.lastSyncTime = this.parseDateBigEndian(143);
    }

    protected void parseAlarms(int startIndex)
    {
//        int byte105 = BinaryUtils.toInt(this.bytes[105]);
//        this.isAlarmDataPresent = BinaryUtils.isBitSet(byte105, 0); 
//
//        if (this.isAlarmDataPresent)
//        {
//            ArrayList<AlarmEntry> alarmList = new ArrayList<AlarmEntry>();
//
//            int alarmStartIndex = startIndex;
//
//            while (true)
//            {
//                // TODO: How do we know we're done?
//
//                this.numAlarms++;
//
//                AlarmEntry alarm = new AlarmEntry();
//
//                alarm.secondsPastMidnight = BinaryUtils.composeInt((byte)0, (byte)0, this.bytes[alarmStartIndex+1], this.bytes[alarmStartIndex]);
//                alarm.dayFlags = BinaryUtils.toInt(this.bytes[alarmStartIndex + 7]);
//                alarm.index = BinaryUtils.toInt(this.bytes[alarmStartIndex + 31]);
//
//                alarmList.add(alarm);
//
//                alarmStartIndex += 28;
//            }
//
//            this.alarms = new AlarmEntry[alarmList.size()];
//            this.alarms = alarmList.toArray(this.alarms);
//        }
    }
}

