package com.michael.container.notifications.fsm.status;

import com.michael.container.registry.model.StatusChangeEvent;
import com.michael.contract.resources.validations.enums.Status;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

class StatusChangeHandlerOrchestratorTest {
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

    Set<StatusChangeHandler> statusChangeHandlers =
        Set.of(beginState, failState, fixState, maintenanceState, restartState);

    statusChangeHandlers.forEach(
        statusChange -> Mockito.doCallRealMethod().when(statusChange).getStatusStateEvent());

    orchestrator = new StatusChangeOrchestrator(statusChangeHandlers);
  }

  @AfterEach
  void tearDown() {
    Mockito.reset(beginState, failState, fixState, maintenanceState, restartState);
  }

  @ParameterizedTest
  @MethodSource("statusChangeSource")
  void statusChange_CallsCorrectStateManager(
      Status previousStatus, Status newStatus, StatusChangeHandler statusChangeHandler) {
    orchestrator.onStatusChange(
        new StatusChangeEvent("applicationName", "URL", 1, 1, previousStatus, newStatus));

    Mockito.verify(statusChangeHandler).triggerEvent(Mockito.any());
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
        Arguments.of(Status.DOWN, Status.UNDER_MAINTENANCE, maintenanceState));
  }
}
