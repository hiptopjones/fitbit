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

public class AlarmEntry
{
    public int index;
    public int secondsPastMidnight;
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
