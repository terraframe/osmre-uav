/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.geoplatform.uasdm.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Range
{
  private Long start;

  private Long end;

  private Long suffixLength;

  public Long getStart()
  {
    return start;
  }

  public void setStart(Long start)
  {
    this.start = start;
  }

  public Long getEnd()
  {
    return end;
  }

  public void setEnd(Long end)
  {
    this.end = end;
  }

  public Long getSuffixLength()
  {
    return suffixLength;
  }

  public void setSuffixLength(Long suffixLength)
  {
    this.suffixLength = suffixLength;
  }

  public static List<Range> decodeRange(String rangeHeader)
  {
    List<Range> ranges = new ArrayList<>();
    String byteRangeSetRegex = "(((?<byteRangeSpec>(?<firstBytePos>\\d+)-(?<lastBytePos>\\d+)?)|(?<suffixByteRangeSpec>-(?<suffixLength>\\d+)))(,|$))";
    String byteRangesSpecifierRegex = "bytes=(?<byteRangeSet>" + byteRangeSetRegex + "{1,})";

    Pattern byteRangeSetPattern = Pattern.compile(byteRangeSetRegex);
    Pattern byteRangesSpecifierPattern = Pattern.compile(byteRangesSpecifierRegex);
    Matcher byteRangesSpecifierMatcher = byteRangesSpecifierPattern.matcher(rangeHeader);

    if (byteRangesSpecifierMatcher.matches())
    {
      String byteRangeSet = byteRangesSpecifierMatcher.group("byteRangeSet");
      Matcher byteRangeSetMatcher = byteRangeSetPattern.matcher(byteRangeSet);

      while (byteRangeSetMatcher.find())
      {
        Range range = new Range();

        if (byteRangeSetMatcher.group("byteRangeSpec") != null)
        {
          String start = byteRangeSetMatcher.group("firstBytePos");
          String end = byteRangeSetMatcher.group("lastBytePos");
          range.start = Long.valueOf(start);
          range.end = end == null ? null : Long.valueOf(end);
        }
        else if (byteRangeSetMatcher.group("suffixByteRangeSpec") != null)
        {
          range.suffixLength = Long.valueOf(byteRangeSetMatcher.group("suffixLength"));
        }
        else
        {
          throw new InvalidRangeException(rangeHeader);
        }

        ranges.add(range);
      }
    }
    else
    {
      throw new InvalidRangeException(rangeHeader);
    }

    return ranges;
  }
}
