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

class SyncLogParser
{
    private int packetCount = 0;

    private SyncPacketAccumulator accumulator = null;

    private int readPacketTypes = SyncPacketType.MicroDump | SyncPacketType.MegaDump | SyncPacketType.ServerResponse;

    public static void main(String[] args)
    {
        SyncLogParser logParser = new SyncLogParser();
        
        for (int i = 0; i < args.length; i++)
        {
            if (args[i] == "-packetType")
            {
                logParser.readPacketTypes = SyncPacketType.None;

                int itmp = i + 1;
                while (itmp < args.length && !args[itmp].startsWith("-"))
                {
                    if (args[itmp].toLowerCase().equals("megadump"))
                    {
                        logParser.readPacketTypes |= SyncPacketType.MegaDump;
                    }
                    else if (args[itmp].toLowerCase().equals("microdump"))
                    {
                        logParser.readPacketTypes |= SyncPacketType.MicroDump;
                    }
                    else if (args[itmp].toLowerCase().equals("server"))
                    {
                        logParser.readPacketTypes |= SyncPacketType.ServerResponse;
                    }

                    itmp++;
                }

                i = itmp - 1;
            }
        }

        logParser.start();
    }

    private void start()
    {
        // Should we wait for more input (tail), or exit when the input runs out?
        boolean tailLog = false;

        try
        {
            String currentLine = null;
            String lastNonEmptyLine = null;

            String currentPacketTimeStamp = null;

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while (true)
            {
                if (currentLine != null && currentLine.length() > 0)
                {
                    lastNonEmptyLine = currentLine;
                }

                currentLine = reader.readLine();

                // No data, wait some more
                if (currentLine == null)
                {
                    if (tailLog)
                    {
                        Thread.sleep(1000);
                        continue;
                    }
                    else
                    {
                        // If we're currently reading a packet, hand it off
                        if (this.accumulator != null)
                        {
                            this.accumulator.createPacket();
                        }

                        return;
                    }
                }

                currentLine = currentLine.trim();

                if (this.accumulator == null)
                {
                    if (this.isPacketLine(currentLine))
                    {
                        this.accumulator = new SyncPacketAccumulator();

                        this.accumulator.index = this.packetCount++;
                        this.accumulator.timestamp = this.getSyncLogTimeStamp(lastNonEmptyLine);
                        this.accumulator.forcedSync = !lastNonEmptyLine.contains("MegaDump");
                    }
                }

                if (this.accumulator != null)
                {
                    if (!this.isPacketLine(currentLine))
                    {
                        this.accumulator.createPacket();

                        // clear for the next round
                        this.accumulator = null;
                        continue;
                    }
                    
                    // Add the current line to our accumulator
                    this.accumulator.appendHex(currentLine);
                }
            }
        }
        catch (Exception ex)
        {
            System.out.println("Caught exception: " + ex.toString());
            ex.printStackTrace();
        }
    }

    private boolean isPacketLine(String line)
    {
        if (line.isEmpty())
        {
            return false;
        }

        if (this.accumulator == null && line.length() < 32)
        {
            return false;
        }

        for (char c : line.toCharArray())
        {
            if ((c < '0' || c > '9') && (c < 'A' || c > 'F'))
            {
                return false;
            }
        }

        return true;
    }

    private String getSyncLogTimeStamp(String line)
    {
        int startIndex = 0;
        int endIndex = line.indexOf('[');

        return line.substring(startIndex, endIndex).trim();
    }
}
