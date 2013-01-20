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
        this.parseAlarmEntries(120);

        // ad hoc

        //int byte32 = BinaryUtils.toInt(this.bytes[32]);
        //this.sequenceNum = BinaryUtils.getBitRange(byte32, 0, 3); 

        // This seems to maybe be the time of the last sync
        // (or maybe it's just the start of the series time after the last sync?)
        //this.lastSyncTime = this.parseDateBigEndian(143);
    }

    protected void parseAlarmEntries(int startIndex)
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
