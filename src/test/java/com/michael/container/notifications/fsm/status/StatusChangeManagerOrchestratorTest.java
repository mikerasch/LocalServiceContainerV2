package com.michael.container.notifications.fsm.status;


import com.michael.container.notifications.enums.StatusStateEvent;
import com.michael.container.registry.enums.Status;
import com.michael.container.registry.model.StatusChangeEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.stream.Stream;

class StatusChangeManagerOrchestratorTest {
    static StatusChangeOrchestrator orchestrator;

     static BeginState beginState;
     static FailState failState;
     static FixState fixState;
     static MaintenanceState maintenanceState;
     static RestartState restartState;

    @BeforeAll
    static void setUp() {
        beginState = Mockito.mock(BeginState.class);
        failState = Mockito.mock(FailState.class);
        fixState = Mockito.mock(FixState.class);
        maintenanceState = Mockito.mock(MaintenanceState.class);
        restartState = Mockito.mock(RestartState.class);

        Set<StatusChangeManager> statusChangeManagers = Set.of(beginState, failState, fixState, maintenanceState, restartState);

        statusChangeManagers.forEach(statusChange -> Mockito.doCallRealMethod().when(statusChange).getStatusStateEvent());

        orchestrator = new StatusChangeOrchestrator(statusChangeManagers);
    }

    @AfterEach
    void tearDown() {
        Mockito.reset(beginState, failState, fixState, maintenanceState, restartState);
    }

    @ParameterizedTest
    @MethodSource("statusChangeSource")
    void statusChange_CallsCorrectStateManager(Status previousStatus, Status newStatus, StatusChangeManager statusChangeManager) {
        orchestrator.onStatusChange(new StatusChangeEvent("applicationName", "URL", 1, 1, previousStatus, newStatus));

        Mockito.verify(statusChangeManager).triggerEvent(Mockito.any());
    }

    public static Stream<Arguments> statusChangeSource() {
        return Stream.of(
                // RESTART
                Arguments.of(Status.UNDER_MAINTENANCE, Status.STARTING, restartState),
                // BEGIN
                Arguments.of(Status.STARTING, Status.HEALTHY, beginState),
                // FIX
                Arguments.of(Status.DOWN, Status.HEALTHY, fixState),
                // FIX
                Arguments.of(Status.UNDER_MAINTENANCE, Status.HEALTHY, fixState),
                // FAIL
                Arguments.of(Status.HEALTHY, Status.DOWN, failState),
                // MAINTENANCE
                Arguments.of(Status.DOWN, Status.UNDER_MAINTENANCE, maintenanceState)
        );
    }
}