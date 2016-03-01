/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.analytics.esb.util;

import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.joda.time.MutableDateTime;
import org.joda.time.Period;
import org.wso2.carbon.analytics.esb.bean.TimeRange;

import java.util.ArrayList;
import java.util.List;

public class TimeRangeUtils {

    private static final int INTERVAL = 10;

    private TimeRangeUtils() {
    }

    public RangeUnit getSuitableTimeRangeUnit(long from, long to) {
        Interval interval = new Interval(from, to);
        Period period = interval.toPeriod();
        RangeUnit range = null;
        if (period.getMonths() >= INTERVAL) {
            range = RangeUnit.MONTH;
        } else if (period.getDays() >= INTERVAL) {
            range = RangeUnit.DAY;
        } else if (period.getHours() >= INTERVAL) {
            range = RangeUnit.HOUR;
        } else if (period.getMinutes() >= INTERVAL) {
            range = RangeUnit.MINUTE;
        } else {
            range = RangeUnit.SECOND;
        }
        return range;
    }

    public List<TimeRange> getDateTimeRanges(long from, long to) {
        List<TimeRange> ranges = new ArrayList<>(10);
        MutableDateTime fromDate = new MutableDateTime(from);
        MutableDateTime toDate = new MutableDateTime(to);
        MutableDateTime tempFromTime = fromDate.copy();
        MutableDateTime tempToTime = toDate.copy();

        if (toDate.getMillis() - fromDate.getMillis() < DateTimeConstants.MILLIS_PER_MINUTE) {
            ranges.add(new TimeRange(RangeUnit.SECOND, new long[]{fromDate.getMillis(), toDate.getMillis()}));
        }
        if (tempFromTime.getSecondOfMinute() != 0 && (toDate.getMillis() - fromDate.getMillis() > DateTimeConstants.MILLIS_PER_MINUTE)) {
            tempFromTime = tempFromTime.minuteOfHour().roundCeiling();
            ranges.add(new TimeRange(RangeUnit.SECOND, new long[]{fromDate.getMillis(), tempFromTime.getMillis()}));
        }
        if (tempFromTime.getMinuteOfHour() != 0 &&
            ((toDate.getMillis() - tempFromTime.getMillis()) >= DateTimeConstants.MILLIS_PER_MINUTE)) {
            fromDate = tempFromTime.copy();
            if (((toDate.getMillis() - tempFromTime.getMillis()) / DateTimeConstants.MILLIS_PER_MINUTE) < 60) {
                tempFromTime = tempFromTime.minuteOfHour().add((toDate.getMillis() - tempFromTime.getMillis()) / DateTimeConstants.MILLIS_PER_MINUTE);
            } else {
                tempFromTime = tempFromTime.hourOfDay().roundCeiling();
            }
            ranges.add(new TimeRange(RangeUnit.MINUTE, new long[]{fromDate.getMillis(), tempFromTime.getMillis()}));
        }
        if (tempFromTime.getHourOfDay() != 0 &&
            ((toDate.getMillis() - tempFromTime.getMillis()) >= DateTimeConstants.MILLIS_PER_HOUR)) {
            fromDate = tempFromTime.copy();
            if (((toDate.getMillis() - tempFromTime.getMillis()) / DateTimeConstants.MILLIS_PER_HOUR) < 24) {
                tempFromTime = tempFromTime.hourOfDay().add((toDate.getMillis() - tempFromTime.getMillis()) /
                                                            DateTimeConstants.MILLIS_PER_HOUR);
            } else {
                tempFromTime = tempFromTime.dayOfMonth().roundCeiling();
            }
            ranges.add(new TimeRange(RangeUnit.HOUR, new long[]{fromDate.getMillis(), tempFromTime.getMillis()}));
        }
        if (tempFromTime.getDayOfMonth() != 1 &&
            ((toDate.getMillis() - tempFromTime.getMillis()) >= DateTimeConstants.MILLIS_PER_DAY)) {
            fromDate = tempFromTime.copy();
            if ((((toDate.getMillis() - tempFromTime.getMillis()) / DateTimeConstants.MILLIS_PER_DAY)) < tempFromTime
                    .dayOfMonth().getMaximumValue()) {
                tempFromTime = tempFromTime.dayOfMonth().add(((toDate.getMillis() - tempFromTime.getMillis()) /
                                                              ((long) DateTimeConstants.MILLIS_PER_DAY)));
            } else {
                tempFromTime = tempFromTime.monthOfYear().roundCeiling();
            }
            ranges.add(new TimeRange(RangeUnit.DAY, new long[]{fromDate.getMillis(), tempFromTime.getMillis()}));
        }
        if (tempToTime.getSecondOfMinute() != 0 &&
            (tempToTime.getMillis() - tempFromTime.getMillis()) >= DateTimeConstants.MILLIS_PER_SECOND) {
            toDate = tempToTime.copy();
            long remainingSeconds = ((toDate.getMillis() - tempFromTime.getMillis()) % DateTimeConstants
                    .MILLIS_PER_MINUTE) / DateTimeConstants.MILLIS_PER_SECOND;
            if (remainingSeconds < 60) {
                tempToTime = tempToTime.secondOfMinute().add(-1 * remainingSeconds);

            } else {
                tempToTime = tempToTime.secondOfMinute().roundFloor();
            }
            ranges.add(new TimeRange(RangeUnit.SECOND, new long[]{tempToTime.getMillis(), toDate.getMillis()}));
        }
        if (tempToTime.getMinuteOfHour() != 0 &&
            ((tempToTime.getMillis() - tempFromTime.getMillis()) >= DateTimeConstants.MILLIS_PER_MINUTE)) {
            toDate = tempToTime.copy();
            long remainingMinutes = ((toDate.getMillis() - tempFromTime.getMillis()) % DateTimeConstants
                    .MILLIS_PER_HOUR) / DateTimeConstants.MILLIS_PER_MINUTE;
            if (remainingMinutes < 60) {
                tempToTime = tempToTime.minuteOfHour().add(-1 * remainingMinutes);
            } else {
                tempToTime = tempToTime.hourOfDay().roundFloor();
            }
            ranges.add(new TimeRange(RangeUnit.MINUTE, new long[]{tempToTime.getMillis(), toDate.getMillis()}));
        }
        if (tempToTime.getHourOfDay() != 0 &&
            ((tempToTime.getMillis() - tempFromTime.getMillis()) >= DateTimeConstants.MILLIS_PER_HOUR)) {
            toDate = tempToTime.copy();
            long remainingHours = ((toDate.getMillis() - tempFromTime.getMillis()) % DateTimeConstants
                    .MILLIS_PER_DAY) / DateTimeConstants.MILLIS_PER_HOUR;
            if (remainingHours < 24) {
                tempToTime = tempToTime.hourOfDay().add(-1 * remainingHours);
            } else {
                tempToTime = tempToTime.dayOfMonth().roundFloor();
            }
            ranges.add(new TimeRange(RangeUnit.HOUR, new long[]{tempToTime.getMillis(), toDate.getMillis()}));
        }
        if (tempToTime.getDayOfMonth() != 1 &&
            ((tempToTime.getMillis() - tempFromTime.getMillis()) >= DateTimeConstants.MILLIS_PER_DAY)) {
            toDate = tempToTime.copy();
            tempToTime = tempToTime.monthOfYear().roundFloor();
            ranges.add(new TimeRange(RangeUnit.DAY, new long[]{tempToTime.getMillis(), toDate.getMillis()}));
        }
        if (tempToTime.isAfter(tempFromTime)) {
            ranges.add(new TimeRange(RangeUnit.MONTH, new long[]{tempFromTime.getMillis(), tempToTime.getMillis()}));
        }
        return ranges;
    }
}
