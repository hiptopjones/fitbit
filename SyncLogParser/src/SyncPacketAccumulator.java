// Copyright (c) 2013, Peter James
// All rights reserved.
// 
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//     * Redistributions of source code must retain the above copyright
//       notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above copyright
//       notice, this list of conditions and the following disclaimer in the
//       documentation and/or other materials provided with the distribution.
//     * Neither the name of the <organization> nor the
//       names of its contributors may be used to endorse or promote products
//       derived from this software without specific prior written permission.
// 
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

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

