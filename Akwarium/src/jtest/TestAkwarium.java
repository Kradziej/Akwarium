package jtest;

import static org.junit.Assert.*;

import org.junit.Test;

import packet.PacketConstants;

public class TestAkwarium {

	@Test
	public void test() {
		
		//Aquarium aq1 = Aquarium.getInstance(null, null, 0, false, false, null);
		//Aquarium aq2 = Aquarium.getInstance(null, null, 0, false, false, null);
		
		PacketConstants.Packet p = PacketConstants.Packet.getPacketByOP((short)100);
		if(p == null) System.out.println("aaaa");
		System.out.println(p);
		//p.getClass().getClassLoader().get
		
		//assertEquals(aq1, aq1);
	}

}
