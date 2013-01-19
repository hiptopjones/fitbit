import java.util.*;

public class MicroDumpSyncPacket extends SyncPacket
{
    public static MicroDumpSyncPacket createInstance()
    {
        MicroDumpSyncPacket packet = new MicroDumpSyncPacket();
        return packet;
    }

    public MicroDumpSyncPacket()
    {
        this.packetType = SyncPacketType.MicroDump;
    }

    public void parse()
    {
        // some stuff
    }
}
