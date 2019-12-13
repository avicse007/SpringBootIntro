package com.avinash.web.service;

import com.avinash.data.entity.Room;
import com.avinash.data.repository.RoomRepository;
import com.avinash.business.domain.RoomReservation;
import com.avinash.business.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value="/api")
public class ReservationServiceController {

	
    @Autowired
    private ReservationService reservationService;

    @RequestMapping(method= RequestMethod.GET, value="/reservations/{date}")
    public List<RoomReservation> getAllReservationsForDate(@PathVariable(value="date")String dateString){
        return this.reservationService.getRoomReservationsForDate(dateString);
    }
    
	
	@Autowired
    private RoomRepository repository;
    
    @RequestMapping(method= RequestMethod.GET, value="/rooms")
    public List<Room> findAll(@RequestParam(required=false) String roomNumber){
    	List<Room> rooms = new ArrayList<Room>();
    	if(roomNumber==null) {
    		Iterable<Room> results = this.repository.findAll();
    		results.forEach(room->rooms.add(room));
    	}
    	else {
    		Room room = this.repository.findByNumber(roomNumber);
    		if(room!=null)
    			rooms.add(room);
    	}
    	return rooms;
    }
}
