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

// MegaDump packet looks like this:
//  - six major sections, separated by 'C0DBDCDD'
//     - first section contains configuration info
//     - second section contains ??
//     - third section contains steps minute-wise data
//     - fourth section contains floors (in feet) minute-wise data
//     - fifth section contains summaries?
//     - sixth section contains alarms, ??
//  - steps++ data:
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
//     - 2 bytes containing number of seconds past midnight for the alarm
//     - 1 byte containing a bitfield of whether to repat and what days of the week
//     - 1 byte containing an index
//     - what are the other bytes in these entries?
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
        Integer[] sectionIndexes = this.findSectionStartIndexes();

        // TODO: What is the second section?
        this.parseDeviceInfo(sectionIndexes[0]);
        //this.parseSomething(sectionIndexes[1]);
        this.parseMotionSeries(sectionIndexes[2]);
        this.parseElevationSeries(sectionIndexes[3]);
        this.parseSummaryData(sectionIndexes[4]);
        this.parseAlarmEntries(sectionIndexes[5]);

        // ad hoc

        // TODO: Not convinced this is a sequence
        int byte32 = BinaryUtils.toInt(this.bytes[32]);
        this.sequenceNum = BinaryUtils.getBitRange(byte32, 0, 3); 

        // This seems to maybe be the time of the last sync
        // (or maybe it's just the start of the series time after the last sync?)
        this.lastSyncTime = this.parseDateBigEndian(143);
    }

    private void parseDeviceInfo(int startIndex)
    {
        this.parsePersonalMetrics(startIndex + 40);
        this.parseDisplayConfiguration(startIndex + 48);
        this.parseGreetingAndChatter(startIndex + 60);
    }

    private void parseMotionSeries(int startIndex)
    {
        // TODO: Parse the other two bytes in these entries

        this.stepsCollection = new TreeMap<Date, List<Integer>>();

        LinkedList<Integer> stepsList = null;

        int step = 4;

        // Steps are two byte entries (0x80, 0x??)
        for (int i = startIndex; this.bytes[i] != 0xC0; i += step)
        {
            // The step may get changed if we get a random byte, but
            // we leave it as 4 most of the time.
            step = 4;

            if (this.bytes[i] == (byte)0x81)
            {
                stepsList.add(BinaryUtils.toInt(this.bytes[i + 2]));
            }
            else if (this.bytes[i] == (byte)0xC0)
            {
                break;
            }
            else if (BinaryUtils.isBitSet(this.bytes[i], 7))
            {
                // TODO: WTF? Why are there random bytes in there?
                // TODO: Is this marking the difference between walking and running?

                // Adjust the step to just skip the one byte
                step = 1;
            }
            else
            {
                Date date = this.parseDateLittleEndian(i);
                stepsList = new LinkedList<Integer>();

                this.stepsCollection.put(date, stepsList);
            }
        }
    }

    private void parseElevationSeries(int startIndex)
    {
        this.elevationsCollection = new TreeMap<Date, List<Integer>>();

        LinkedList<Integer> elevationsList = null;

        // Elevations are two-byte entries (0x80, 0x??), where 0x?? is in feet (10 per floor)
        // Dates are four-byte entries.  Need to use a different step, depending on what's being parsed.
        int step = 4;

        for (int i = startIndex; this.bytes[i] != 0xC0; i += step)
        {
            if (this.bytes[i] == (byte)0x80)
            {
                // Start stepping by two bytes as long as we're parsing elevation data
                step = 2;

                elevationsList.add(BinaryUtils.toInt(this.bytes[i + 1]));
            }
            else if (this.bytes[i] == (byte)0xC0)
            {
                break;
            }
            else
            {
                // Found a date, so step by four bytes again
                step = 4;

                Date date = this.parseDateLittleEndian(i);
                elevationsList = new LinkedList<Integer>();

                this.elevationsCollection.put(date, elevationsList);
            }
        }
    }

    private void parseSummaryData(int startIndex)
    {
    }

    private void parseAlarmEntries(int startIndex)
    {
        // These alarms are basically the same as those in the server response
        // but there are a few extra bytes in there.

        // TODO: How do we know how many alarms there are?
    }

    protected Integer[] findSectionStartIndexes()
    {
        ArrayList<Integer> sectionIndexList = new ArrayList<Integer>();
        sectionIndexList.add(0);

        // Look for C0DBDCDD
        for (int i = 0; i < (this.bytes.length - 4); i++)
        {
            if (this.bytes[i] == (byte)0xC0 && this.bytes[i + 1] == (byte)0xDB && this.bytes[i + 2] == (byte)0xDC && this.bytes[i + 3] == (byte)0xDD)
            {
                sectionIndexList.add(i + 4);
            }
        }

        Integer[] sectionIndexes = new Integer[sectionIndexList.size()];
        return sectionIndexList.toArray(sectionIndexes);
    }
}
