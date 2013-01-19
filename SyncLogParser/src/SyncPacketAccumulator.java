import java.io.*;
import java.util.*;
import java.text.*;

public class SyncPacketAccumulator
{
    public StringBuilder builder = new StringBuilder();;

    public int index;
    public String timestamp;
    public boolean forcedSync;

    public void appendHex(String hexString)
    {
        this.builder.append(hexString);
    }

    public SyncPacket createPacket()
    {
        // Get the binary data
        byte[] bytes = BinaryUtils.hexStringToByteArray(this.builder.toString());

        int packetType = 0;

        if (bytes[0] == 0x26)
        {
            packetType = SyncPacketType.MegaDump;
        }
        else if (bytes[0] == 0x30)
        {
            packetType = SyncPacketType.MicroDump;
        }

        if (packetType == SyncPacketType.MegaDump && BinaryUtils.isBitSet(bytes[6], 0))
        {
            packetType = SyncPacketType.ServerResponse;
        }

        SyncPacket packet = null;

        switch (packetType)
        {
            case SyncPacketType.MegaDump:
                packet = MegaDumpSyncPacket.createInstance();
                break;

            case SyncPacketType.MicroDump:
                packet = MicroDumpSyncPacket.createInstance();
                break;

            case SyncPacketType.ServerResponse:
                packet = MegaDumpServerResponsePacket.createInstance();
                break;

            default:
                System.out.println("Unknown packet type: " + packetType);
                return null;
        }

        packet.index = this.index;
        packet.timestamp = this.timestamp;
        packet.forcedSync = this.forcedSync;
        packet.bytes = bytes;
        
        packet.parse();

        // TODO: handle parsed packet
        // Only dump the packet if this is the type we're inspecting
//        if ((packet.packetType & this.readPacketTypes) != 0)
        {
            packet.dump();
        }

        return packet;
    }
}

