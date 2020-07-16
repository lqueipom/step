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
  
  /**
  * Returns an unsorted ArrayList of TimeRanges that represent the times a group of attendees
  * can meet.
  */
  private ArrayList<TimeRange> getAvailability(ArrayList<TimeRange> busyTimesForChosenAttendees) {
    TimeRange current = busyTimesForChosenAttendees.get(0);
    ArrayList<TimeRange> busyTimesForChosenAttendeesNoOverlaps = new ArrayList<>();
    // Stores all TimeRanges in which attendees can meet.
    ArrayList<TimeRange> freeTimesForChosenAttendees = new ArrayList<>();
    
    for (TimeRange time : busyTimesForChosenAttendees) {
      // Checks whether our attendees have overlapping TimeRanges.
      if (time.overlaps(current)) {
        int start = current.start();
        current = TimeRange.fromStartEnd(start, time.end() < current.end() ? current.end() : time.end(), false);
      } else {
        // Adds TimeRange when they don't conflict with each other.
        busyTimesForChosenAttendeesNoOverlaps.add(current);
        current = time;
        }
    }
    // Adding last one, we now have the full calendar of not available time ranges. 
    busyTimesForChosenAttendeesNoOverlaps.add(current);
    TimeRange first = busyTimesForChosenAttendeesNoOverlaps.get(0);
    TimeRange last = busyTimesForChosenAttendeesNoOverlaps.get(busyTimesForChosenAttendeesNoOverlaps.size() - 1);
    
    // Creates TimeRanges that include the start/end of the day.
    TimeRange firstHalf = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, first.start(), false);
    TimeRange secondHalf = TimeRange.fromStartEnd(last.end(), TimeRange.END_OF_DAY, true);
    
    // Takes out TimeRanges that do not last enough time. 
    if (firstHalf.duration() >= request.duration()) {
      freeTimeForChosenAttendees.add(firstHalf);
    }
    if (secondHalf.duration() >= request.duration()) {
      freeTimesForChosenAttendees.add(secondHalf);
    }
    // Checks whether we have enough TimeRanges to iterate through
    if (busyTimesForChosenAttendeesNoOverlaps.size() > 1) {
      for (int j = 0; j < busyTimesForChosenAttendeesNoOverlaps.size() - 1; j++) {
        TimeRange newTime = TimeRange.fromStartEnd(busyTimesForChosenAttendeesNoOverlaps.get(j).end(), busyTimesForChosenAttendeesNoOverlaps.get(j + 1).start(), false);
        if (newTime.duration() >= request.duration()) {
          freeTimesForChosenAttendees.add(newTime);
        }
      }
    }
    Collections.sort(freeTimesForChosenAttendees, TimeRange.ORDER_BY_START);
    return freeTimesForChosenAttendees;                              
  }

  /**
  * Takes in a collection of Events and a MeetingRequest and is expected to return
  * a collection of TimeRanges in which the MeetingRequest can be satisfied given the
  * the constraints posed by the attendees' events. 
  */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) { 
    // Stores all TimeRanges of mandatory attendees with no repetitions.
    Collection<TimeRange> busyTimesForRequiredAttendees = new HashSet<>();
    // Stores all TimeRanges of optional attendees with no repetitions.
    Collection<TimeRange> busyTimesForOptionalAttendees = new HashSet<>();
    // Stores all TimeRanges in which mandatory attendees are busy with no overlaps.

    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }
    // No events to conflict with.
    if (events.size() == 0) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    ArrayList<String> attendeesWithoutEvents = new ArrayList<>(request.getAttendees());
    ArrayList<String> optionalAttendeesWithoutEvents = new ArrayList<>(request.getOptionalAttendees());
    
    for (Event event : events) {
      for (String attendee : event.getAttendees()) {
        if (request.getAttendees().contains(attendee)) {
          // Adds TimeRange if any mandatory attendee attends this event.
          busyTimesForRequiredAttendees.add(event.getWhen());
          // Deletes from list when attendee appears in an Event.
          attendeesWithoutEvents.remove(attendee);
        }
        if (request.getOptionalAttendees().contains(attendee)) {
          busyTimesForOptionalAttendees.add(event.getWhen());
          optionalAttendeesWithoutEvents.remove(attendee);
        }
      }
    }
    // Transforming collections of TimeRanges into an ArrayList to allow indexing.
    ArrayList<TimeRange> busyTimesForRequiredAttendeesList = new ArrayList<>(busyTimesForRequiredAttendees);
    ArrayList<TimeRange> busyTimesForOptionalAttendeesList = new ArrayList<>(busyTimesForOptionalAttendees);
    Collections.sort(busyTimesForRequiredAttendeesList, TimeRange.ORDER_BY_START);
    Collections.sort(busyTimesForOptionalAttendeesList, TimeRange.ORDER_BY_START);

    // No events conflict the schedules of the requested attendees. 
    if (attendeesWithoutEvents.size() == request.getAttendees().size() && optionalAttendeesWithoutEvents.size() == request.getOptionalAttendees().size()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    // Checks whether mandatory attendees have no restrictions in their schedules.
    if (busyTimesForRequiredAttendeesList.isEmpty()) {
      ArrayList<TimeRange> optionalAttendeesCalendar = getAvailability(busyTimesForOptionalAttendeesList);
      return optionalAttendeesCalendar;
    } else {
      ArrayList<TimeRange> requiredAttendeesCalendar = getAvailability(busyTimesForRequiredAttendeesList);
      ArrayList<TimeRange> requiredAndOptionalAttendeesCalendar = new ArrayList<>(requiredAttendeesCalendar);

      for (TimeRange busyTimeForOptionalAttendee : busyTimesForOptionalAttendeesList) {
        for (TimeRange freeTimeForRequiredAttendee : requiredAttendeesCalendar) {
          if (permanent.overlaps(busyTimeForOptionalAttendee)) {
            requiredAndOptionalAttendeesCalendar.remove(freeTimeForRequiredAttendee);
          }
        }
      }
      requiredAndOptionalAttendeesCalendar.isEmpty() ? return requiredAttendeesCalendar : return requiredAndOptionalAttendeesCalendar;
    }
  }
}
