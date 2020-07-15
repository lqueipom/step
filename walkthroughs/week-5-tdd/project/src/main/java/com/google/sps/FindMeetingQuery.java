// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashSet;
import java.util.Arrays;

// Meeting request: duration and attendes. 
// Event: Time range, name of event, attendes of event. 
public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    
    // Duration of meeting is more than allowed.

    ArrayList<String> attendees = new ArrayList<>(request.getAttendees());
    int amountAttendees = attendees.size();
    ArrayList<String> optionalAttendees = new ArrayList<>(request.getOptionalAttendees());
    int amountOptional = optionalAttendees.size();
    long duration = request.getDuration();
    
    Collection<TimeRange> notRepeatedMan = new HashSet<>();
    Collection<TimeRange> notRepeatedOpt = new HashSet<>();
    ArrayList<TimeRange> notAvailableTimes = new ArrayList<>();
    ArrayList<TimeRange> notOptionalTimes = new ArrayList<>();
    ArrayList<TimeRange> availableTimes = new ArrayList<>();
    ArrayList<TimeRange> optionalCalendar = new ArrayList<>();

    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }

    // No events to conflict with.
    if (events.size() == 0) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    ArrayList<String> toDeleteMan = new ArrayList<>();
    ArrayList<String> toDeleteOpt = new ArrayList<>();
    for (Event event:events) {

      for (String attendee:event.getAttendees()) {
        if (attendees.contains(attendee)) {
          notRepeatedMan.add(event.getWhen());
          toDeleteMan.add(attendee);
        }
        if (optionalAttendees.contains(attendee)) {
          notRepeatedOpt.add(event.getWhen());
          toDeleteOpt.add(attendee);
        }
      }
    }
    attendees.removeAll(toDeleteMan);
    optionalAttendees.removeAll(toDeleteOpt);

    ArrayList<TimeRange> timeBlocks = new ArrayList<>(notRepeatedMan);
    ArrayList<TimeRange> optionalTimeBlocks = new ArrayList<>(notRepeatedOpt);
    Collections.sort(timeBlocks, TimeRange.ORDER_BY_START);
    Collections.sort(optionalTimeBlocks, TimeRange.ORDER_BY_START);

    // No events conflict with their schedules.
    if (attendees.size() == amountAttendees && optionalAttendees.size() == amountOptional) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    
    if (timeBlocks.isEmpty() == false) {
      TimeRange current = timeBlocks.get(0);
      for (TimeRange time: timeBlocks) {

        if (time.overlaps(current)) {
          int start = current.start();
          if (time.end() < current.end()) {
            current = TimeRange.fromStartEnd(start, current.end(), false);
          } else {
            current = TimeRange.fromStartEnd(start, time.end(), false);
          }

        } else {
          notAvailableTimes.add(current);
          current = time;
        }
      }
      // Adding last one, we now have the full calendar of not available time ranges. 
      notAvailableTimes.add(current);
      int numberOfTimes = notAvailableTimes.size();
      TimeRange first = notAvailableTimes.get(0);
      TimeRange last = notAvailableTimes.get(numberOfTimes-1);

      TimeRange firstHalf = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, first.start(), false);
      TimeRange secondHalf = TimeRange.fromStartEnd(last.end(), TimeRange.END_OF_DAY, true);

      if (firstHalf.duration() >= duration) {
        availableTimes.add(firstHalf);
      }
      if (secondHalf.duration() >= duration) {
        availableTimes.add(secondHalf);
      }
        
      if (numberOfTimes > 1) {

        for (int j = 0; j < numberOfTimes - 1; j++) {
          TimeRange newTime = TimeRange.fromStartEnd(notAvailableTimes.get(j).end(), notAvailableTimes.get(j + 1).start(), false);
          if (newTime.duration() >= duration) {
            availableTimes.add(newTime);
          }
        }
      }
      Collections.sort(availableTimes, TimeRange.ORDER_BY_START);
      ArrayList<TimeRange> calendar = new ArrayList<>(availableTimes);
      for (TimeRange opt: optionalTimeBlocks) {
        for (TimeRange permanent: availableTimes) {
          if (permanent.overlaps(opt)) {
            calendar.remove(permanent);
          }
        }
      }
      if (calendar.isEmpty()) {
        return availableTimes;
      } else {
        return calendar;
      }

    } else {

      TimeRange current = optionalTimeBlocks.get(0);
      for (TimeRange time:optionalTimeBlocks) {

        if (time.overlaps(current)) {
          int start = current.start();
    
          if (time.end() < current.end()) {
            current = TimeRange.fromStartEnd(start, current.end(), false);
          } else {
            current = TimeRange.fromStartEnd(start, time.end(), false);
          }

        } else {
          notOptionalTimes.add(current);
          current = time;
        }
      }
      // Adding last one, we now have the full calendar of not available time ranges. 
      notOptionalTimes.add(current);
      int numberOfTimes = notOptionalTimes.size();
      TimeRange first = notOptionalTimes.get(0);
      TimeRange last = notOptionalTimes.get(numberOfTimes-1);
      TimeRange firstHalf = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, first.start(), false);
      TimeRange secondHalf = TimeRange.fromStartEnd(last.end(), TimeRange.END_OF_DAY, true);

      if (firstHalf.duration() >= duration) {
        optionalCalendar.add(firstHalf);
      }
      if (secondHalf.duration() >= duration) {
        optionalCalendar.add(secondHalf);
      }
      if (numberOfTimes > 1) {

        for (int j = 0; j < numberOfTimes - 1; j++) {
          TimeRange newTime = TimeRange.fromStartEnd(notOptionalTimes.get(j).end(), notOptionalTimes.get(j + 1).start(), false);
          if (newTime.duration() >= duration) {
            optionalCalendar.add(newTime);
          }
        }
      }     
      Collections.sort(optionalCalendar, TimeRange.ORDER_BY_START);
      return optionalCalendar;
    }
  }
}
