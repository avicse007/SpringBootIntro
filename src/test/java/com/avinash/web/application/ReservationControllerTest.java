package com.avinash.web.application;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import com.avinash.business.domain.RoomReservation;
import com.avinash.business.service.ReservationService;









@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(ReservationController.class)
public class ReservationControllerTest {
	
	@MockBean
	private ReservationService reservationService;
	
	@Autowired
	private MockMvc mockMvc;
	
	@Test
	public void getReservations()throws Exception{
		String date = "2017-11-12";
		List<RoomReservation> mockRoomReservationList = new ArrayList<RoomReservation>();
		RoomReservation mockRoomReservation = new RoomReservation();
		mockRoomReservation.setRoomId(112);
		mockRoomReservation.setRoomNumber("134C");
		mockRoomReservation.setRoomName("TEST");
		mockRoomReservation.setFirstName("Tester");
		mockRoomReservation.setLastName("Last");
		mockRoomReservation.setGuestId(123);
		mockRoomReservation.setDate(new Date());
		mockRoomReservationList.add(mockRoomReservation);
		
		org.mockito.BDDMockito.given(reservationService.getRoomReservationsForDate(date)).
		willReturn(mockRoomReservationList);
		
		this.mockMvc.perform(get("/reservations?data=2017-11-12")).andExpect(status().isOk());
		this.mockMvc.perform(get("/reservations?data=2017-11-12")).andReturn();
		
	}

}
