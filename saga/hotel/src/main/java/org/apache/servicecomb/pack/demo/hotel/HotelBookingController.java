/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.pack.demo.hotel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class HotelBookingController {
  @Autowired
  private HotelBookingService service;

  private final AtomicInteger id = new AtomicInteger(0);

  @CrossOrigin
  @GetMapping("/bookings")
  List<HotelBooking> getAll() {
    return new ArrayList<>(service.getAllBookings());
  }

  @PostMapping("/order/{name}/{rooms}")
  HotelBooking order(@PathVariable String name, @PathVariable Integer rooms) {
    HotelBooking booking = new HotelBooking();
    booking.setId(id.incrementAndGet());
    booking.setName(name);
    booking.setAmount(rooms);
    service.order(booking);
    return booking;
  }

  @DeleteMapping("/bookings")
  void clear() {
    service.clearAllBookings();
    id.set(0);
  }
}
