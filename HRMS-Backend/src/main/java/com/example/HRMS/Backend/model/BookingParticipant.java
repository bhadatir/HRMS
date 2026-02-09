package com.example.HRMS.Backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "booking_participants")
public class BookingParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_participant_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    @JoinColumn(name = "fk_game_booking_id")
    private GameBooking fkGameBooking;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    @JoinColumn(name = "fk_booking_waiting_list_id")
    private BookingWaitingList fkBookingWaitingList;

    @NotNull(message = "Employee id is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_employee_id", nullable = false)
    private Employee fkEmployee;

//
//    @NotNull(message = "GameBookingStatus id is required")
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "fk_participant_game_booking_status_id", nullable = false)
//    private GameBookingStatus fkParticipantGameBookingStatus;

}


//
//drop trigger dbo.I_game_booking_from_Waiting_list
//
//Create trigger dbo.I_game_booking_from_Waiting_list
//on game_booking
//after update
//as
//        begin
//set nocount on;
//
//declare @SloatDuration int;
//declare @GameType int;
//declare @TopGameBooking int;
//declare @TopGameBookingFirstTime int;
//declare @TopGameBookingSecondTime int;
//declare @TopIsSecondTime bit = 0;
//declare @EarlyBook date;
//declare @CreatedAt date;
//
//select top 1 @TopGameBookingFirstTime = w.pk_waiting_id from inserted i
//inner join deleted d on i.pk_game_booking_id = d.pk_game_booking_id
//inner join booking_waiting_list w
//on w.fk_game_type_id = i.fk_game_type_id and
//w.target_slot_datetime = i.game_booking_start_time
//        where
//d.fk_game_booking_status_id <> 3 /* cancel */
//and i.fk_game_booking_status_id = 3
//and w.is_second_time_attempt = 0
//and w.waiting_status_is_active = 1
//order by w.waiting_list_created_at ASC;
//
//select top 1 @TopGameBookingSecondTime = w.pk_waiting_id, @EarlyBook = w.target_slot_datetime from inserted i
//inner join deleted d on i.pk_game_booking_id = d.pk_game_booking_id
//inner join booking_waiting_list w
//on w.fk_game_type_id = i.fk_game_type_id and
//w.target_slot_datetime = i.game_booking_start_time
//        where
//d.fk_game_booking_status_id <> 3 /* cancel */
//and i.fk_game_booking_status_id = 3
//and w.is_second_time_attempt = 1
//and w.waiting_status_is_active = 1
//order by w.waiting_list_created_at ASC;
//
//        if @TopGameBookingFirstTime is null
//begin
//            if DATEDIFF(minute,GETDATE(),@EarlyBook) < 30
//begin
//set @TopGameBooking = @TopGameBookingSecondTime;
//set @TopIsSecondTime = 1;
//end
//            else
//                    return;
//end
//        else
//set @TopGameBooking = @TopGameBookingFirstTime;
//
//
//
//select @GameType = fk_game_type_id from inserted;
//
//select @SloatDuration = game_slot_duration
//from game_type where pk_game_type_id = @GameType;
//
//insert into game_booking (fk_game_type_id,
//                          game_booking_start_time,
//                          game_booking_end_time,
//                          game_booking_created_at,
//                          is_second_time_play,
//                          fk_game_booking_status_id,
//                          fk_host_employee_id)
//select w.fk_game_type_id,
//w.target_slot_datetime,
//DATEADD(MINUTE ,@SloatDuration,w.target_slot_datetime),
//GETDATE(),
//@TopIsSecondTime,
//        1, /* accepted */
//w.fk_host_employee_id
//from inserted i
//inner join deleted d on i.pk_game_booking_id = d.pk_game_booking_id
//inner join booking_waiting_list w
//on w.fk_game_type_id = i.fk_game_type_id and
//w.target_slot_datetime = i.game_booking_start_time
//        where
//w.pk_waiting_id = (
//@TopGameBooking
//        )
//
//
//delete from booking_waiting_list
//where pk_waiting_id in (
//        @TopGameBooking
//        );
//
//end;
//
//
//
//
///*
//
//
//Create trigger dbo.I_game_booking_from_Waiting_list
//on game_booking
//after update
//as
//begin
//    set nocount on;
//
//    declare @SloatDuration int;
//    declare @GameType int;
//    declare @TopGameBooking int;
//
//    select top 1
//    @TopGameBooking = w.pk_waiting_id
//        FROM inserted i
//        inner join deleted d on i.pk_game_booking_id = d.pk_game_booking_id
//        inner join booking_waiting_list w
//        on w.fk_game_type_id = i.fk_game_type_id and
//            w.target_slot_datetime = i.game_booking_start_time
//        where
//            d.fk_game_booking_status_id <> 3
//            and i.fk_game_booking_status_id = 3
//        order by w.waiting_list_created_at ASC;
//
//    select @GameType = fk_game_type_id from inserted;
//
//    select @SloatDuration = game_slot_duration
//    from game_type where pk_game_type_id = @GameType;
//
//    insert into game_booking (fk_game_type_id,
//        game_booking_start_time,
//        game_booking_end_time,
//        game_booking_created_at,
//        fk_game_booking_status_id,
//        fk_host_employee_id)
//    select w.fk_game_type_id,
//    w.target_slot_datetime,
//    DATEADD(MINUTE ,@SloatDuration,w.target_slot_datetime),
//    GETDATE(),
//    1,
//    w.fk_host_employee_id
//    from inserted i
//        inner join deleted d on i.pk_game_booking_id = d.pk_game_booking_id
//        inner join booking_waiting_list w
//        on w.fk_game_type_id = i.fk_game_type_id and
//            w.target_slot_datetime = i.game_booking_start_time
//    where
//        d.fk_game_booking_status_id <> 3
//        and i.fk_game_booking_status_id = 3
//        and w.pk_waiting_id = (
//            select top 1 w2.pk_waiting_id
//            from booking_waiting_list w2
//            where w.fk_game_type_id = i.fk_game_type_id and
//            w.target_slot_datetime = i.game_booking_start_time
//            ORDER BY w2.waiting_list_created_at ASC
//        )
//
//
//    delete from booking_waiting_list
//    where pk_waiting_id in (
//        select top 1 w.pk_waiting_id
//        FROM inserted i
//        inner join deleted d on i.pk_game_booking_id = d.pk_game_booking_id
//        inner join booking_waiting_list w
//        on w.fk_game_type_id = i.fk_game_type_id and
//            w.target_slot_datetime = i.game_booking_start_time
//        where
//            d.fk_game_booking_status_id <> 3
//            and i.fk_game_booking_status_id = 3
//        order by w.waiting_list_created_at ASC
//    );
//
//end;
//
//
//*/
