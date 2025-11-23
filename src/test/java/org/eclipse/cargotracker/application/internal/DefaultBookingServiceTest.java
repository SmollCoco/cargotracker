package org.eclipse.cargotracker.application.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.CargoRepository;
import org.eclipse.cargotracker.domain.model.cargo.Itinerary;
import org.eclipse.cargotracker.domain.model.cargo.RouteSpecification;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.location.LocationRepository;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.service.RoutingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultBookingServiceTest {

  @Mock private CargoRepository cargoRepository;
  @Mock private LocationRepository locationRepository;
  @Mock private RoutingService routingService;
  @Mock private Logger logger;
  @InjectMocks private DefaultBookingService bookingService;

  @Test
  void bookNewCargoPersistsAggregate() {
    TrackingId trackingId = new TrackingId("BOOK123");
    UnLocode originCode = SampleLocations.CHICAGO.getUnLocode();
    UnLocode destinationCode = SampleLocations.STOCKHOLM.getUnLocode();

    when(cargoRepository.nextTrackingId()).thenReturn(trackingId);
    when(locationRepository.find(originCode)).thenReturn(SampleLocations.CHICAGO);
    when(locationRepository.find(destinationCode)).thenReturn(SampleLocations.STOCKHOLM);

    LocalDate deadline = LocalDate.now().plusMonths(1);

    TrackingId returned = bookingService.bookNewCargo(originCode, destinationCode, deadline);

    assertEquals(trackingId, returned);

    ArgumentCaptor<Cargo> cargoCaptor = ArgumentCaptor.forClass(Cargo.class);
    verify(cargoRepository).store(cargoCaptor.capture());

    Cargo stored = cargoCaptor.getValue();
    assertEquals(SampleLocations.CHICAGO, stored.getOrigin());
    assertEquals(SampleLocations.STOCKHOLM, stored.getRouteSpecification().getDestination());
    assertEquals(deadline, stored.getRouteSpecification().getArrivalDeadline());
  }

  @Test
  void requestPossibleRoutesReturnsEmptyWhenCargoMissing() {
    TrackingId trackingId = new TrackingId("UNKNOWN");
    when(cargoRepository.find(trackingId)).thenReturn(null);

    List<Itinerary> itineraries = bookingService.requestPossibleRoutesForCargo(trackingId);

    assertTrue(itineraries.isEmpty());
    verifyNoInteractions(routingService);
  }

  @Test
  void changeDestinationRebuildsRouteSpecification() {
    LocalDate deadline = LocalDate.now().plusWeeks(2);
    TrackingId trackingId = new TrackingId("CHANGE1");
    RouteSpecification originalSpec =
        new RouteSpecification(
            SampleLocations.CHICAGO, SampleLocations.STOCKHOLM, deadline);
    Cargo cargo = new Cargo(trackingId, originalSpec);

    when(cargoRepository.find(trackingId)).thenReturn(cargo);
    when(locationRepository.find(SampleLocations.HELSINKI.getUnLocode()))
        .thenReturn(SampleLocations.HELSINKI);

    bookingService.changeDestination(trackingId, SampleLocations.HELSINKI.getUnLocode());

    assertEquals(SampleLocations.HELSINKI, cargo.getRouteSpecification().getDestination());
    verify(cargoRepository).store(cargo);
  }
}
