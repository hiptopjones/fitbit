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
        
        if (args.length > 0)
        {
            if (args[0] == "-megadump")
            {
                logParser.readPacketTypes = SyncPacketType.MegaDump;
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
