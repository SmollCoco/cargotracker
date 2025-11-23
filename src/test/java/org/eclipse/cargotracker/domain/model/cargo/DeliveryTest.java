package org.eclipse.cargotracker.domain.model.cargo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.handling.HandlingHistory;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeliveryTest {

  private Cargo cargo;
  private RouteSpecification routeSpecification;
  private Itinerary itinerary;
  private Voyage voyage;
  private Leg firstLeg;
  private Leg secondLeg;
  private LocalDateTime baseTime;

  @BeforeEach
  void setUp() {
    baseTime = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS);
    routeSpecification =
        new RouteSpecification(
            SampleLocations.HONGKONG, SampleLocations.DALLAS, LocalDate.now().plusDays(30));
    cargo = new Cargo(new TrackingId("DEL-123"), routeSpecification);

    voyage =
        new Voyage.Builder(new VoyageNumber("VY01"), SampleLocations.HONGKONG)
            .addMovement(SampleLocations.MELBOURNE, baseTime, baseTime.plusDays(2))
            .addMovement(SampleLocations.DALLAS, baseTime.plusDays(2), baseTime.plusDays(4))
            .build();

    firstLeg =
        new Leg(
            voyage,
            SampleLocations.HONGKONG,
            SampleLocations.MELBOURNE,
            baseTime,
            baseTime.plusDays(1));
    secondLeg =
        new Leg(
            voyage,
            SampleLocations.MELBOURNE,
            SampleLocations.DALLAS,
            baseTime.plusDays(1),
            baseTime.plusDays(3));
    itinerary = new Itinerary(List.of(firstLeg, secondLeg));
    cargo.assignToRoute(itinerary);
  }

  @Test
  void shouldExposeNextExpectedUnloadActivityAfterLoading() {
    List<HandlingEvent> events =
        List.of(
            new HandlingEvent(
                cargo,
                baseTime.minusDays(1),
                baseTime.minusDays(1).plusHours(1),
                HandlingEvent.Type.RECEIVE,
                SampleLocations.HONGKONG),
            new HandlingEvent(
                cargo,
                baseTime,
                baseTime.plusHours(1),
                HandlingEvent.Type.LOAD,
                SampleLocations.HONGKONG,
                voyage));

    Delivery delivery = Delivery.derivedFrom(routeSpecification, itinerary, new HandlingHistory(events));

    assertEquals(TransportStatus.ONBOARD_CARRIER, delivery.getTransportStatus());
    assertEquals(
        new HandlingActivity(HandlingEvent.Type.UNLOAD, SampleLocations.MELBOURNE, voyage),
        delivery.getNextExpectedActivity());
    assertEquals(secondLeg.getUnloadTime(), delivery.getEstimatedTimeOfArrival());
  }

  @Test
  void shouldFlagMisdirectionWhenEventUnexpected() {
    List<HandlingEvent> events =
        List.of(
            new HandlingEvent(
                cargo,
                baseTime.minusDays(1),
                baseTime.minusDays(1).plusHours(1),
                HandlingEvent.Type.RECEIVE,
                SampleLocations.HONGKONG),
            new HandlingEvent(
                cargo,
                baseTime,
                baseTime.plusHours(1),
                HandlingEvent.Type.UNLOAD,
                SampleLocations.TOKYO,
                voyage));

    Delivery delivery = Delivery.derivedFrom(routeSpecification, itinerary, new HandlingHistory(events));

    assertTrue(delivery.isMisdirected());
    assertEquals(Delivery.NO_ACTIVITY, delivery.getNextExpectedActivity());
    assertNull(delivery.getEstimatedTimeOfArrival());
  }

  @Test
  void shouldMarkCargoUnloadedAtDestination() {
    List<HandlingEvent> events =
        List.of(
            new HandlingEvent(
                cargo,
                baseTime.minusDays(2),
                baseTime.minusDays(2).plusHours(1),
                HandlingEvent.Type.RECEIVE,
                SampleLocations.HONGKONG),
            new HandlingEvent(
                cargo,
                baseTime.minusDays(1),
                baseTime.minusDays(1).plusHours(1),
                HandlingEvent.Type.LOAD,
                SampleLocations.HONGKONG,
                voyage),
            new HandlingEvent(
                cargo,
                baseTime,
                baseTime.plusHours(1),
                HandlingEvent.Type.UNLOAD,
                SampleLocations.DALLAS,
                voyage));

    Delivery delivery = Delivery.derivedFrom(routeSpecification, itinerary, new HandlingHistory(events));

    assertTrue(delivery.isUnloadedAtDestination());
    assertEquals(TransportStatus.IN_PORT, delivery.getTransportStatus());
    assertEquals(
        new HandlingActivity(HandlingEvent.Type.CLAIM, SampleLocations.DALLAS),
        delivery.getNextExpectedActivity());
  }
}
