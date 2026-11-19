package org.group6.childrenelectricvehiclerentalmanagement.Services;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Vehicle.VehicleResponse;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.SessionVehicleRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.SesssionRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.VehicleIncidentRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.BusinessTime;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.VehicleRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class VehicleServiceListTest {

    private final VehicleRepository repository = mock(VehicleRepository.class);
    private final VehicleService service = new VehicleService(repository, mock(SessionVehicleRepository.class), mock(SesssionRepository.class), mock(VehicleIncidentRepository.class), mock(BusinessTime.class));

    @Test
    void nonPositiveSessionIdIsRejectedBeforeQuerying() {
        assertThrows(ValidationException.class, () -> service.list(null, 0L));
        assertThrows(ValidationException.class, () -> service.list("Online", -3L));
        verifyNoInteractions(repository);
    }

    @Test
    void sessionIdAloneUsesTheSessionQuery() throws Exception {
        when(repository.findActiveBySessionId(5L)).thenReturn(List.of());
        List<VehicleResponse> result = service.list(null, 5L);
        assertTrue(result.isEmpty());
        verify(repository).findActiveBySessionId(5L);
    }

    @Test
    void sessionIdWithStatusUsesTheCombinedQuery() throws Exception {
        when(repository.findByStatusAndSessionId("Online", 5L)).thenReturn(List.of());
        List<VehicleResponse> result = service.list("Online", 5L);
        assertTrue(result.isEmpty());
        verify(repository).findByStatusAndSessionId("Online", 5L);
    }

    @Test
    void withoutSessionIdTheOriginalQueriesAreUsed() throws Exception {
        when(repository.findByStatusNotOrderByIdAsc(VehicleStatus.Deleted)).thenReturn(List.of());
        when(repository.findByStatusOrderByIdAsc(VehicleStatus.Offline)).thenReturn(List.of());
        service.list(null, null);
        service.list("Offline", null);
        verify(repository).findByStatusNotOrderByIdAsc(VehicleStatus.Deleted);
        verify(repository).findByStatusOrderByIdAsc(VehicleStatus.Offline);
    }

    @Test
    void invalidStatusIsStillRejectedWithSessionId() {
        assertThrows(ValidationException.class, () -> service.list("Bay", 5L));
    }
}
