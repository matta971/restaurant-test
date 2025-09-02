package com.restaurant.service.reservation.infrastructure.adapter.in.web;

import com.restaurant.service.reservation.domain.model.Reservation;
import com.restaurant.service.reservation.domain.model.ReservationStatus;
import com.restaurant.service.reservation.domain.port.in.ReservationManagementUseCase;
import com.restaurant.service.reservation.infrastructure.adapter.in.web.dto.ReservationRequestDto;
import com.restaurant.service.reservation.infrastructure.adapter.in.web.dto.ReservationResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * REST Controller for Reservation operations
 * Provides REST API endpoints with HATEOAS support
 */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reservations", description = "Restaurant reservation management")
public class ReservationController {

    private final ReservationManagementUseCase reservationManagementUseCase;

    @Operation(summary = "Create a new reservation")
    @PostMapping
    public ResponseEntity<EntityModel<ReservationResponseDto>> createReservation(
            @Valid @RequestBody ReservationRequestDto request) {
        
        log.info("Creating new reservation for customer: {}", request.customerEmail());

        var command = new ReservationManagementUseCase.CreateReservationCommand(
                request.customerEmail(),
                request.customerFirstName(),
                request.customerLastName(),
                request.customerPhoneNumber(),
                request.restaurantId(),
                request.tableId(),
                request.reservationDate(),
                request.startTime(),
                request.endTime(),
                request.partySize(),
                request.specialRequests()
        );

        Reservation reservation = reservationManagementUseCase.createReservation(command);
        
        ReservationResponseDto response = ReservationResponseDto.fromDomain(reservation);
        EntityModel<ReservationResponseDto> entityModel = EntityModel.of(response)
                .add(linkTo(methodOn(ReservationController.class).getReservation(reservation.getId())).withSelfRel())
                .add(linkTo(methodOn(ReservationController.class).confirmReservation(reservation.getId())).withRel("confirm"))
                .add(linkTo(methodOn(ReservationController.class).cancelReservation(reservation.getId(), null)).withRel("cancel"))
                .add(linkTo(methodOn(ReservationController.class).getAllReservations(Pageable.unpaged())).withRel("reservations"));

        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    @Operation(summary = "Get reservation by ID")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ReservationResponseDto>> getReservation(
            @Parameter(description = "Reservation ID") @PathVariable Long id) {
        
        log.debug("Fetching reservation with ID: {}", id);

        Reservation reservation = reservationManagementUseCase.getReservation(id);
        ReservationResponseDto response = ReservationResponseDto.fromDomain(reservation);
        
        EntityModel<ReservationResponseDto> entityModel = EntityModel.of(response)
                .add(linkTo(methodOn(ReservationController.class).getReservation(id)).withSelfRel())
                .add(linkTo(methodOn(ReservationController.class).getAllReservations(Pageable.unpaged())).withRel("reservations"));

        // Add conditional links based on reservation status
        if (reservation.getStatus().allowsConfirmation()) {
            entityModel.add(linkTo(methodOn(ReservationController.class).confirmReservation(id)).withRel("confirm"));
        }
        if (reservation.canBeCancelled()) {
            entityModel.add(linkTo(methodOn(ReservationController.class).cancelReservation(id, null)).withRel("cancel"));
        }
        if (reservation.canBeModified()) {
            entityModel.add(linkTo(methodOn(ReservationController.class).updateReservation(id, null)).withRel("update"));
        }

        return ResponseEntity.ok(entityModel);
    }

    @Operation(summary = "Get all reservations with pagination")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<ReservationResponseDto>>> getAllReservations(
            @PageableDefault(size = 20, sort = "reservationDate") Pageable pageable) {
        
        log.debug("Fetching all reservations with pagination: {}", pageable);

        Page<Reservation> reservations = reservationManagementUseCase.getAllReservations(pageable);
        Page<ReservationResponseDto> responsePage = reservations.map(ReservationResponseDto::fromDomain);
        
        PagedModel<EntityModel<ReservationResponseDto>> pagedModel = PagedModel.of(
                responsePage.getContent().stream()
                        .map(dto -> EntityModel.of(dto)
                                .add(linkTo(methodOn(ReservationController.class).getReservation(dto.id())).withSelfRel()))
                        .toList(),
                new PagedModel.PageMetadata(
                        responsePage.getSize(),
                        responsePage.getNumber(),
                        responsePage.getTotalElements(),
                        responsePage.getTotalPages()
                )
        );

        pagedModel.add(linkTo(methodOn(ReservationController.class).getAllReservations(pageable)).withSelfRel());

        return ResponseEntity.ok(pagedModel);
    }

    @Operation(summary = "Get reservations by customer email")
    @GetMapping("/customer/{email}")
    public ResponseEntity<CollectionModel<EntityModel<ReservationResponseDto>>> getReservationsByCustomer(
            @Parameter(description = "Customer email") @PathVariable String email) {
        
        log.debug("Fetching reservations for customer: {}", email);

        List<Reservation> reservations = reservationManagementUseCase.getReservationsByCustomer(email);
        List<EntityModel<ReservationResponseDto>> entityModels = reservations.stream()
                .map(reservation -> {
                    ReservationResponseDto dto = ReservationResponseDto.fromDomain(reservation);
                    return EntityModel.of(dto)
                            .add(linkTo(methodOn(ReservationController.class).getReservation(reservation.getId())).withSelfRel());
                })
                .toList();

        CollectionModel<EntityModel<ReservationResponseDto>> collectionModel = 
                CollectionModel.of(entityModels)
                        .add(linkTo(methodOn(ReservationController.class).getReservationsByCustomer(email)).withSelfRel())
                        .add(linkTo(methodOn(ReservationController.class).getAllReservations(Pageable.unpaged())).withRel("reservations"));

        return ResponseEntity.ok(collectionModel);
    }

    @Operation(summary = "Get upcoming reservations for customer")
    @GetMapping("/customer/{email}/upcoming")
    public ResponseEntity<CollectionModel<EntityModel<ReservationResponseDto>>> getUpcomingReservations(
            @Parameter(description = "Customer email") @PathVariable String email) {
        
        log.debug("Fetching upcoming reservations for customer: {}", email);

        List<Reservation> reservations = reservationManagementUseCase.getUpcomingReservations(email);
        List<EntityModel<ReservationResponseDto>> entityModels = reservations.stream()
                .map(reservation -> {
                    ReservationResponseDto dto = ReservationResponseDto.fromDomain(reservation);
                    return EntityModel.of(dto)
                            .add(linkTo(methodOn(ReservationController.class).getReservation(reservation.getId())).withSelfRel());
                })
                .toList();

        CollectionModel<EntityModel<ReservationResponseDto>> collectionModel = 
                CollectionModel.of(entityModels)
                        .add(linkTo(methodOn(ReservationController.class).getUpcomingReservations(email)).withSelfRel())
                        .add(linkTo(methodOn(ReservationController.class).getReservationsByCustomer(email)).withRel("all-reservations"));

        return ResponseEntity.ok(collectionModel);
    }

    @Operation(summary = "Get reservations by restaurant")
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<PagedModel<EntityModel<ReservationResponseDto>>> getReservationsByRestaurant(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @PageableDefault(size = 20, sort = "reservationDate") Pageable pageable) {
        
        log.debug("Fetching reservations for restaurant: {}", restaurantId);

        Page<Reservation> reservations = reservationManagementUseCase.getReservationsByRestaurant(restaurantId, pageable);
        Page<ReservationResponseDto> responsePage = reservations.map(ReservationResponseDto::fromDomain);
        
        PagedModel<EntityModel<ReservationResponseDto>> pagedModel = PagedModel.of(
                responsePage.getContent().stream()
                        .map(dto -> EntityModel.of(dto)
                                .add(linkTo(methodOn(ReservationController.class).getReservation(dto.id())).withSelfRel()))
                        .toList(),
                new PagedModel.PageMetadata(
                        responsePage.getSize(),
                        responsePage.getNumber(),
                        responsePage.getTotalElements(),
                        responsePage.getTotalPages()
                )
        );

        pagedModel.add(linkTo(methodOn(ReservationController.class).getReservationsByRestaurant(restaurantId, pageable)).withSelfRel());

        return ResponseEntity.ok(pagedModel);
    }

    @Operation(summary = "Get reservations by status")
    @GetMapping("/status/{status}")
    public ResponseEntity<CollectionModel<EntityModel<ReservationResponseDto>>> getReservationsByStatus(
            @Parameter(description = "Reservation status") @PathVariable ReservationStatus status) {
        
        log.debug("Fetching reservations with status: {}", status);

        List<Reservation> reservations = reservationManagementUseCase.getReservationsByStatus(status);
        List<EntityModel<ReservationResponseDto>> entityModels = reservations.stream()
                .map(reservation -> {
                    ReservationResponseDto dto = ReservationResponseDto.fromDomain(reservation);
                    return EntityModel.of(dto)
                            .add(linkTo(methodOn(ReservationController.class).getReservation(reservation.getId())).withSelfRel());
                })
                .toList();

        CollectionModel<EntityModel<ReservationResponseDto>> collectionModel = 
                CollectionModel.of(entityModels)
                        .add(linkTo(methodOn(ReservationController.class).getReservationsByStatus(status)).withSelfRel())
                        .add(linkTo(methodOn(ReservationController.class).getAllReservations(Pageable.unpaged())).withRel("reservations"));

        return ResponseEntity.ok(collectionModel);
    }

    @Operation(summary = "Get reservations for a specific date")
    @GetMapping("/restaurant/{restaurantId}/date/{date}")
    public ResponseEntity<CollectionModel<EntityModel<ReservationResponseDto>>> getReservationsForDate(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @Parameter(description = "Date (YYYY-MM-DD)") @PathVariable LocalDate date) {
        
        log.debug("Fetching reservations for restaurant {} on date: {}", restaurantId, date);

        List<Reservation> reservations = reservationManagementUseCase.getReservationsForDate(restaurantId, date);
        List<EntityModel<ReservationResponseDto>> entityModels = reservations.stream()
                .map(reservation -> {
                    ReservationResponseDto dto = ReservationResponseDto.fromDomain(reservation);
                    return EntityModel.of(dto)
                            .add(linkTo(methodOn(ReservationController.class).getReservation(reservation.getId())).withSelfRel());
                })
                .toList();

        CollectionModel<EntityModel<ReservationResponseDto>> collectionModel = 
                CollectionModel.of(entityModels)
                        .add(linkTo(methodOn(ReservationController.class).getReservationsForDate(restaurantId, date)).withSelfRel())
                        .add(linkTo(methodOn(ReservationController.class).getReservationsByRestaurant(restaurantId, Pageable.unpaged())).withRel("restaurant-reservations"));

        return ResponseEntity.ok(collectionModel);
    }

    @Operation(summary = "Update reservation")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<ReservationResponseDto>> updateReservation(
            @Parameter(description = "Reservation ID") @PathVariable Long id,
            @Valid @RequestBody ReservationRequestDto request) {
        
        log.info("Updating reservation with ID: {}", id);

        var command = new ReservationManagementUseCase.UpdateReservationCommand(
                id,
                request.reservationDate(),
                request.startTime(),
                request.endTime(),
                request.partySize(),
                request.specialRequests()
        );

        Reservation reservation = reservationManagementUseCase.updateReservation(command);
        ReservationResponseDto response = ReservationResponseDto.fromDomain(reservation);
        
        EntityModel<ReservationResponseDto> entityModel = EntityModel.of(response)
                .add(linkTo(methodOn(ReservationController.class).getReservation(id)).withSelfRel())
                .add(linkTo(methodOn(ReservationController.class).getAllReservations(Pageable.unpaged())).withRel("reservations"));

        return ResponseEntity.ok(entityModel);
    }

    @Operation(summary = "Confirm reservation")
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<EntityModel<ReservationResponseDto>> confirmReservation(
            @Parameter(description = "Reservation ID") @PathVariable Long id) {
        
        log.info("Confirming reservation with ID: {}", id);

        Reservation reservation = reservationManagementUseCase.confirmReservation(id);
        ReservationResponseDto response = ReservationResponseDto.fromDomain(reservation);
        
        EntityModel<ReservationResponseDto> entityModel = EntityModel.of(response)
                .add(linkTo(methodOn(ReservationController.class).getReservation(id)).withSelfRel())
                .add(linkTo(methodOn(ReservationController.class).cancelReservation(id, null)).withRel("cancel"))
                .add(linkTo(methodOn(ReservationController.class).completeReservation(id)).withRel("complete"));

        return ResponseEntity.ok(entityModel);
    }

    @Operation(summary = "Cancel reservation")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<EntityModel<ReservationResponseDto>> cancelReservation(
            @Parameter(description = "Reservation ID") @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        
        log.info("Cancelling reservation with ID: {}", id);

        String cancellationReason = reason != null ? reason : "Cancelled by customer";
        Reservation reservation = reservationManagementUseCase.cancelReservation(id, cancellationReason);
        ReservationResponseDto response = ReservationResponseDto.fromDomain(reservation);
        
        EntityModel<ReservationResponseDto> entityModel = EntityModel.of(response)
                .add(linkTo(methodOn(ReservationController.class).getReservation(id)).withSelfRel())
                .add(linkTo(methodOn(ReservationController.class).getAllReservations(Pageable.unpaged())).withRel("reservations"));

        return ResponseEntity.ok(entityModel);
    }

    @Operation(summary = "Complete reservation")
    @PatchMapping("/{id}/complete")
    public ResponseEntity<EntityModel<ReservationResponseDto>> completeReservation(
            @Parameter(description = "Reservation ID") @PathVariable Long id) {
        
        log.info("Completing reservation with ID: {}", id);

        Reservation reservation = reservationManagementUseCase.completeReservation(id);
        ReservationResponseDto response = ReservationResponseDto.fromDomain(reservation);
        
        EntityModel<ReservationResponseDto> entityModel = EntityModel.of(response)
                .add(linkTo(methodOn(ReservationController.class).getReservation(id)).withSelfRel())
                .add(linkTo(methodOn(ReservationController.class).getAllReservations(Pageable.unpaged())).withRel("reservations"));

        return ResponseEntity.ok(entityModel);
    }

    @Operation(summary = "Mark reservation as no-show")
    @PatchMapping("/{id}/no-show")
    public ResponseEntity<EntityModel<ReservationResponseDto>> markAsNoShow(
            @Parameter(description = "Reservation ID") @PathVariable Long id) {
        
        log.info("Marking reservation with ID {} as no-show", id);

        Reservation reservation = reservationManagementUseCase.markAsNoShow(id);
        ReservationResponseDto response = ReservationResponseDto.fromDomain(reservation);
        
        EntityModel<ReservationResponseDto> entityModel = EntityModel.of(response)
                .add(linkTo(methodOn(ReservationController.class).getReservation(id)).withSelfRel())
                .add(linkTo(methodOn(ReservationController.class).getAllReservations(Pageable.unpaged())).withRel("reservations"));

        return ResponseEntity.ok(entityModel);
    }

    @Operation(summary = "Check availability")
    @PostMapping("/check-availability")
    public ResponseEntity<Boolean> checkAvailability(
            @Valid @RequestBody ReservationManagementUseCase.AvailabilityQuery query) {
        
        log.debug("Checking availability for: {}", query);

        boolean available = reservationManagementUseCase.checkAvailability(query);
        return ResponseEntity.ok(available);
    }
}