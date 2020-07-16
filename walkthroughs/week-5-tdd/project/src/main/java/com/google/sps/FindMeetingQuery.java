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

/**
* The FindMeetingQuery class represents a meeting that we want to schedule given
* the TimeRanges of events. 
*/
public final class FindMeetingQuery {
  
  public ArrayList<TimeRange> gettingCalendar(
                                                ArrayList<TimeRange> allTimesRequiredAttendees,
                                                ArrayList<TimeRange> notAvailableTimes,
                                                ArrayList<TimeRange> availableTimes,
                                              ) {
    TimeRange current = allTimesRequiredAttendees.get(0);
    for (TimeRange time: allTimesRequiredAttendees) {
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

    if (firstHalf.duration() >= request.duration()) {
      availableTimes.add(firstHalf);
    }
    if (secondHalf.duration() >= request.duration()) {
      availableTimes.add(secondHalf);
    }
    if (numberOfTimes > 1) {
      for (int j = 0; j < numberOfTimes - 1; j++) {
        TimeRange newTime = TimeRange.fromStartEnd(notAvailableTimes.get(j).end(), notAvailableTimes.get(j + 1).start(), false);
        if (newTime.duration() >= request.duration()) {
          availableTimes.add(newTime);
        }
      }
    }
    return availableTimes;                              
  }

  /**
  * Takes in a collection of Events and a MeetingRequest and is expected to return
  * a collection of TimeRanges in which the Meeting Request can be satisfied given the
  * the constraints posed by Events.
  */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    ArrayList<String> attendeesWithoutEvents = new ArrayList<>(request.getAttendees());
    int initialAttendeeCount = attendeesWithoutEvents.size();
    ArrayList<String> optionalAttendeesWithoutEvents = new ArrayList<>(request.getOptionalAttendees());
    int initialOptionalAttendeeCount = optionalAttendeesWithoutEvents.size();
    
    // Stores all TimeRanges of mandatory attendees with no repetitions.
    Collection<TimeRange> timesRequiredAttendees = new HashSet<>();
    // Stores all TimeRanges of optional attendees with no repetitions.
    Collection<TimeRange> timesOptionalAttendees = new HashSet<>();
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
      
    for (Event event : events) {
      for (String attendee : event.getAttendees()) {
        if (attendeesWithoutEvents.contains(attendee)) {
          timesRequiredAttendees.add(event.getWhen());
          toDeleteMan.add(attendee);
        }
        if (optionalAttendeesWithoutEvents.contains(attendee)) {
          timesOptionalAttendees.add(event.getWhen());
          toDeleteOpt.add(attendee);
        }
      }
    }
    attendeesWithoutEvents.removeAll(toDeleteMan);
    optionalAttendeesWithoutEvents.removeAll(toDeleteOpt);
    
    // Transforming collections of TimeRanges into an ArrayList to allow indexing.
    ArrayList<TimeRange> allTimesRequiredAttendees = new ArrayList<>(timesRequiredAttendees);
    ArrayList<TimeRange> allTimesOptionalAttendees = new ArrayList<>(timesOptionalAttendees);
    Collections.sort(allTimesRequiredAttendees, TimeRange.ORDER_BY_START);
    Collections.sort(allTimesOptionalAttendees, TimeRange.ORDER_BY_START);

    // No events conflict with their schedules.
    if (attendeesWithoutEvents.size() == initialAttendeeCount && optionalAttendeesWithoutEvents.size() == initialOptionalAttendeeCount) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    if (!allTimesRequiredAttendees.isEmpty()) {
      ArrayList<TimeRange> finalCalendar = gettingCalendar(allTimesRequiredAttendees, notAvailableTimes, availableTimes);
      Collections.sort(finalCalendar, TimeRange.ORDER_BY_START);
      ArrayList<TimeRange> calendar = new ArrayList<>(finalCalendar);
    
      for (TimeRange opt : allTimesOptionalAttendees) {
        for (TimeRange permanent : finalCalendar) {
          if (permanent.overlaps(opt)) {
            calendar.remove(permanent);
          }
        }
      }
      calendar.isEmpty() ? finalCalendar : calendar;
    } else {
      ArrayList<TimeRange> finalOptionalCalendar = gettingCalendar(
                                                                    allTimesOptionalAttendees, 
                                                                    notOptionalTimes, 
                                                                    optionalCalendar, 
                                                                  );
      Collections.sort(finalOptionalCalendar, TimeRange.ORDER_BY_START);
      return finalOptionalCalendar;
    }
  }
}
