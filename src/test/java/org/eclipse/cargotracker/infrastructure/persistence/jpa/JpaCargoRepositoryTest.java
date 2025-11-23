package org.eclipse.cargotracker.infrastructure.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.event.Event;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.Itinerary;
import org.eclipse.cargotracker.domain.model.cargo.Leg;
import org.eclipse.cargotracker.domain.model.cargo.RouteSpecification;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JpaCargoRepositoryTest {

  @Mock private EntityManager entityManager;
  @Mock private TypedQuery<Cargo> findByTrackingIdQuery;
  @Mock private TypedQuery<Cargo> findAllQuery;
  @Mock private Event<Cargo> cargoUpdated;
  @Mock private Logger logger;
  @InjectMocks private JpaCargoRepository repository;

  @BeforeEach
  void setUpQueries() {
    lenient()
        .when(entityManager.createNamedQuery("Cargo.findByTrackingId", Cargo.class))
        .thenReturn(findByTrackingIdQuery);
    lenient()
        .when(entityManager.createNamedQuery("Cargo.findAll", Cargo.class))
        .thenReturn(findAllQuery);
  }

  @Test
  void findReturnsCargoWhenPresent() {
    TrackingId trackingId = new TrackingId("FIND1");
    Cargo cargo = new Cargo(trackingId, sampleRouteSpecification());

    when(findByTrackingIdQuery.setParameter("trackingId", trackingId))
        .thenReturn(findByTrackingIdQuery);
    when(findByTrackingIdQuery.getSingleResult()).thenReturn(cargo);

    Cargo result = repository.find(trackingId);

    assertEquals(cargo, result);
  }

  @Test
  void findReturnsNullWhenMissing() {
    TrackingId trackingId = new TrackingId("MISS1");

    when(findByTrackingIdQuery.setParameter("trackingId", trackingId))
        .thenReturn(findByTrackingIdQuery);
    when(findByTrackingIdQuery.getSingleResult()).thenThrow(new NoResultException("missing"));

    Cargo result = repository.find(trackingId);

    assertNull(result);
    verify(logger).log(eq(Level.FINE), any(String.class), any(NoResultException.class));
  }

  @Test
  void storePersistsCargoLegsAndNotifiesListeners() {
    Cargo cargo = new Cargo(new TrackingId("STORE1"), sampleRouteSpecification());
    cargo.assignToRoute(sampleItinerary());

    when(cargoUpdated.fireAsync(cargo)).thenReturn(CompletableFuture.completedFuture(cargo));

    repository.store(cargo);

    cargo.getItinerary().getLegs().forEach(leg -> verify(entityManager).persist(leg));
    verify(entityManager).persist(cargo);
    verify(cargoUpdated).fireAsync(cargo);
  }

  @Test
  void findAllReturnsResults() {
    Cargo first = new Cargo(new TrackingId("ALL1"), sampleRouteSpecification());
    Cargo second = new Cargo(new TrackingId("ALL2"), sampleRouteSpecification());

    when(findAllQuery.getResultList()).thenReturn(List.of(first, second));

    List<Cargo> result = repository.findAll();

    assertEquals(List.of(first, second), result);
  }

  @Test
  void nextTrackingIdCreatesUppercaseToken() {
    TrackingId trackingId = repository.nextTrackingId();

    assertNotNull(trackingId.getIdString());
    assertEquals(8, trackingId.getIdString().length());
    assertTrue(trackingId.getIdString().matches("[0-9A-F]{8}"));
  }

  private RouteSpecification sampleRouteSpecification() {
    return new RouteSpecification(
        SampleLocations.CHICAGO, SampleLocations.STOCKHOLM, LocalDate.now().plusMonths(1));
  }

  private Itinerary sampleItinerary() {
    LocalDateTime start = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    Voyage voyage =
        new Voyage.Builder(new VoyageNumber("VY11"), SampleLocations.CHICAGO)
            .addMovement(SampleLocations.HELSINKI, start, start.plusDays(1))
            .addMovement(SampleLocations.STOCKHOLM, start.plusDays(1), start.plusDays(2))
            .build();

    Leg firstLeg =
        new Leg(
            voyage,
            SampleLocations.CHICAGO,
            SampleLocations.HELSINKI,
            start,
            start.plusDays(1));
    Leg secondLeg =
        new Leg(
            voyage,
            SampleLocations.HELSINKI,
            SampleLocations.STOCKHOLM,
            start.plusDays(1),
            start.plusDays(2));

    return new Itinerary(List.of(firstLeg, secondLeg));
  }
}
